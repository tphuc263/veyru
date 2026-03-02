import { useState, useRef, useCallback, useEffect } from 'react';
import { toggleLike } from '../services/likeService';
import { showToast } from '../utils/toastService';


export const useOptimisticLike = (
  photoId: string,
  initialIsLiked: boolean,
  initialLikesCount: number,
  onUpdate?: (photoId: string, photo: any) => void
) => {
  const [isLiked, setIsLiked] = useState(initialIsLiked);
  const [likesCount, setLikesCount] = useState(initialLikesCount);
  const [isProcessing, setIsProcessing] = useState(false);
  
  // Request deduplication - prevent multiple simultaneous requests
  const pendingRequestRef = useRef<Promise<any> | null>(null);
  const lastActionTimeRef = useRef<number>(0);
  
  // Track the current photoId to detect when it changes
  const currentPhotoIdRef = useRef(photoId);
  
  const onUpdateRef = useRef(onUpdate);
  useEffect(() => {
    onUpdateRef.current = onUpdate;
  });

  // Sync from parent props when they actually change.
  useEffect(() => {
    if (currentPhotoIdRef.current !== photoId) {
      currentPhotoIdRef.current = photoId;
    }
    setIsLiked(initialIsLiked);
    setLikesCount(initialLikesCount);
  }, [photoId, initialIsLiked, initialLikesCount]);

  const handleLike = useCallback(async () => {
    const now = Date.now();
    if (now - lastActionTimeRef.current < 300) {
      return;
    }
    lastActionTimeRef.current = now;

    if (pendingRequestRef.current) {
      try {
        await pendingRequestRef.current;
      } catch (error) {
      }
      return;
    }

    setIsProcessing(true);
    
    // Save current state for rollback
    const previousState = { isLiked, likesCount };
    
    // Step 1: Optimistic update - UI responds immediately
    const optimisticIsLiked = !isLiked;
    const optimisticLikesCount = optimisticIsLiked 
      ? likesCount + 1 
      : Math.max(0, likesCount - 1);
    
    setIsLiked(optimisticIsLiked);
    setLikesCount(optimisticLikesCount);
    
    // Step 2: Send request to backend
    const requestPromise = toggleLike(photoId);
    pendingRequestRef.current = requestPromise;
    
    try {
      const response = await requestPromise;
      
      // Step 3: Use backend response as source of truth
      const updatedPhoto = response.data;
      
      setIsLiked(updatedPhoto.isLikedByCurrentUser);
      setLikesCount(updatedPhoto.likeCount);
      
      // Step 4: Notify parent component to sync state
      if (onUpdateRef.current) {
        onUpdateRef.current(photoId, updatedPhoto);
      }
      
    } catch (error) {
      // Step 5: Rollback on error
      setIsLiked(previousState.isLiked);
      setLikesCount(previousState.likesCount);
      
      showToast('error', 'Không thể cập nhật. Vui lòng thử lại.');
      console.error('Failed to toggle like:', error);
    } finally {
      setIsProcessing(false);
      pendingRequestRef.current = null;
    }
  }, [photoId, isLiked, likesCount]);

  return {
    isLiked,
    likesCount,
    isProcessing,
    handleLike,
  };
};
