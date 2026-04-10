import { createPost } from '@/api/post';
import { QUERY_KEYS } from '@/lib/constants';
import type { UseMutationCallback } from '@/types';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useCreatePost(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createPost,
        onSuccess: () => {
            queryClient.resetQueries({ queryKey: QUERY_KEYS.post.list });
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
