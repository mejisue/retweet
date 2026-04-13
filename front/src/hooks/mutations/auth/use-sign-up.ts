import { signUp } from '@/api/auth';
import { useSetSession } from '@/store/session';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useSignUp(callbacks?: UseMutationCallback) {
    const setSession = useSetSession();

    return useMutation({
        mutationFn: signUp,
        onSuccess: (session) => {
            setSession(session);
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
