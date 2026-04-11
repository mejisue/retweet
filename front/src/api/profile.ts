import apiClient from './client';

export type ProfileResponse = {
    memberId: number;
    nickname: string;
    avatarUrl: string | null;
    bio: string | null;
};

export async function fetchMyProfile(): Promise<ProfileResponse> {
    const { data } = await apiClient.get('/api/profiles/me');
    return data;
}
