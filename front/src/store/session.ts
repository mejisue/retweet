import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';
import type { Session } from '@/types';

type State = {
    isLoaded: boolean;
    session: Session | null;
};

const initialState: State = {
    isLoaded: false,
    session: null,
};

const useSessionStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                setSession: (session: Session | null) => {
                    set({ session, isLoaded: true });
                },
                clearSession: () => {
                    set({ session: null, isLoaded: true });
                },
            },
        })),
        { name: 'sessionStore' }
    )
);

export const useSession = () => useSessionStore((s) => s.session);
export const useIsSessionLoaded = () => useSessionStore((s) => s.isLoaded);
export const useSetSession = () => useSessionStore((s) => s.actions.setSession);
export const useClearSession = () => useSessionStore((s) => s.actions.clearSession);

// 인터셉터 등 React 외부에서 store에 접근할 때 사용
export const getSessionStore = () => useSessionStore.getState();
