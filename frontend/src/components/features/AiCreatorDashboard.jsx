import { useState } from 'react'
import { analyzeEngagement, suggestPostTiming } from '../../services/aiService'
import '../../assets/styles/components/aiAssistant.css'

const AiCreatorDashboard = () => {
    const [activeTab, setActiveTab] = useState('engagement')
    const [engagementData, setEngagementData] = useState(null)
    const [timingData, setTimingData] = useState(null)
    const [loadingEngagement, setLoadingEngagement] = useState(false)
    const [loadingTiming, setLoadingTiming] = useState(false)
    const [error, setError] = useState('')

    const handleAnalyzeEngagement = async () => {
        setLoadingEngagement(true)
        setError('')
        try {
            const result = await analyzeEngagement({ recentPostCount: 20 })
            setEngagementData(result)
        } catch (err) {
            console.error('Failed to analyze engagement:', err)
            setError('Không thể phân tích. Vui lòng thử lại.')
        } finally {
            setLoadingEngagement(false)
        }
    }

    const handleSuggestTiming = async () => {
        setLoadingTiming(true)
        setError('')
        try {
            const result = await suggestPostTiming()
            setTimingData(result)
        } catch (err) {
            console.error('Failed to get timing suggestions:', err)
            setError('Không thể lấy gợi ý. Vui lòng thử lại.')
        } finally {
            setLoadingTiming(false)
        }
    }

    const getTrendIcon = (trend) => {
        switch (trend) {
            case 'growing': return '📈'
            case 'declining': return '📉'
            case 'stable': return '➡️'
            default: return '📊'
        }
    }

    const getTrendLabel = (trend) => {
        switch (trend) {
            case 'growing': return 'Tăng trưởng'
            case 'declining': return 'Đang giảm'
            case 'stable': return 'Ổn định'
            case 'new_account': return 'Tài khoản mới'
            case 'insufficient_data': return 'Chưa đủ dữ liệu'
            default: return 'Không xác định'
        }
    }

    return (
        <div className="ai-dashboard">
            <div className="ai-dashboard-header">
                <h3><span className="ai-icon">🤖</span> AI Creator Dashboard</h3>
            </div>

            <div className="ai-dashboard-tabs">
                <button
                    className={`ai-tab ${activeTab === 'engagement' ? 'active' : ''}`}
                    onClick={() => setActiveTab('engagement')}
                >
                    📊 Phân tích Engagement
                </button>
                <button
                    className={`ai-tab ${activeTab === 'timing' ? 'active' : ''}`}
                    onClick={() => setActiveTab('timing')}
                >
                    ⏰ Thời điểm đăng bài
                </button>
            </div>

            {error && <p className="ai-error">{error}</p>}

            {/* ===== Engagement Analysis Tab ===== */}
            {activeTab === 'engagement' && (
                <div className="ai-tab-content">
                    {!engagementData ? (
                        <div className="ai-empty-state">
                            <p>Phân tích hiệu suất bài đăng của bạn bằng AI</p>
                            <button
                                className="ai-generate-btn"
                                onClick={handleAnalyzeEngagement}
                                disabled={loadingEngagement}
                            >
                                {loadingEngagement ? (
                                    <><span className="ai-spinner"></span> Đang phân tích...</>
                                ) : (
                                    '📊 Phân tích ngay'
                                )}
                            </button>
                        </div>
                    ) : (
                        <div className="ai-engagement-results">
                            {/* Stats cards */}
                            <div className="ai-stats-grid">
                                <div className="ai-stat-card">
                                    <span className="ai-stat-icon">❤️</span>
                                    <span className="ai-stat-value">{engagementData.averageLikes}</span>
                                    <span className="ai-stat-label">TB Likes</span>
                                </div>
                                <div className="ai-stat-card">
                                    <span className="ai-stat-icon">💬</span>
                                    <span className="ai-stat-value">{engagementData.averageComments}</span>
                                    <span className="ai-stat-label">TB Comments</span>
                                </div>
                                <div className="ai-stat-card">
                                    <span className="ai-stat-icon">⚡</span>
                                    <span className="ai-stat-value">{engagementData.engagementRate}</span>
                                    <span className="ai-stat-label">Engagement</span>
                                </div>
                                <div className="ai-stat-card">
                                    <span className="ai-stat-icon">{getTrendIcon(engagementData.trend)}</span>
                                    <span className="ai-stat-value ai-stat-trend">{getTrendLabel(engagementData.trend)}</span>
                                    <span className="ai-stat-label">Xu hướng</span>
                                </div>
                            </div>

                            {/* AI Summary */}
                            <div className="ai-summary-box">
                                <h4>🤖 Phân tích AI</h4>
                                <p className="ai-summary-text">{engagementData.aiSummary}</p>
                            </div>

                            {/* Top Posts */}
                            {engagementData.topPosts && engagementData.topPosts.length > 0 && (
                                <div className="ai-top-posts">
                                    <h4>🏆 Top bài đăng</h4>
                                    {engagementData.topPosts.map((post, index) => (
                                        <div key={post.photoId || index} className="ai-top-post-item">
                                            <span className="ai-post-rank">#{index + 1}</span>
                                            <div className="ai-post-info">
                                                <p className="ai-post-caption">{post.caption || 'Không có caption'}</p>
                                                <div className="ai-post-stats">
                                                    <span>❤️ {post.likeCount}</span>
                                                    <span>💬 {post.commentCount}</span>
                                                    <span>⚡ {post.engagementScore}</span>
                                                </div>
                                            </div>
                                            {post.imageUrl && (
                                                <img src={post.imageUrl} alt="" className="ai-post-thumb" />
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}

                            <button
                                className="ai-refresh-btn"
                                onClick={handleAnalyzeEngagement}
                                disabled={loadingEngagement}
                            >
                                {loadingEngagement ? 'Đang cập nhật...' : '🔄 Phân tích lại'}
                            </button>
                        </div>
                    )}
                </div>
            )}

            {/* ===== Post Timing Tab ===== */}
            {activeTab === 'timing' && (
                <div className="ai-tab-content">
                    {!timingData ? (
                        <div className="ai-empty-state">
                            <p>AI sẽ gợi ý thời điểm tốt nhất để đăng bài dựa trên dữ liệu của bạn</p>
                            <button
                                className="ai-generate-btn"
                                onClick={handleSuggestTiming}
                                disabled={loadingTiming}
                            >
                                {loadingTiming ? (
                                    <><span className="ai-spinner"></span> Đang phân tích...</>
                                ) : (
                                    '⏰ Xem gợi ý'
                                )}
                            </button>
                        </div>
                    ) : (
                        <div className="ai-timing-results">
                            {/* Timing slots */}
                            <div className="ai-timing-list">
                                <h4>🕐 Thời điểm tốt nhất</h4>
                                {timingData.bestTimes.map((slot, index) => (
                                    <div key={index} className="ai-timing-slot">
                                        <div className="ai-timing-rank">
                                            <span className="ai-timing-medal">
                                                {index === 0 ? '🥇' : index === 1 ? '🥈' : index === 2 ? '🥉' : `#${index + 1}`}
                                            </span>
                                        </div>
                                        <div className="ai-timing-info">
                                            <div className="ai-timing-day">{slot.dayOfWeek}</div>
                                            <div className="ai-timing-time">{slot.timeRange}</div>
                                            <div className="ai-timing-reason">{slot.reason}</div>
                                        </div>
                                        <div className="ai-timing-score">
                                            <div className="ai-score-bar">
                                                <div
                                                    className="ai-score-fill"
                                                    style={{ width: `${Math.min((slot.score / 10) * 100, 100)}%` }}
                                                ></div>
                                            </div>
                                            <span className="ai-score-value">{slot.score}</span>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* AI Summary */}
                            <div className="ai-summary-box">
                                <h4>🤖 Gợi ý từ AI</h4>
                                <p className="ai-summary-text">{timingData.aiSummary}</p>
                            </div>

                            <button
                                className="ai-refresh-btn"
                                onClick={handleSuggestTiming}
                                disabled={loadingTiming}
                            >
                                {loadingTiming ? 'Đang cập nhật...' : '🔄 Cập nhật gợi ý'}
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}

export default AiCreatorDashboard
