import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

type OpenState = {
    isOpen: true;
    postId: number;
};

type CloseState = {
    isOpen: false;
};

type State = OpenState | CloseState;

const initialState: State = { isOpen: false };

const useCommentEditorModalStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                open: (postId: number) => set({ isOpen: true, postId }),
                close: () => set({ isOpen: false }),
            },
        })),
        { name: 'commentEditorModalStore' }
    )
);

export const useOpenCommentEditorModal = () =>
    useCommentEditorModalStore((s) => s.actions.open);

export const useCommentEditorModal = () => {
    const store = useCommentEditorModalStore();
    return store as typeof store & State;
};
