import axios from 'axios';
import { getSessionStore } from '@/store/session';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

// 토큰 갱신용 별도 인스턴스 (인터셉터 없음, 쿠키 포함)
const refreshClient = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
});

const apiClient = axios.create({
    baseURL: BASE_URL,
    withCredentials: true, // httpOnly refresh token 쿠키 자동 전송
});

// 요청 인터셉터: access token 주입
apiClient.interceptors.request.use((config) => {
    const { session } = getSessionStore();
    if (session?.accessToken) {
        config.headers.Authorization = `Bearer ${session.accessToken}`;
    }
    return config;
});

// 응답 인터셉터: 401 시 토큰 재발급 후 재시도
let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;

        if (error.response?.status !== 401 || original._retry) {
            return Promise.reject(error);
        }

        if (isRefreshing) {
            return new Promise((resolve) => {
                pendingRequests.push((token) => {
                    original.headers.Authorization = `Bearer ${token}`;
                    resolve(apiClient(original));
                });
            });
        }

        original._retry = true;
        isRefreshing = true;

        try {
            const { data } = await refreshClient.post('/api/auth/refresh');
            const newToken: string = data.accessToken;

            const { actions } = getSessionStore();
            actions.setSession({ accessToken: newToken, member: data });

            pendingRequests.forEach((cb) => cb(newToken));
            pendingRequests = [];

            original.headers.Authorization = `Bearer ${newToken}`;
            return apiClient(original);
        } catch {
            const { actions } = getSessionStore();
            actions.clearSession();
            return Promise.reject(error);
        } finally {
            isRefreshing = false;
        }
    }
);

export default apiClient;
