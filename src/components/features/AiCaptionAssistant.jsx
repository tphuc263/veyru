import { useState } from 'react'
import { suggestCaptions } from '../../services/aiService'
import '../../assets/styles/components/aiAssistant.css'

const AiCaptionAssistant = ({ onSelectCaption, onSelectTags, currentCaption, currentTags }) => {
    const [loading, setLoading] = useState(false)
    const [mood, setMood] = useState('')
    const [captions, setCaptions] = useState([])
    const [suggestedTags, setSuggestedTags] = useState([])
    const [error, setError] = useState('')
    const [showCaptions, setShowCaptions] = useState(false)

    const moods = [
        { value: 'happy', label: '😊 Vui vẻ' },
        { value: 'aesthetic', label: '✨ Aesthetic' },
        { value: 'inspirational', label: '💪 Cảm hứng' },
        { value: 'funny', label: '😄 Hài hước' },
        { value: 'romantic', label: '💕 Lãng mạn' },
        { value: 'chill', label: '😎 Chill' },
    ]

    const handleGenerate = async () => {
        setLoading(true)
        setError('')
        setCaptions([])
        setSuggestedTags([])
        setShowCaptions(true)

        try {
            const parsedTags = currentTags
                ? currentTags.split(/[,\s]+/).map(t => t.replace('#', '').trim()).filter(Boolean)
                : []

            const result = await suggestCaptions({
                imageDescription: currentCaption?.trim() || 'photo post',
                tags: parsedTags,
                mood: mood || undefined,
                language: 'vi'
            })

            setCaptions(result.captions || [])
            setSuggestedTags(result.suggestedTags || [])
        } catch (err) {
            console.error('Failed to generate captions:', err)
            setError('Không thể tạo gợi ý. Vui lòng thử lại.')
        } finally {
            setLoading(false)
        }
    }

    const handleSelectCaption = (caption) => {
        if (onSelectCaption) onSelectCaption(caption)
    }

    const handleSelectTag = (tag) => {
        if (onSelectTags) onSelectTags(tag)
    }

    const isTagSelected = (tagName) => {
        if (!currentTags) return false
        return currentTags.toLowerCase().includes(tagName.toLowerCase())
    }

    return (
        <div className="ai-inline">
            {/* Mood selector row */}
            <div className="ai-mood-row">
                <span className="ai-mood-label">Phong cách:</span>
                <div className="ai-mood-scroll">
                    {moods.map(m => (
                        <button
                            key={m.value}
                            className={`ai-mood-pill ${mood === m.value ? 'active' : ''}`}
                            onClick={() => setMood(mood === m.value ? '' : m.value)}
                            type="button"
                        >
                            {m.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* AI Generate button */}
            <button
                className="ai-suggest-btn"
                onClick={handleGenerate}
                disabled={loading}
                type="button"
            >
                {loading ? (
                    <>
                        <span className="ai-spinner-sm"></span>
                        Đang tạo gợi ý...
                    </>
                ) : (
                    <>✨ Gợi ý caption</>
                )}
            </button>

            {error && <p className="ai-inline-error">{error}</p>}

            {/* Caption suggestions */}
            {showCaptions && captions.length > 0 && (
                <div className="ai-caption-results">
                    {captions.map((caption, index) => (
                        <div
                            key={index}
                            className="ai-caption-card"
                            onClick={() => handleSelectCaption(caption)}
                        >
                            <p className="ai-caption-preview">{caption}</p>
                            <span className="ai-apply-tag">Dùng</span>
                        </div>
                    ))}
                </div>
            )}

            {/* AI Suggested Tags */}
            {suggestedTags.length > 0 && (
                <div className="ai-suggested-tags">
                    <span className="ai-section-label">🏷️ Hashtag gợi ý:</span>
                    <div className="ai-tag-row">
                        {suggestedTags.map((tag, index) => (
                            <span
                                key={index}
                                className={`ai-tag-pill ${isTagSelected(tag) ? 'selected' : ''}`}
                                onClick={() => handleSelectTag(tag)}
                            >
                                #{tag}
                            </span>
                        ))}
                    </div>
                </div>
            )}
        </div>
    )
}

export default AiCaptionAssistant
