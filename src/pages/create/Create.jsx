import {useEffect} from 'react'
import {useNavigate} from 'react-router-dom'
import {useCreatePost} from '../../hooks/useCreatePost.js'
import {toastSuccess, toastError} from '../../utils/toastService.js'
import AiCaptionAssistant from '../../components/features/AiCaptionAssistant.jsx'
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
        }
    }

    const handleCaptionChange = (e) => {
        setFormData(prev => ({...prev, caption: e.target.value}))
        setError('')
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


    return (
        <div className="create-page">
            <div className="create-header">
                <button onClick={handleCancel} className="cancel-btn">Cancel</button>
                <h2>New post</h2>
                <button
                    onClick={handleSubmit}
                    className="share-btn"
                    disabled={loading || !formData.image || !formData.caption.trim()}
                >
                    {loading ? 'Sharing...' : 'Share'}
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
                    onChange={handleCaptionChange}
                    rows="4"
                    className="caption-input"
                    autoComplete="off"
                ></textarea>
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

                <AiCaptionAssistant
                    currentCaption={formData.caption}
                    currentTags={formData.tags}
                    onSelectCaption={(caption) => {
                        setFormData(prev => ({...prev, caption}))
                    }}
                    onSelectTags={(tag) => {
                        setFormData(prev => ({
                            ...prev,
                            tags: prev.tags
                                ? `${prev.tags}, #${tag}`
                                : `#${tag}`
                        }))
                    }}
                />
            </div>
        </div>
    )
}

export default Create