// Re-export so `FavoriteLibraryRows`, `IosUiBridge`, etc. from the Kotlin XCFramework resolve in this SPM target after a rebuild of `Shared.xcframework`.
@_exported import Shared
import SwiftUI

// MARK: - Root

public struct TVShowTrackerRootView: View {
    public init() {
        IosBootstrapKt.bootstrapSharedForIos()
    }

    public var body: some View {
        TabView {
            SearchTab()
                .tabItem { Label("Search", systemImage: "magnifyingglass") }
            LibraryTab()
                .tabItem { Label("My shows", systemImage: "heart.fill") }
        }
    }
}

private enum RootRoute: Hashable {
    case detail(Int64)
    case episodes(Int64)
}

// MARK: - Search

@MainActor
private final class SearchScreenModel: ObservableObject {
    private let kotlinVm: SearchViewModel
    private var flow: FlowWrapper<SearchUiState>?

    @Published private(set) var state: SearchUiState

    init() {
        kotlinVm = IosUiBridge.shared.koinSearchViewModel()
        state = SearchUiState(
            query: "",
            results: [],
            page: 0,
            isLoading: false,
            error: nil,
            canLoadMore: false
        )
        flow = IosUiBridge.shared.bindSearchState(vm: kotlinVm) { [weak self] update in
            Task { @MainActor in self?.state = update }
        }
    }

    deinit {
        flow?.cancel()
        kotlinVm.clear()
    }

    func onQueryEdit(_ raw: String) {
        kotlinVm.onQueryChange(q: raw)
    }

    func searchNow() {
        kotlinVm.search(resetPage: true)
    }

    func loadMore() {
        kotlinVm.loadMore()
    }

    func retry() {
        kotlinVm.retry()
    }
}

private struct SearchTab: View {
    @StateObject private var model = SearchScreenModel()
    @State private var field = ""
    @State private var path: [RootRoute] = []
    @State private var debounceTask: Task<Void, Never>?

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()
            NavigationStack(path: $path) {
                searchContent
                    .navigationTitle("Search")
                    .navigationDestination(for: RootRoute.self) { route in
                        switch route {
                        case .detail(let id):
                            DetailScreen(showId: id, path: $path)
                        case .episodes(let id):
                            EpisodesScreen(showId: id)
                        }
                    }
            }
        }
        .onChange(of: field) { newValue in
            model.onQueryEdit(newValue)
            debounceTask?.cancel()
            debounceTask = Task {
                try? await Task.sleep(nanoseconds: 450_000_000)
                guard !Task.isCancelled else { return }
                guard newValue.trimmingCharacters(in: .whitespacesAndNewlines).count >= 2 else { return }
                await MainActor.run { model.searchNow() }
            }
        }
    }

    private var searchContent: some View {
        let st = model.state

        return VStack(alignment: .leading, spacing: 12) {
            TextField("Title (≥2 chars)", text: $field)
                .textFieldStyle(.roundedBorder)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled(true)

            Button("Search now") {
                model.searchNow()
            }
            .buttonStyle(.borderedProminent)

            contentBody(st)
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }

    @ViewBuilder
    private func contentBody(_ st: SearchUiState) -> some View {
        if st.isLoading, st.results.isEmpty {
            Spacer()
            ProgressView().frame(maxWidth: .infinity)
            Spacer()
        } else if let err = st.error, st.results.isEmpty {
            Text(err).foregroundStyle(.red)
            Button("Retry") { model.retry() }
        } else if st.results.isEmpty,
                  field.trimmingCharacters(in: .whitespacesAndNewlines).count >= 2,
                  !st.isLoading {
            Text("No results").foregroundStyle(.secondary)
        } else {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 10) {
                    ForEach(Array(st.results.enumerated()), id: \.element.id) { index, show in
                        Button {
                            path.append(.detail(show.id))
                        } label: {
                            SearchResultRow(summary: show)
                        }
                        .buttonStyle(.plain)
                        .onAppear {
                            if index >= max(0, st.results.count - 3),
                               st.canLoadMore, !st.isLoading {
                                model.loadMore()
                            }
                        }
                    }
                    if st.isLoading, !st.results.isEmpty {
                        ProgressView()
                            .padding()
                            .frame(maxWidth: .infinity)
                    }
                }
            }
        }
    }
}

private struct SearchResultRow: View {
    let summary: ShowSummary

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            PosterImage(urlString: summary.posterUrl, height: 110)
                .frame(width: 80)
            VStack(alignment: .leading, spacing: 4) {
                Text(summary.name)
                    .font(.headline)
                    .foregroundStyle(.primary)
                if let plain = summary.summaryPlain {
                    Text(plain)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(3)
                }
            }
        }
        .padding(10)
        .background(Color(.secondarySystemGroupedBackground))
        .cornerRadius(10)
    }
}

