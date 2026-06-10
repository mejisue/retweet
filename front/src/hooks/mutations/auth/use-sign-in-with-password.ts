import { signIn } from '@/api/auth';
import { AMPLITUDE_EVENTS, identifyUser, trackEvent } from '@/lib/analytics';
import { useSetSession } from '@/store/session';
import type { UseMutationCallback } from '@/types';
import { useMutation } from '@tanstack/react-query';

export function useSignInWithPassword(callbacks?: UseMutationCallback) {
    const setSession = useSetSession();

    return useMutation({
        mutationFn: signIn,
        onSuccess: (session) => {
            setSession(session);
            identifyUser(session.member.memberId);
            trackEvent(AMPLITUDE_EVENTS.SIGN_IN, { method: 'password' });
            callbacks?.onSuccess?.();
        },
        onError: (error) => {
            callbacks?.onError?.(error);
        },
    });
}
