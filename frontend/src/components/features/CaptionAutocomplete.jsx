import { useState, useEffect, useRef } from 'react'
import '../../assets/styles/components/captionAutocomplete.css'

/**
 * Caption Autocomplete - Shows dropdown suggestions
 */
const CaptionAutocomplete = ({
    value,
    onChange,
    suggestions = [],
    onGenerateSuggestions,
    placeholder = "Write a caption..."
}) => {
    const [showDropdown, setShowDropdown] = useState(false)
    const [selectedIndex, setSelectedIndex] = useState(-1)
    const dropdownRef = useRef(null)

    // Show dropdown when there are suggestions AND user is actively typing
    useEffect(() => {
        if (suggestions && suggestions.length > 0 && value.length > 0) {
            setShowDropdown(true)
            setSelectedIndex(-1)
        } else {
            setShowDropdown(false)
        }
    }, [suggestions, value])

    // Clear suggestions when input is cleared
    useEffect(() => {
        if (!value || value.trim() === '') {
            setShowDropdown(false)
            setSelectedIndex(-1)
        }
    }, [value])

    // Handle click outside to close dropdown
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [])

    const handleInputChange = (e) => {
        const newValue = e.target.value
        onChange(newValue)

        // Generate suggestions when typing
        if (newValue.length >= 2 && onGenerateSuggestions) {
            onGenerateSuggestions(newValue)
        }
    }

    const handleSelectSuggestion = (suggestion) => {
        onChange(suggestion)
        setShowDropdown(false)
        setSelectedIndex(-1)
    }

    const handleKeyDown = (e) => {
        if (!showDropdown || suggestions.length === 0) return

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault()
                setSelectedIndex(prev =>
                    prev < suggestions.length - 1 ? prev + 1 : 0
                )
                break
            case 'ArrowUp':
                e.preventDefault()
                setSelectedIndex(prev =>
                    prev > 0 ? prev - 1 : suggestions.length - 1
                )
                break
            case 'Enter':
                e.preventDefault()
                if (selectedIndex >= 0) {
                    handleSelectSuggestion(suggestions[selectedIndex])
                }
                break
            case 'Escape':
                setShowDropdown(false)
                setSelectedIndex(-1)
                break
            default:
                break
        }
    }

    return (
        <div className="caption-inline-wrapper" ref={dropdownRef}>
            <div className="caption-input-wrapper">
                <textarea
                    className="caption-input-inline"
                    value={value}
                    onChange={handleInputChange}
                    onKeyDown={handleKeyDown}
                    placeholder={placeholder}
                    rows={3}
                    autoComplete="off"
                    autoCorrect="off"
                    spellCheck="false"
                />
            </div>

            {/* Suggestions Dropdown */}
            {showDropdown && suggestions.length > 0 && (
                <div className="suggestions-dropdown">
                    <div className="suggestions-header">
                        <span>✨ AI Suggestions</span>
                        <span className="suggestions-hint">
                            Press <kbd>Enter</kbd> to select
                        </span>
                    </div>
                    <ul className="suggestions-list">
                        {suggestions.map((suggestion, index) => (
                            <li
                                key={index}
                                className={`suggestion-item ${index === selectedIndex ? 'selected' : ''}`}
                                onClick={() => handleSelectSuggestion(suggestion)}
                            >
                                <span className="suggestion-text">{suggestion}</span>
                                <span className="suggestion-action">Click to use</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    )
}

export default CaptionAutocomplete

