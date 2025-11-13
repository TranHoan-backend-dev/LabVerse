// Types for Account Service API

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  username: string;
  roleName: "PI" | "RESEARCHER" | "STUDENT";
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: string;
  email: string;
  username: string;
  fullName: string;
  avatarUrl: string | null;
  role: string;
}

export interface User {
  id: string;
  email: string;
  username: string;
  fullName: string;
  avatarUrl: string | null;
  role: string;
  createdDate?: string;
  updatedDate?: string;
}

export interface WrapperApiResponse<T = any> {
  status: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface UpdateProfileRequest {
  username?: string;
  fullName?: string;
  avatarUrl?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface GoogleLoginRequest {
  idToken: string;
}

export interface VerifyOtpRequest {
  email: string;
  otpCode: string;
}

