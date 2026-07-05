import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to every request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401/403 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 ||
        error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth endpoints
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  signup: (data) => api.post('/auth/signup', data),
};

// Market data endpoints
export const marketAPI = {
  getAllPrices: () => api.get('/market/prices'),
  getPrice: (symbol) => api.get(`/market/price/${symbol}`),
};

// Challenge endpoints
export const challengeAPI = {
  getRules: () => api.get('/challenge/rules'),
  startChallenge: (data) => api.post('/challenge/start', data),
  getMyChallenges: () => api.get('/challenge/my-challenges'),
};

// Trade endpoints
export const tradeAPI = {
  openTrade: (data) => api.post('/trade/open', data),
  closeTrade: (tradeId) =>
      api.post(`/trade/close?tradeId=${tradeId}`),
  getOpenTrades: () => api.get('/trade/open-trades'),
  getAllTrades: () => api.get('/trade/all-trades'),
};

export default api;