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

  const desiredLikedRef = useRef(initialIsLiked);
  const serverLikedRef = useRef(initialIsLiked);
  const countRef = useRef(initialLikesCount);
  const syncingRef = useRef(false);
  const currentPhotoIdRef = useRef(photoId);

  const onUpdateRef = useRef(onUpdate);
  useEffect(() => {
    onUpdateRef.current = onUpdate;
  });

  // Sync from parent props when they change and no background sync is in progress
  useEffect(() => {
    if (currentPhotoIdRef.current !== photoId) {
      currentPhotoIdRef.current = photoId;
      syncingRef.current = false;
    }
    if (!syncingRef.current) {
      desiredLikedRef.current = initialIsLiked;
      serverLikedRef.current = initialIsLiked;
      countRef.current = initialLikesCount;
      setIsLiked(initialIsLiked);
      setLikesCount(initialLikesCount);
    }
  }, [photoId, initialIsLiked, initialLikesCount]);

  const flushLikeSync = useCallback(async (targetPhotoId: string) => {
    if (syncingRef.current) return;
    syncingRef.current = true;
    setIsProcessing(true);

    try {
      while (desiredLikedRef.current !== serverLikedRef.current) {
        const previousServerState = serverLikedRef.current;
        const nextTargetState = desiredLikedRef.current;
        await toggleLike(targetPhotoId, previousServerState);
        serverLikedRef.current = nextTargetState;
      }
      if (onUpdateRef.current) {
        onUpdateRef.current(targetPhotoId, {
          isLikedByCurrentUser: desiredLikedRef.current,
          likeCount: countRef.current,
        });
      }
    } catch (error) {
      // Rollback UI state to last confirmed server state on failure
      const confirmedLiked = serverLikedRef.current;
      const wasOptimisticallyLiked = desiredLikedRef.current;
      desiredLikedRef.current = confirmedLiked;
      if (wasOptimisticallyLiked !== confirmedLiked) {
        countRef.current = confirmedLiked
          ? countRef.current + 1
          : Math.max(0, countRef.current - 1);
      }
      setIsLiked(confirmedLiked);
      setLikesCount(countRef.current);
      if (onUpdateRef.current) {
        onUpdateRef.current(targetPhotoId, {
          isLikedByCurrentUser: confirmedLiked,
          likeCount: countRef.current,
        });
      }
      showToast('error', 'Không thể cập nhật. Thử lại sau.');
      console.error('Failed to toggle like:', error);
    } finally {
      syncingRef.current = false;
      setIsProcessing(false);
    }
  }, []);

  const handleLike = useCallback(() => {
    if (!photoId) return;

    const nextLiked = !desiredLikedRef.current;
    const nextCount = nextLiked
      ? countRef.current + 1
      : Math.max(0, countRef.current - 1);

    desiredLikedRef.current = nextLiked;
    countRef.current = nextCount;

    setIsLiked(nextLiked);
    setLikesCount(nextCount);

    if (onUpdateRef.current) {
      onUpdateRef.current(photoId, {
        isLikedByCurrentUser: nextLiked,
        likeCount: nextCount,
      });
    }

    void flushLikeSync(photoId);
  }, [photoId, flushLikeSync]);

  return {
    isLiked,
    likesCount,
    isProcessing,
    handleLike,
  };
};
