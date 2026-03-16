import {useEffect, useState, useCallback} from 'react'
import {useNavigate} from 'react-router-dom'
import {useCreatePost} from '../../hooks/useCreatePost.js'
import {toastSuccess, toastError} from '../../utils/toastService.js'
import {getTrendingHashtags} from '../../services/aiService.js'
import '../../assets/styles/pages/createPage.css'

const Create = () => {
    const navigate = useNavigate()
    const {
        formData,
        setFormData,
        loading,
        error,
        setError,
        submitPost,
        resetForm
    } = useCreatePost()

    const [trendingHashtags, setTrendingHashtags] = useState([])

    // Analyze image when uploaded
    const analyzeUploadedImage = useCallback(async (file) => {
        try {
            // Convert image to base64
            const base64 = await fileToBase64(file)
            const mimeType = file.type || 'image/jpeg'

            const result = await analyzeImage({
                imageBase64: base64,
                mimeType: mimeType
            })

            if (result) {
                // Auto-fill tags from AI analysis
                if (result.suggestedTags && result.suggestedTags.length > 0) {
                    const tagString = result.suggestedTags.map(t => `#${t}`).join(' ')
                    setFormData(prev => ({
                        ...prev,
                        tags: prev.tags ? `${prev.tags} ${tagString}` : tagString
                    }))
                }
            }
        } catch (err) {
            console.error('Image analysis failed:', err)
        }
    }, [setFormData])

    // Helper: Convert file to base64
    const fileToBase64 = (file) => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.readAsDataURL(file)
            reader.onload = () => resolve(reader.result.split(',')[1])
            reader.onerror = reject
        })
    }

    const handleImageChange = (e) => {
        const file = e.target.files[0]
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                toastError.invalidImage()
                return
            }

            // Validate file size (10MB)
            if (file.size > 10 * 1024 * 1024) {
                toastError.imageTooLarge()
                return
            }

            if (formData.imagePreview) {
                URL.revokeObjectURL(formData.imagePreview)
            }
            const previewUrl = URL.createObjectURL(file)
            setFormData(prev => ({
                ...prev,
                image: file,
                imagePreview: previewUrl
            }))
            setError('')

            // Analyze image with AI
            analyzeUploadedImage(file)
        }
    }

    const handleTagsChange = (e) => {
        setFormData(prev => ({...prev, tags: e.target.value}))
        setError('')
    }

    const handleSubmit = async () => {
        const success = await submitPost(formData)
        if (success) {
            toastSuccess.photoUploaded()
            resetForm()
            navigate('/home')
        } else {
            toastError.uploadFailed()
        }
    }

    const handleCancel = () => {
        resetForm()
        navigate('/')
    }

    useEffect(() => {
        return () => {
            if (formData.imagePreview) {
                URL.revokeObjectURL(formData.imagePreview)
            }
        }
    }, [formData.imagePreview])

    // Load trending hashtags on mount
    useEffect(() => {
        const loadTrendingHashtags = async () => {
            try {
                const hashtags = await getTrendingHashtags(10)
                setTrendingHashtags(hashtags)
            } catch (err) {
                console.error('Failed to load trending hashtags:', err)
            }
        }
        loadTrendingHashtags()
    }, [])

    // Add trending hashtag to tags input
    const addTrendingHashtag = (hashtag) => {
        const currentTags = formData.tags || ''
        // Check if already added
        if (currentTags.toLowerCase().includes(hashtag.toLowerCase())) {
            return
        }
        const newTags = currentTags ? `${currentTags} ${hashtag}` : hashtag
        setFormData(prev => ({...prev, tags: newTags}))
    }


    return (
        <div className="create-page">
            <div className="create-header">
                <button onClick={handleCancel} className="cancel-btn">Cancel</button>
                <h2>New post</h2>
                <button
                    onClick={handleSubmit}
                    className="post-btn"
                    disabled={loading || !formData.image || !formData.caption.trim()}
                >
                    {loading ? 'Posting...' : 'Post'}
                </button>
            </div>

            {error && <p className="error-message">{error}</p>}

            <div className="image-upload-section">
                <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    style={{display: 'none'}}
                    id="image-upload-input"
                    name="image"
                />
                <label htmlFor="image-upload-input" className="image-preview-container">
                    {formData.imagePreview ? (
                        <img src={formData.imagePreview} alt="Selected preview" className="image-preview"/>
                    ) : (
                        <div className="placeholder">
                            <span className="icon">+</span>
                            <p>Select a photo</p>
                        </div>
                    )}
                </label>
            </div>

            <div className="form-section">
                <textarea
                    id="caption-input"
                    name="caption"
                    placeholder="Write a caption..."
                    value={formData.caption}
                    onChange={(e) => {
                        setFormData(prev => ({...prev, caption: e.target.value}))
                        setError('')
                    }}
                    rows={3}
                />

                <input
                    type="text"
                    id="tags-input"
                    name="tags"
                    placeholder="Add tags (e.g., #nature, #travel)"
                    value={formData.tags}
                    onChange={handleTagsChange}
                    className="tags-input"
                    autoComplete="off"
                />

                {/* Trending Hashtags */}
                {trendingHashtags.length > 0 && (
                    <div className="trending-hashtags">
                        <span className="trending-label">🔥 Xu hướng:</span>
                        <div className="trending-tags">
                            {trendingHashtags.map((tag, index) => (
                                <button
                                    key={index}
                                    type="button"
                                    className="trending-tag-btn"
                                    onClick={() => addTrendingHashtag(tag)}
                                >
                                    {tag}
                                </button>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    )
}

export default Create