import { resetPassword } from '@/api/auth';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useResetPassword(callbacks?: UseMutationCallback) {
    return useMutation({
        mutationFn: resetPassword,
        onSuccess: () => {
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
