import { useCallback, useEffect, useState, useRef } from 'react'
import { debounce } from '../../utils/helpers'
import { UserResultItem } from '../../components/features/UserResultItem.jsx'
import { searchUsers } from '../../services/searchService'
import { getExploreFeed } from '../../services/exploreService'
import PhotoModal from '../../components/features/PhotoModal.jsx'
import { Loader } from '../../components/common/Loader.jsx'
import { Heart, MessageCircle, Search as SearchIcon, X } from 'lucide-react'
import '../../assets/styles/pages/searchPage.css'

const Search = () => {
    // Search state
    const [query, setQuery] = useState('')
    const [searchResults, setSearchResults] = useState([])
    const [searchLoading, setSearchLoading] = useState(false)
    const [hasSearched, setHasSearched] = useState(false)

    // Explore state
    const [explorePhotos, setExplorePhotos] = useState([])
    const [exploreLoading, setExploreLoading] = useState(true)
    const [exploreLoadingMore, setExploreLoadingMore] = useState(false)
    const [explorePage, setExplorePage] = useState(0)
    const [exploreHasMore, setExploreHasMore] = useState(true)
    const [exploreError, setExploreError] = useState(null)

    // Modal state
    const [selectedPhotoId, setSelectedPhotoId] = useState(null)

    // Refs
    const inputRef = useRef(null)
    const loadTriggerRef = useRef(null)
    const observerRef = useRef(null)

    const isSearching = query.trim().length > 0

    // ── Search logic ──
    const fetchUsers = async (searchText) => {
        if (!searchText) {
            setSearchResults([])
            setHasSearched(false)
            return
        }

        setSearchLoading(true)
        setHasSearched(true)
        try {
            const users = (await searchUsers(searchText)).content || []
            setSearchResults(users)
        } catch (error) {
            console.error('Failed to fetch users:', error)
            setSearchResults([])
        } finally {
            setSearchLoading(false)
        }
    }

    const debouncedFetch = useCallback(
        debounce((val) => {
            fetchUsers(val).catch(() => {})
        }, 500),
        []
    )

    useEffect(() => {
        debouncedFetch(query)
    }, [query, debouncedFetch])

    // ── Explore logic ──
    const loadExplorePhotos = useCallback(async (pageNum = 0, isRefresh = false) => {
        try {
            if (pageNum === 0) {
                setExploreLoading(true)
            } else {
                setExploreLoadingMore(true)
            }
            setExploreError(null)

            const response = await getExploreFeed(pageNum, 21)
            const newPhotos = response.content || []

            if (isRefresh) {
                setExplorePhotos(newPhotos)
            } else {
                setExplorePhotos(prev => {
                    const existingIds = new Set(prev.map(p => p.id))
                    const unique = newPhotos.filter(p => !existingIds.has(p.id))
                    return [...prev, ...unique]
                })
            }

            setExploreHasMore(response.page < response.totalPages - 1)
            setExplorePage(pageNum)
        } catch (err) {
            setExploreError(err.message)
        } finally {
            setExploreLoading(false)
            setExploreLoadingMore(false)
        }
    }, [])

    // Initial load
    useEffect(() => {
        loadExplorePhotos(0, true)
    }, [loadExplorePhotos])

    // Infinite scroll
    useEffect(() => {
        if (exploreLoadingMore || !exploreHasMore || isSearching) return

        const observer = new IntersectionObserver(
            (entries) => {
                if (entries[0].isIntersecting && exploreHasMore && !exploreLoadingMore) {
                    loadExplorePhotos(explorePage + 1, false)
                }
            },
            { threshold: 0.1 }
        )

        observerRef.current = observer
        if (loadTriggerRef.current) {
            observer.observe(loadTriggerRef.current)
        }

        return () => observer.disconnect()
    }, [exploreHasMore, exploreLoadingMore, explorePage, loadExplorePhotos, isSearching])

    const handlePhotoUpdate = (photoId, updates) => {
        setExplorePhotos(prev =>
            prev.map(photo =>
                photo.id === photoId ? { ...photo, ...updates } : photo
            )
        )
    }

    const clearSearch = () => {
        setQuery('')
        setSearchResults([])
        setHasSearched(false)
        inputRef.current?.focus()
    }

    return (
        <div className="search-explore-page">
            {/* Search bar */}
            <div className="search-bar-wrapper">
                <div className="search-bar">
                    <SearchIcon size={16} className="search-bar-icon" />
                    <input
                        ref={inputRef}
                        type="text"
                        placeholder="Search"
                        className="search-bar-input"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                    {query && (
                        <button className="search-bar-clear" onClick={clearSearch}>
                            <X size={14} />
                        </button>
                    )}
                </div>
            </div>

            {/* Search results overlay */}
            {isSearching && (
                <div className="search-results-section">
                    {searchLoading && (
                        <div className="search-status-message">
                            <Loader />
                        </div>
                    )}

                    {!searchLoading && hasSearched && searchResults.length === 0 && (
                        <div className="search-status-message">
                            <p>No results found.</p>
                        </div>
                    )}

                    {!searchLoading && searchResults.length > 0 && (
                        <div className="search-results-list">
                            {searchResults.map(user => (
                                <UserResultItem key={user.id} user={user} />
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Explore grid (visible when not searching) */}
            {!isSearching && (
                <>
                    {exploreLoading && explorePhotos.length === 0 && (
                        <div className="explore-grid-loading">
                            <Loader />
                        </div>
                    )}

                    {exploreError && explorePhotos.length === 0 && (
                        <div className="explore-grid-error">
                            <p>Could not load explore feed</p>
                            <button onClick={() => loadExplorePhotos(0, true)} className="explore-grid-retry-btn">
                                Try again
                            </button>
                        </div>
                    )}

                    {!exploreLoading && !exploreError && explorePhotos.length === 0 && (
                        <div className="explore-grid-empty">
                            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="var(--ig-secondary-text)" strokeWidth="1">
                                <rect x="3" y="3" width="7" height="7" />
                                <rect x="14" y="3" width="7" height="7" />
                                <rect x="14" y="14" width="7" height="7" />
                                <rect x="3" y="14" width="7" height="7" />
                            </svg>
                            <h3>Explore</h3>
                            <p>Discover photos from people you don't follow yet</p>
                        </div>
                    )}

                    {explorePhotos.length > 0 && (
                        <div className="explore-grid">
                            {explorePhotos.map((photo, index) => (
                                <div
                                    key={photo.id}
                                    className={`explore-grid-item ${getGridClass(index)}`}
                                    onClick={() => setSelectedPhotoId(photo.id)}
                                >
                                    <img
                                        src={photo.imageUrl}
                                        alt={photo.caption || 'Photo'}
                                        loading="lazy"
                                    />
                                    <div className="explore-grid-overlay">
                                        <div className="explore-grid-stats">
                                            <span className="explore-stat">
                                                <Heart size={20} fill="white" stroke="white" />
                                                {formatCount(photo.likeCount)}
                                            </span>
                                            <span className="explore-stat">
                                                <MessageCircle size={20} fill="white" stroke="white" />
                                                {formatCount(photo.commentCount)}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Infinite scroll trigger */}
                    <div ref={loadTriggerRef} className="explore-load-trigger">
                        {exploreLoadingMore && (
                            <div className="explore-grid-loading-more">
                                <Loader />
                            </div>
                        )}
                    </div>
                </>
            )}

            {/* Photo Modal */}
            {selectedPhotoId && (
                <PhotoModal
                    photoId={selectedPhotoId}
                    onClose={() => setSelectedPhotoId(null)}
                    onPhotoUpdate={handlePhotoUpdate}
                />
            )}
        </div>
    )
}

/**
 * Instagram-style grid: every 3rd row has one item spanning 2x2
 * Pattern resets every 18 items (6 rows of 3)
 */
function getGridClass(index) {
    const position = index % 18
    if (position === 2 || position === 11) {
        return 'explore-grid-item-large'
    }
    return ''
}

function formatCount(count) {
    if (count >= 1000000) return (count / 1000000).toFixed(1) + 'M'
    if (count >= 1000) return (count / 1000).toFixed(1) + 'K'
    return count?.toString() || '0'
}

export default Search