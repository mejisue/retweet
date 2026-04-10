import { requestPasswordResetEmail } from '@/api/auth';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useRequestPasswordResetEmail(callbacks?: UseMutationCallback) {
    return useMutation({
        mutationFn: requestPasswordResetEmail,
        onSuccess: () => {
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
