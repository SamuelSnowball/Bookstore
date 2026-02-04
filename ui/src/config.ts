// API Configuration
// Single API Gateway URL - all requests go through auth_service gateway

// Extend Window interface to include our runtime config
declare global {
  interface Window {
    ENV?: {
      VITE_API_URL?: string;
    };
  }
}

const API_URL = window.ENV?.VITE_API_URL || import.meta.env.VITE_API_URL || 'http://localhost:9000';

export const API_CONFIG = {
  API_URL: API_URL,
};
