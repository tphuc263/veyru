// Common API types
export interface ApiResponse<T = any> {
  data: T;
  message?: string;
  status?: number;
}

export interface PaginationParams {
  page?: number;
  size?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Auth types
export interface LoginCredentials {
  identifier: string; // Can be email, username, or phone number
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}

export interface AuthResponse {
  jwt: string;
  id: number;
  username: string;
  email: string;
  role?: string;
}

// User types
export interface User {
  id: number | string;
  username: string;
  email: string;
  bio?: string;
  imageUrl?: string;
  createdAt?: string;
  followerCount?: number;
  followingCount?: number;
  followingByCurrentUser?: boolean;
  stats?: {
    posts?: number;
    followers?: number;
    following?: number;
  };
}

export interface UpdateProfileData {
  username?: string;
  bio?: string;
  profilePicture?: File;
}

// Photo types
export interface Photo {
  id: number | string;
  userId?: string;
  username?: string;
  userImageUrl?: string;
  imageUrl: string;
  caption?: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
  isLikedByCurrentUser: boolean;
  tags?: string[];
}

export interface CreatePhotoData {
  caption?: string;
  image: File;
}

// Search types
export interface SearchUsersResponse {
  content: User[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Validation types
export interface ValidationErrors {
  [key: string]: string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationErrors;
}

// ==================== AI Types ====================
export interface CaptionSuggestionRequest {
  imageDescription?: string;
  tags?: string[];
  mood?: string;
  language?: string;
}

export interface CaptionSuggestionResponse {
  captions: string[];
  suggestedTags: string[];
}

export interface EngagementAnalysisRequest {
  recentPostCount?: number;
}

export interface PostInsight {
  photoId: string;
  caption: string;
  imageUrl: string;
  likeCount: number;
  commentCount: number;
  engagementScore: number;
}

export interface EngagementAnalysisResponse {
  averageLikes: number;
  averageComments: number;
  engagementRate: number;
  trend: string;
  topPosts: PostInsight[];
  aiSummary: string;
}

export interface TimingSlot {
  dayOfWeek: string;
  timeRange: string;
  score: number;
  reason: string;
}

export interface PostTimingSuggestionResponse {
  bestTimes: TimingSlot[];
  aiSummary: string;
}

// ==================== Recommendation Types ====================
export interface RecommendedUser {
  id: string;
  username: string;
  imageUrl: string;
  bio?: string;
  followerCount: number;
  photoCount: number;
  similarityScore: number;
  reason: string;
}
