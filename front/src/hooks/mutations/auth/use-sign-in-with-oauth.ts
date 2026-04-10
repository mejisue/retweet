import { redirectToGithubOAuth } from '@/api/auth';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useSignInWithOAuth(callbacks?: UseMutationCallback) {
    return useMutation({
        mutationFn: async () => redirectToGithubOAuth(),
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
