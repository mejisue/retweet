import { createPostWithImages } from '@/api/post';
import { AMPLITUDE_EVENTS, trackEvent } from '@/lib/analytics';
import { QUERY_KEYS } from '@/lib/constants';
import type { UseMutationCallback } from '@/types';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useCreatePost(callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createPostWithImages,
        onSuccess: () => {
            queryClient.resetQueries({ queryKey: QUERY_KEYS.post.list });
            trackEvent(AMPLITUDE_EVENTS.POST_CREATED);
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