// MARK: - Library

@MainActor
private final class LibraryScreenModel: ObservableObject {
    private let kotlinVm: LibraryViewModel
    private var flow: FlowWrapper<FavoriteLibraryRows>?

    @Published private(set) var favorites: [FavoriteListItem] = []

    init() {
        kotlinVm = IosUiBridge.shared.koinLibraryViewModel()
        flow = IosUiBridge.shared.bindLibraryRows(vm: kotlinVm) { [weak self] rows in
            Task { @MainActor in
                guard let self else { return }
                self.favorites = (rows.items as NSArray).compactMap { $0 as? FavoriteListItem }
            }
        }
    }

    deinit {
        flow?.cancel()
        kotlinVm.clear()
    }

    func remove(_ showId: Int64) {
        kotlinVm.remove(showId: showId)
    }
}

private struct LibraryTab: View {
    @StateObject private var model = LibraryScreenModel()
    @State private var path: [RootRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if model.favorites.isEmpty {
                    Text("No saved shows yet. Add from Search.")
                        .foregroundStyle(.secondary)
                        .padding()
                } else {
                    List {
                        ForEach(model.favorites, id: \.show.id) { item in
                            Button {
                                path.append(.detail(item.show.id))
                            } label: {
                                FavoriteRow(item: item)
                            }
                            .swipeActions {
                                Button(role: .destructive) {
                                    model.remove(item.show.id)
                                } label: {
                                    Label("Remove", systemImage: "trash")
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("My shows")
            .navigationDestination(for: RootRoute.self) { route in
                switch route {
                case .detail(let id):
                    DetailScreen(showId: id, path: $path)
                case .episodes(let id):
                    EpisodesScreen(showId: id)
                }
            }
        }
    }
}

private struct FavoriteRow: View {
    let item: FavoriteListItem

    var body: some View {
        HStack {
            PosterImage(urlString: item.show.posterUrl, height: 56)
                .frame(width: 42)
            VStack(alignment: .leading, spacing: 4) {
                Text(item.show.name)
                    .font(.headline)
                Text("\(item.progressPercent)% · next: \(subtitleNext)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding(.vertical, 4)
    }

    private var subtitleNext: String {
        guard let ep = item.nextEpisode else { return "—" }
        let name = ep.name ?? ""
        return "S\(ep.season)E\(ep.number) \(name)".trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - Detail

@MainActor
private final class DetailScreenModel: ObservableObject {
    let kotlinVm: DetailViewModel
    private var flow: FlowWrapper<DetailUiState>?

    @Published private(set) var state: DetailUiState

    init() {
        kotlinVm = IosUiBridge.shared.koinDetailViewModel()
        state = DetailUiState(
            isLoading: true,
            show: nil,
            isFavorite: false,
            progressLine: nil,
            nextEpisodeLine: nil,
            error: nil
        )
        flow = IosUiBridge.shared.bindDetailState(vm: kotlinVm) { [weak self] update in
            Task { @MainActor in self?.state = update }
        }
    }

    deinit {
        flow?.cancel()
        kotlinVm.clear()
    }

    func load(_ showId: Int64) {
        kotlinVm.load(showId: showId)
    }

    func toggleFavorite(_ showId: Int64) {
        kotlinVm.toggleFavorite(showId: showId)
    }
}

private struct DetailScreen: View {
    let showId: Int64
    @Binding var path: [RootRoute]

    @StateObject private var model = DetailScreenModel()

    var body: some View {
        Group {
            if model.state.isLoading {
                ProgressView()
            } else if let error = model.state.error, model.state.show == nil {
                VStack(spacing: 12) {
                    Text(error).foregroundStyle(.red)
                    Button("Retry") { model.load(showId) }
                }
                .padding()
            } else if let detail = model.state.show {
                ScrollView {
                    VStack(alignment: .leading, spacing: 10) {
                        PosterImage(urlString: detail.posterUrl, height: 220)
                            .frame(maxWidth: .infinity)
                        Text(detail.name).font(.title2)
                        GenreLine(detail)
                        if let r = detail.ratingAverage?.doubleValue {
                            Text(String(format: "Rating: %.1f", r))
                        }
                        if let rt = runtimeText(detail) {
                            Text(rt)
                        }
                        if let lang = detail.language {
                            Text("Language: \(lang)")
                        }
                        if let st = detail.status {
                            Text("Status: \(st)")
                        }
                        if let pr = detail.premiered {
                            Text("Premiered: \(pr)")
                        }
                        if let net = detail.networkName {
                            Text(net)
                        }
                        if let sum = detail.summaryPlain {
                            Text(sum).font(.body)
                        }
                        if let p = model.state.progressLine {
                            Text(p)
                        }
                        if let n = model.state.nextEpisodeLine {
                            Text(n).font(.subheadline).foregroundStyle(.secondary)
                        }
                        Button("Episodes & seasons") {
                            path.append(.episodes(showId))
                        }
                        .buttonStyle(.borderedProminent)
                        .padding(.top, 8)
                    }
                    .padding(16)
                }
            }
        }
        .id(showId)
        .navigationTitle(model.state.show?.name ?? "Show")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    model.toggleFavorite(showId)
                } label: {
                    Image(systemName: model.state.isFavorite ? "heart.fill" : "heart")
                }
            }
        }
        .onAppear { model.load(showId) }
        .onChange(of: showId) { newId in
            model.load(newId)
        }
    }

    private func runtimeText(_ d: ShowDetail) -> String? {
        let mins = Int(d.runtimeMinutes?.intValue ?? d.averageRuntime?.intValue ?? 0)
        return mins > 0 ? "Runtime: \(mins) min (avg)" : nil
    }
}

private struct GenreLine: View {
    let detail: ShowDetail

    init(_ detail: ShowDetail) {
        self.detail = detail
    }

    var body: some View {
        let joined = ((detail.genres as? [String]) ?? []).joined(separator: ", ")
        return Group {
            if !joined.isEmpty {
                Text("Genres: \(joined)").font(.subheadline)
            }
        }
    }
}

// MARK: - Episodes

@MainActor
private final class EpisodesScreenModel: ObservableObject {
    let kotlinVm: EpisodesViewModel
    private var flow: FlowWrapper<EpisodesUiState>?

    @Published private(set) var state: EpisodesUiState

    init() {
        kotlinVm = IosUiBridge.shared.koinEpisodesViewModel()
        state = EpisodesUiState(
            isLoading: true,
            seasons: [],
            watchedIds: Set<KotlinLong>(),
            error: nil
        )
        flow = IosUiBridge.shared.bindEpisodesState(vm: kotlinVm) { [weak self] update in
            Task { @MainActor in self?.state = update }
        }
    }

    deinit {
        flow?.cancel()
        kotlinVm.clear()
    }

    func load(_ showId: Int64) {
        kotlinVm.load(showId: showId)
    }

    func toggle(_ showId: Int64, episode: Episode, watched: Bool) {
        kotlinVm.toggleWatched(showId: showId, episodeId: episode.id, watched: watched)
    }

    func watched(_ episode: Episode) -> Bool {
        kotlinVm.isWatched(episode: episode)
    }

    func markSeason(showId: Int64, season: Int32, watched: Bool) {
        kotlinVm.setSeasonWatched(showId: showId, season: season, watched: watched)
    }
}

private struct EpisodesScreen: View {
    let showId: Int64

    @StateObject private var model = EpisodesScreenModel()

    var body: some View {
        Group {
            if model.state.isLoading {
                ProgressView()
            } else if let err = model.state.error {
                Text(err).foregroundStyle(.red)
            } else {
                List {
                    ForEach(model.state.seasons, id: \.season) { block in
                        Section(header: Text("Season \(block.season)")) {
                            ForEach(block.episodes, id: \.id) { ep in
                                EpisodeWatchedToggleRow(showId: showId, episode: ep, model: model)
                            }
                            Button("Season watched ✓") {
                                model.markSeason(showId: showId, season: Int32(block.season), watched: true)
                            }
                            .font(.caption)
                            Button("Season unwatched") {
                                model.markSeason(showId: showId, season: Int32(block.season), watched: false)
                            }
                            .font(.caption)
                        }
                    }
                }
            }
        }
        .id(showId)
        .navigationTitle("Episodes")
        .onAppear { model.load(showId) }
        .onChange(of: showId) { newId in
            model.load(newId)
        }
    }
}

private struct EpisodeWatchedToggleRow: View {
    let showId: Int64
    let episode: Episode
    @ObservedObject var model: EpisodesScreenModel

    var body: some View {
        Toggle(isOn: watchedBinding) {
            VStack(alignment: .leading) {
                Text("E\(episode.number) · \(episode.name ?? "")")
                    .font(.subheadline)
                if let d = episode.airdate {
                    Text(d).font(.caption2).foregroundStyle(.secondary)
                }
            }
        }
    }

    private var watchedBinding: Binding<Bool> {
        Binding(
            get: { model.watched(episode) },
            set: { model.toggle(showId, episode: episode, watched: $0) }
        )
    }
}

// MARK: - Shared UI

private struct PosterImage: View {
    let urlString: String?
    var height: CGFloat = 120

    var body: some View {
        Group {
            if let s = urlString, let url = URL(string: s) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                    case .failure:
                        Color.gray.opacity(0.25)
                    @unknown default:
                        Color.gray.opacity(0.25)
                    }
                }
            } else {
                Color.gray.opacity(0.25)
            }
        }
        .frame(height: height)
        .clipped()
        .cornerRadius(6)
    }
}
