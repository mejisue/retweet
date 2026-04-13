import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteComment } from '@/api/comment';
import { QUERY_KEYS } from '@/lib/constants';
import type { Comment, UseMutationCallback } from '@/types';

export function useDeleteComment(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: deleteComment,
        onSuccess: (_, variables) => {
            callbacks?.onSuccess?.();
            queryClient.setQueryData<Comment[]>(
                QUERY_KEYS.comment.post(variables.postId),
                (comments) => comments?.filter((c) => c.id !== variables.id) ?? []
            );
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
