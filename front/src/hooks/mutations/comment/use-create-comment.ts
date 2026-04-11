import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createComment } from '@/api/comment';
import { QUERY_KEYS } from '@/lib/constants';
import type { Comment, UseMutationCallback } from '@/types';

export function useCreateComment(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createComment,
        onSuccess: (newComment) => {
            callbacks?.onSuccess?.();
            queryClient.setQueryData<Comment[]>(
                QUERY_KEYS.comment.post(newComment.postId),
                (comments) => [...(comments ?? []), newComment]
            );
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
