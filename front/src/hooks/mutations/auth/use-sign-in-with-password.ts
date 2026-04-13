import { signIn } from '@/api/auth';
import { useSetSession } from '@/store/session';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useSignInWithPassword(callbacks?: UseMutationCallback) {
    const setSession = useSetSession();

    return useMutation({
        mutationFn: signIn,
        onSuccess: (session) => {
            setSession(session);
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
