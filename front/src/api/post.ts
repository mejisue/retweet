import apiClient from './client';
import type { Post, PageSlice } from '@/types';
import { uploadImages } from './image';

export async function createPostWithImages({ content, images }: { content: string; images: File[] }): Promise<Post> {
    const imageUrls = images.length > 0 ? await uploadImages(images) : [];
    const { data } = await apiClient.post('/api/posts', { content, imageUrls });
    return data;
}

export async function fetchPosts(page: number): Promise<PageSlice<Post>> {
    const { data } = await apiClient.get('/api/posts', { params: { page, size: 10 } });
    return data;
}

export async function fetchPost(postId: number): Promise<Post> {
    const { data } = await apiClient.get(`/api/posts/${postId}`);
    return data;
}

export async function fetchMemberPosts(memberId: number, page: number): Promise<PageSlice<Post>> {
    const { data } = await apiClient.get(`/api/posts/member/${memberId}`, { params: { page, size: 10 } });
    return data;
}

export async function updatePost({ postId, content, imageUrls }: { postId: number; content: string; imageUrls: string[] }): Promise<Post> {
    const { data } = await apiClient.put(`/api/posts/${postId}`, { content, imageUrls });
    return data;
}

export async function deletePost(postId: number): Promise<void> {
    await apiClient.delete(`/api/posts/${postId}`);
}

export async function togglePostLike(postId: number): Promise<{ liked: boolean }> {
    const { data } = await apiClient.post(`/api/posts/${postId}/like`);
    return data;
}
