import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

type OpenState = {
    isOpen: true;
    title: string;
    description: string;
    onPositive?: () => void;
    onNegative?: () => void;
};

type CloseState = {
    isOpen: false;
};

type State = OpenState | CloseState;

const initialState = { isOpen: false } as State;

const useAlertModalStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                open: (params: Omit<OpenState, 'isOpen'>) => {
                    set({ ...params, isOpen: true });
                },
                close: () => {
                    set({ isOpen: false });
                },
            },
        })),
        { name: 'AlertModalStore' }
    )
);

export const useOpenAlertModal = () =>
    useAlertModalStore((store) => store.actions.open);

export const useAlertModal = () => {
    const store = useAlertModalStore();
    return store as typeof store & State;
};
