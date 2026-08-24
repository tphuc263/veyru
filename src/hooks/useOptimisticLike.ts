import { useState, useRef, useCallback, useEffect } from 'react';
import { toggleLike } from '../services/likeService';
import { showToast } from '../utils/toastService';
import type { Photo } from '../types/api';


export const useOptimisticLike = (
  photoId: string,
  initialIsLiked: boolean,
  initialLikesCount: number,
  onUpdate?: (photoId: string, photo: Partial<Photo>) => void
) => {
  const [isLiked, setIsLiked] = useState(initialIsLiked);
  const [likesCount, setLikesCount] = useState(initialLikesCount);
  const [isProcessing, setIsProcessing] = useState(false);
  
  // Request deduplication - prevent multiple simultaneous requests
  const pendingRequestRef = useRef<Promise<void> | null>(null);
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
      await pendingRequestRef.current.catch(() => undefined);
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
    const requestPromise = toggleLike(photoId, isLiked);
    pendingRequestRef.current = requestPromise;
    
    try {
      await requestPromise;

      const updatedPhoto = {
        isLikedByCurrentUser: optimisticIsLiked,
        likeCount: optimisticLikesCount,
      };

      if (onUpdateRef.current) {
        onUpdateRef.current(photoId, updatedPhoto);
      }
      
    } catch (error) {
      // Step 5: Rollback on error
      setIsLiked(previousState.isLiked);
      setLikesCount(previousState.likesCount);
      
      showToast('error', 'Không thể cập nhật. Thử lại sau.');
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
