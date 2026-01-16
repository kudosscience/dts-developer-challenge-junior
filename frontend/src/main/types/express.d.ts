/* eslint-disable @typescript-eslint/no-unused-vars */
// Extend Express Request type to include CSRF token method
declare namespace Express {
  interface Request {
    csrfToken?: () => string;
  }
}
