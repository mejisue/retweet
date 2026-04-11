import apiClient from './client';
import type { Comment } from '@/types';

export async function fetchComments(postId: number): Promise<Comment[]> {
    const { data } = await apiClient.get(`/api/posts/${postId}/comments`);
    return data;
}

export async function createComment({
    postId,
    content,
    parentCommentId,
    rootCommentId,
}: {
    postId: number;
    content: string;
    parentCommentId?: number;
    rootCommentId?: number;
}): Promise<Comment> {
    const { data } = await apiClient.post(`/api/posts/${postId}/comments`, {
        content,
        parentCommentId,
        rootCommentId,
    });
    return data;
}

export async function updateComment({
    id,
    content,
}: {
    id: number;
    content: string;
}): Promise<Comment> {
    const { data } = await apiClient.put(`/api/comments/${id}`, { content });
    return data;
}

export async function deleteComment({ id }: { id: number; postId: number }): Promise<void> {
    await apiClient.delete(`/api/comments/${id}`);
}
