import { signOut } from '@/api/auth';
import { useClearSession } from '@/store/session';
import type { UseMutationCallback } from '@/types';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useSignOut(callbacks?: UseMutationCallback) {
    const clearSession = useClearSession();
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: signOut,
        onSuccess: () => {
            clearSession();
            queryClient.clear();
            callbacks?.onSuccess?.();
        },
        onError: () => {
            // 서버 요청 실패해도 클라이언트 세션은 초기화
            clearSession();
            queryClient.clear();
        },
    });
}
