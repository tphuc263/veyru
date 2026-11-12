import type { Photo, UnifiedPost } from '../types/api';

const CACHE_TTL_MS = 2 * 60 * 1000; // 2 minutes fresh window

export interface HomeFeedCacheEntry {
    feed: UnifiedPost[];
    nextCursor: string | null;
    hasMore: boolean;
    isExploreFallback: boolean;
    explorePage: number;
    updatedAt: number;
}

export interface SuggestedUsersCacheEntry {
    suggestedUsers: Record<string, unknown>[];
    currentUserProfile: Record<string, unknown> | null;
    followingIds: string[];
    updatedAt: number;
}

export interface ExploreCacheEntry {
    photos: Photo[];
    page: number;
    hasMore: boolean;
    updatedAt: number;
}

export interface ProfileCacheEntry {
    profile: Record<string, unknown>;
    posts: UnifiedPost[];
    currentPage: number;
    totalPages: number;
    updatedAt: number;
}

let homeFeedCache: HomeFeedCacheEntry | null = null;
let suggestedUsersCache: SuggestedUsersCacheEntry | null = null;
let exploreCache: ExploreCacheEntry | null = null;
const profileCacheMap = new Map<string, ProfileCacheEntry>();
let favoritesCache: { items: Photo[]; updatedAt: number } | null = null;

export const pageCache = {
    getHomeFeed: (): HomeFeedCacheEntry | null => homeFeedCache,
    setHomeFeed: (entry: Omit<HomeFeedCacheEntry, 'updatedAt'>): void => {
        homeFeedCache = { ...entry, updatedAt: Date.now() };
    },
    isHomeFeedFresh: (): boolean =>
        Boolean(homeFeedCache && Date.now() - homeFeedCache.updatedAt < CACHE_TTL_MS),

    getSuggestedUsers: (): SuggestedUsersCacheEntry | null => suggestedUsersCache,
    setSuggestedUsers: (entry: Omit<SuggestedUsersCacheEntry, 'updatedAt'>): void => {
        suggestedUsersCache = { ...entry, updatedAt: Date.now() };
    },
    isSuggestedUsersFresh: (): boolean =>
        Boolean(suggestedUsersCache && Date.now() - suggestedUsersCache.updatedAt < CACHE_TTL_MS),

    getExplore: (): ExploreCacheEntry | null => exploreCache,
    setExplore: (entry: Omit<ExploreCacheEntry, 'updatedAt'>): void => {
        exploreCache = { ...entry, updatedAt: Date.now() };
    },
    isExploreFresh: (): boolean =>
        Boolean(exploreCache && Date.now() - exploreCache.updatedAt < CACHE_TTL_MS),

    getProfile: (key: string): ProfileCacheEntry | null => profileCacheMap.get(key) ?? null,
    setProfile: (key: string, entry: Omit<ProfileCacheEntry, 'updatedAt'>): void => {
        profileCacheMap.set(key, { ...entry, updatedAt: Date.now() });
    },
    isProfileFresh: (key: string): boolean => {
        const entry = profileCacheMap.get(key);
        return Boolean(entry && Date.now() - entry.updatedAt < CACHE_TTL_MS);
    },

    getFavorites: (): Photo[] | null => favoritesCache?.items ?? null,
    setFavorites: (items: Photo[]): void => {
        favoritesCache = { items, updatedAt: Date.now() };
    },

    clearAll: (): void => {
        homeFeedCache = null;
        suggestedUsersCache = null;
        exploreCache = null;
        profileCacheMap.clear();
        favoritesCache = null;
    }
};
