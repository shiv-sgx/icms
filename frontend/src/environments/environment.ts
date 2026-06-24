/**
 * Runtime configuration. The API base is a relative path so it works the same in
 * dev (Angular dev-server proxies /api -> http://localhost:3000) and in prod
 * (Angular dist served same-origin behind the Node API / reverse proxy).
 */
export const environment = {
  production: false,
  apiBaseUrl: '/api/v1',
};
