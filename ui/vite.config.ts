// https://dev.to/samuel_kinuthia/testing-react-applications-with-vitest-a-comprehensive-guide-2jm8
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: { // Mocks a browser environment for testing.
    environment: 'jsdom',
    globals: true, // Allows using global variables like describe, it, expect without importing them.
    setupFiles: ['./vitest-setup.js'], // A file to set up testing configurations
  },
})