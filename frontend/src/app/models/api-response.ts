export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiErrorResponse {
  success: false;
  message: string;
  error: {
    code: string;
    details: { field: string; message: string }[];
  };
  path: string;
  timestamp: string;
}
