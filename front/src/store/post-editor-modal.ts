import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

type CreateMode = {
    isOpen: true;
    type: 'CREATE';
};

type EditMode = {
    isOpen: true;
    type: 'EDIT';
    postId: number;
    content: string;
    imageUrls: string[];
};

type CloseState = {
    isOpen: false;
};

type State = CreateMode | EditMode | CloseState;

const initialState: State = { isOpen: false };

const usePostEditorModalStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                openCreate: () => set({ isOpen: true, type: 'CREATE' }),
                openEdit: (param: Omit<EditMode, 'isOpen' | 'type'>) =>
                    set({ isOpen: true, type: 'EDIT', ...param }),
                close: () => set({ isOpen: false }),
            },
        })),
        { name: 'postEditorModalStore' }
    )
);

export const useOpenCreatePostModal = () =>
    usePostEditorModalStore((s) => s.actions.openCreate);

export const useOpenEditPostModal = () =>
    usePostEditorModalStore((s) => s.actions.openEdit);

export const usePostEditorModal = () => {
    const store = usePostEditorModalStore();
    return store as typeof store & State;
};
