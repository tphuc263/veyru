import {useState, useEffect, useRef, useCallback} from "react";
import Cropper from "react-easy-crop";
import {useAuthContext} from "../../context/AuthContext.jsx";
import {
  updateUserProfile,
  getCurrentUserProfile,
} from "../../services/userService";
import {toastSuccess, toastError} from '../../utils/toastService.js';
import {DEFAULT_AVATAR} from '../../utils/constants.js';
import '../../assets/styles/pages/editProfile.css';

const createImage = (url) =>
    new Promise((resolve, reject) => {
        const img = new Image();
        img.addEventListener("load", () => resolve(img));
        img.addEventListener("error", (e) => reject(e));
        img.src = url;
    });

async function getCroppedImg(imageSrc, pixelCrop) {
    const image = await createImage(imageSrc);
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");
    canvas.width = pixelCrop.width;
    canvas.height = pixelCrop.height;
    ctx.drawImage(
        image,
        pixelCrop.x, pixelCrop.y,
        pixelCrop.width, pixelCrop.height,
        0, 0,
        pixelCrop.width, pixelCrop.height
    );
    return new Promise((resolve) => {
        canvas.toBlob((blob) => resolve(blob), "image/jpeg", 0.9);
    });
}

const EditProfileForm = () => {
    const {user: currentUser, setUser} = useAuthContext();

    const [pageLoading, setPageLoading] = useState(true);
    const [error, setError] = useState(null);

     const [formData, setFormData] = useState({
       username: "",
       bio: "",
     });

    const fileInputRef = useRef(null);
    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Crop state
    const [cropModalOpen, setCropModalOpen] = useState(false);
    const [rawImageSrc, setRawImageSrc] = useState(null);
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState(null);

    const onCropComplete = useCallback((_, croppedPixels) => {
        setCroppedAreaPixels(croppedPixels);
    }, []);

    useEffect(() => {
      const fetchProfileForEditing = async () => {
        if (currentUser?.id) {
          try {
            const fullProfileData = await getCurrentUserProfile(); // Hoặc getUserProfile
            setFormData({
              username: fullProfileData.username || "",
              bio: fullProfileData.bio || "",
            });
            setAvatarPreview(fullProfileData.imageUrl || null);
            setError(null);
          } catch (err) {
            console.error("Failed to load profile data for editing:", err);
            setError("Could not load your profile. Please refresh the page.");
          } finally {
            setPageLoading(false);
          }
        }
      };

      fetchProfileForEditing();
    }, [currentUser?.id]);

    const handleInputChange = (e) => {
      const { name, value } = e.target;
      setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleAvatarClick = () => {
        fileInputRef.current.click();
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                toastError.invalidImage();
                return;
            }
            
            // Validate file size (10MB)
            if (file.size > 10 * 1024 * 1024) {
                toastError.imageTooLarge();
                return;
            }
            
            const objectUrl = URL.createObjectURL(file);
            setRawImageSrc(objectUrl);
            setCrop({ x: 0, y: 0 });
            setZoom(1);
            setCropModalOpen(true);
            // Reset input so same file can be re-selected
            e.target.value = "";
        }
    };

    const handleCropConfirm = async () => {
        try {
            const blob = await getCroppedImg(rawImageSrc, croppedAreaPixels);
            const croppedFile = new File([blob], "avatar.jpg", { type: "image/jpeg" });
            setAvatarFile(croppedFile);
            setAvatarPreview(URL.createObjectURL(blob));
            setCropModalOpen(false);
        } catch (err) {
            console.error("Crop failed:", err);
        }
    };

    const handleCropCancel = () => {
        setCropModalOpen(false);
        setRawImageSrc(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);

        const dataToSubmit = new FormData();
        dataToSubmit.append("username", formData.username);
        dataToSubmit.append("bio", formData.bio);
        if (avatarFile) {
            dataToSubmit.append("image", avatarFile);
        }

        try {
            const updatedProfileData = await updateUserProfile(dataToSubmit);
            setUser(updatedProfileData);
            if (avatarFile) {
                toastSuccess.avatarUpdated();
            } else {
                toastSuccess.profileUpdated();
            }
            setAvatarFile(null);
        } catch (error) {
            console.error('Failed to update profile:', error);
            toastError.updateFailed();
        } finally {
            setIsSubmitting(false);
        }
    };

    if (pageLoading) {
      return (
        <div className="edit-profile-container">Loading your profile...</div>
      );
    }

    if (error) {
      return (
        <div className="edit-profile-container error-message">{error}</div>
      );
    }

    return (
      <>
        {/* Crop Modal */}
        {cropModalOpen && (
          <div className="crop-modal-overlay">
            <div className="crop-modal">
              <h3 className="crop-modal-title">Crop your photo</h3>
              <div className="crop-container">
                <Cropper
                  image={rawImageSrc}
                  crop={crop}
                  zoom={zoom}
                  aspect={1}
                  cropShape="round"
                  showGrid={false}
                  onCropChange={setCrop}
                  onZoomChange={setZoom}
                  onCropComplete={onCropComplete}
                />
              </div>
              <div className="crop-zoom-control">
                <span>Zoom</span>
                <input
                  type="range"
                  min={1}
                  max={3}
                  step={0.05}
                  value={zoom}
                  onChange={(e) => setZoom(Number(e.target.value))}
                  className="crop-zoom-slider"
                />
              </div>
              <div className="crop-modal-actions">
                <button className="crop-btn crop-btn-cancel" onClick={handleCropCancel}>Cancel</button>
                <button className="crop-btn crop-btn-confirm" onClick={handleCropConfirm}>Apply</button>
              </div>
            </div>
          </div>
        )}

        <div className="edit-profile-container">
          <form onSubmit={handleSubmit} className="edit-profile-form">
            <div className="form-row form-row-avatar">
              <div className="avatar-preview-wrapper">
                <img
                  src={avatarPreview || DEFAULT_AVATAR}
                  alt="Avatar Preview"
                  className="avatar-preview"
                  onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}
                />
              </div>
              <div className="avatar-actions">
                <span className="current-username">{formData.username}</span>
                <button
                  type="button"
                  className="change-photo-button"
                  onClick={handleAvatarClick}
                >
                  Change profile photo
                </button>
              </div>
            </div>

            <div className="form-row">
              <aside>
                <label htmlFor={"username"}> Username </label>
              </aside>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                autoComplete="username"
              />
            </div>

            <div className="form-row">
              <aside>
                <label htmlFor={"bio"}> Bio </label>
              </aside>
              <input
                type="text"
                id="bio"
                name="bio"
                value={formData.bio}
                onChange={handleInputChange}
                autoComplete="off"
              />
            </div>

            <div className="form-row">
              <aside></aside>
              <div className="input-wrapper">
                <button
                  type="submit"
                  className="submit-button"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? "Submitting..." : "Submit"}
                </button>
              </div>
            </div>
          </form>
          <input
            type="file"
            id="avatar-file-input"
            name="avatar"
            ref={fileInputRef}
            onChange={handleFileChange}
            style={{ display: "none" }}
            accept="image/png, image/jpeg, image/gif"
          />
        </div>
      </>
    );
}
export default EditProfileForm
