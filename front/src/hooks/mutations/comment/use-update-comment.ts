import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateComment } from '@/api/comment';
import { QUERY_KEYS } from '@/lib/constants';
import type { Comment, UseMutationCallback } from '@/types';

export function useUpdateComment(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateComment,
        onSuccess: (updatedComment) => {
            callbacks?.onSuccess?.();
            queryClient.setQueryData<Comment[]>(
                QUERY_KEYS.comment.post(updatedComment.postId),
                (comments) =>
                    comments?.map((c) => (c.id === updatedComment.id ? { ...c, ...updatedComment } : c)) ?? []
            );
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
