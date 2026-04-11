import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

type State = {
    isOpen: boolean;
};

const initialState: State = { isOpen: false };

const useProfileEditorModalStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                open: () => set({ isOpen: true }),
                close: () => set({ isOpen: false }),
            },
        })),
        { name: 'profileEditorModalStore' }
    )
);

export const useOpenProfileEditorModal = () =>
    useProfileEditorModalStore((s) => s.actions.open);

export const useProfileEditorModal = () => useProfileEditorModalStore();

// React 외부에서 store에 접근할 때 사용 (테스트 등)
export const getProfileEditorModalStore = () => useProfileEditorModalStore.getState();
