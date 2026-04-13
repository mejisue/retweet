import apiClient from './client';
import { uploadImages } from './image';

export type ProfileResponse = {
    memberId: number;
    nickname: string;
    avatarUrl: string | null;
    bio: string | null;
};

export type ProfileUpdateRequest = {
    nickname: string;
    bio: string;
    avatarFile?: File;
};

export async function fetchMyProfile(): Promise<ProfileResponse> {
    const { data } = await apiClient.get('/api/profiles/me');
    return data;
}

export async function fetchProfile(userId: number): Promise<ProfileResponse> {
    const { data } = await apiClient.get(`/api/profiles/${userId}`);
    return data;
}

export async function updateProfile({ nickname, bio, avatarFile }: ProfileUpdateRequest): Promise<ProfileResponse> {
    let avatarUrl: string | undefined;
    if (avatarFile) {
        const urls = await uploadImages([avatarFile]);
        avatarUrl = urls[0];
    }
    const { data } = await apiClient.put('/api/profiles/me', { nickname, avatarUrl, bio });
    return data;
}
