import axios from 'axios';

let authToken = null;
let onUnauthorizedCallback = null;

export const setAuthToken = (token) => {
  authToken = token || null;
};

export const setOnUnauthorized = (callback) => {
  onUnauthorizedCallback = callback;
};

const rawBase = import.meta.env.VITE_API_BASE_URL !== undefined
  ? import.meta.env.VITE_API_BASE_URL
  : 'http://localhost:8080';
const normalizedBase = rawBase.endsWith('/api/v1')
  ? rawBase
  : `${rawBase.replace(/\/+$/, '')}/api/v1`;

const client = axios.create({
  baseURL: normalizedBase,
  headers: {
    'Content-Type': 'application/json',
  },
});

client.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response.data;
    }
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    const status = error.response ? error.response.status : (error.status || 500);
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred.';

    const normalizedError = new Error(message);
    normalizedError.status = status;
    normalizedError.data = error.response?.data || null;

    const isLoginEndpoint = error.config?.url?.includes('/auth/login');
    if (status === 401 && !isLoginEndpoint && typeof onUnauthorizedCallback === 'function') {
      onUnauthorizedCallback();
    }

    return Promise.reject(normalizedError);
  }
);

export const register = (payload) => client.post('/auth/register', payload);

export const login = (payload) => client.post('/auth/login', payload);

export const uploadResume = (formData) =>
  client.post('/resumes/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

/**
 * @typedef {Object} AnalysisResponse
 * @property {string} id - Analysis report UUID
 * @property {string} fileName - Original file name
 * @property {string} analyzedAt - Timestamp of analysis
 * @property {string|null} targetRole - Target role hint
 * @property {number|null} experienceYears - Candidate experience in years
 * @property {number} overallScore - Capped overall score
 * @property {string} statusLabel - Qualitative score tier label
 * @property {number} uncappedScore - Plain weighted sum of six dimension scores
 * @property {boolean} scoreCapped - True if overall score was capped by job fit
 * @property {number} atsScore - ATS dimension score
 * @property {number} skillsScore - Skills dimension score
 * @property {number} impactScore - Impact dimension score
 * @property {number} projectScore - Project dimension score
 * @property {number} writingScore - Writing dimension score
 * @property {number} professionalismScore - Professionalism dimension score
 * @property {number} keywordMatchPercent - Percentage of keywords matched
 * @property {Array<Object>} sectionScores - Dimension breakdown
 * @property {Array<Object>} suggestions - Actionable suggestions
 * @property {Array<Object>} risks - Scoring risks
 * @property {string} improvementPrompt - Formatted fix prompt
 */

/**
 * Run analysis on an uploaded resume against a job description.
 * @param {Object} payload
 * @returns {Promise<AnalysisResponse>}
 */
export const runAnalysis = (payload) => client.post('/analysis/match', payload);

/**
 * Retrieve analysis result by report ID.
 * @param {string} id - Analysis report UUID
 * @returns {Promise<AnalysisResponse>}
 */
export const getAnalysis = (id) => client.get(`/analysis/${id}`);

export const getFixPrompt = (id, templateStyle = 'Simple Single-Column') =>
  client.get(`/analysis/${id}/fix-prompt`, {
    params: { templateStyle },
  });

export const downloadReport = (id) =>
  client.get(`/analysis/${id}/report`, {
    responseType: 'blob',
  });

export const getHistory = () => client.get('/history');
export const deleteHistoryItem = (id) => client.delete(`/history/${id}`);

export default client;
