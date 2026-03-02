import {useState} from 'react'
import {createPhoto} from '../services/photoService'

export interface CreatePostFormData {
    image: File | null;
    imagePreview: string | null;
    caption: string;
    tags: string;
}

interface CreatePostResponse {
    success: boolean;
    error?: string;
}

const initialFormData: CreatePostFormData = {
    image: null,
    imagePreview: null,
    caption: '',
    tags: ''
}

export const useCreatePost = () => {
    const [formData, setFormData] = useState<CreatePostFormData>(initialFormData)
    const [loading, setLoading] = useState<boolean>(false)
    const [error, setError] = useState<string>('')

    const parseTagsFromInput = (tagString: string): string[] => {
        if (!tagString.trim()) return []

        return tagString
            .split(/[,\s]+/)
            .map(tag => tag.replace('#', '').trim().toLowerCase())
            .filter(tag => tag.length > 0)
            .slice(0, 10)
    }

    const validateCreatePostForm = (data: CreatePostFormData): string | null => {
        if (!data.image) {
            return 'Please select an image'
        }

        if (!data.caption.trim()) {
            return 'Please add a caption'
        }

        if (data.caption.length > 2200) {
            return 'Caption is too long (max 2200 characters)'
        }

        return null
    }

    const submitPost = async (postData: CreatePostFormData): Promise<boolean> => {
        const validationError = validateCreatePostForm(postData)
        if (validationError) {
            setError(validationError)
            return false
        }

        try {
            setLoading(true)
            setError('')

            const formDataToSend = new FormData()
            if (!postData.image) {
                setError('Image file is missing')
                return false
            }
            formDataToSend.append('image', postData.image)
            formDataToSend.append('caption', postData.caption.trim())

            const parsedTags = parseTagsFromInput(postData.tags)
            if (parsedTags.length > 0) {
                parsedTags.forEach(tag => {
                    formDataToSend.append('tags', tag)
                })
            }

            await createPhoto(formDataToSend);
            return true

        } catch (error) {
            console.error('Failed to create post:', error)
            setError('Failed to create post. Please try again.')
            return false
        } finally {
            setLoading(false)
        }
    }

    const resetForm = () => {
        if (formData.imagePreview) {
            URL.revokeObjectURL(formData.imagePreview)
        }
        setFormData(initialFormData)
        setError('')
    }

    return {
        formData,
        setFormData,
        loading,
        error,
        setError,
        submitPost,
        resetForm,
        validateForm: validateCreatePostForm,
        parseTagsFromInput
    }
}