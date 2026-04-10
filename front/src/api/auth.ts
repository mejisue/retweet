import axios from 'axios';
import apiClient from './client';
import type { Session } from '@/types';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function toSession(data: { accessToken: string; memberId: number; email: string; provider: string }): Session {
    return {
        accessToken: data.accessToken,
        member: {
            memberId: data.memberId,
            email: data.email,
            provider: data.provider as Session['member']['provider'],
        },
    };
}

export async function signUp({ email, password }: { email: string; password: string }): Promise<Session> {
    const { data } = await apiClient.post('/api/auth/signup', { email, password });
    return toSession(data);
}

export async function signIn({ email, password }: { email: string; password: string }): Promise<Session> {
    const { data } = await apiClient.post('/api/auth/login', { email, password });
    return toSession(data);
}

export async function authWithGithub(code: string): Promise<Session> {
    const { data } = await apiClient.post('/api/auth/github', { code });
    return toSession(data);
}

// 앱 시작 시 httpOnly 쿠키로 세션 복구
export async function refreshSession(): Promise<Session> {
    const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, {}, { withCredentials: true });
    return toSession(data);
}

export async function signOut(): Promise<void> {
    await apiClient.post('/api/auth/logout');
}

export async function requestPasswordResetEmail(email: string): Promise<void> {
    await apiClient.post('/api/auth/forgot-password', { email });
}

export async function resetPassword({ token, newPassword }: { token: string; newPassword: string }): Promise<void> {
    await apiClient.post('/api/auth/reset-password', { token, newPassword });
}

// GitHub OAuth URL 생성 후 리다이렉트
export function redirectToGithubOAuth(): void {
    const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID;
    const redirectUri = encodeURIComponent(`${import.meta.env.VITE_PUBLIC_URL}/auth/github/callback`);
    window.location.href = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email`;
}
