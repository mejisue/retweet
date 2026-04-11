import type { Theme } from '@/types';
import { create } from 'zustand';
import { combine, devtools, persist } from 'zustand/middleware';

type State = {
    theme: Theme;
};

const initialState: State = {
    theme: "light",
};

const useThemeStore = create(devtools(
    persist(combine(initialState, (set) => ({
        actions: {
            setTheme: (theme: Theme) => {
                const htmlTag = document.documentElement;
                htmlTag.classList.remove("dark", "light");
                htmlTag.classList.add(theme);
                set({ theme });
            },
        },
    })), {
        name: "ThemeStore",
        partialize: (store) => ({
            theme: store.theme,
        }),
    }),
    { name: "ThemeStore" }
));

export const useTheme = () => useThemeStore((store) => store.theme);

export const useSetTheme = () => useThemeStore((store) => store.actions.setTheme);

export const getThemeStore = () => useThemeStore.getState();
