import { updatePost } from '@/api/post';
import { QUERY_KEYS } from '@/lib/constants';
import type { UseMutationCallback } from '@/types';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useUpdatePost(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updatePost,
        onSuccess: (updated) => {
            queryClient.resetQueries({ queryKey: QUERY_KEYS.post.list });
            queryClient.setQueryData(QUERY_KEYS.post.byId(updated.id), updated);
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
