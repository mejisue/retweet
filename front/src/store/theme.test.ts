import { describe, it, expect, beforeEach } from 'vitest';
import { getThemeStore } from './theme';

beforeEach(() => {
    document.documentElement.classList.remove('light', 'dark');
    getThemeStore().actions.setTheme('light');
});

describe('themeStore', () => {
    it('초기 상태: theme="light"', () => {
        expect(getThemeStore().theme).toBe('light');
    });

    it('setTheme("dark") 호출 후 theme이 "dark"로 변경된다', () => {
        getThemeStore().actions.setTheme('dark');

        expect(getThemeStore().theme).toBe('dark');
    });

    it('setTheme("dark") 호출 시 <html>에 dark 클래스가 추가된다', () => {
        getThemeStore().actions.setTheme('dark');

        expect(document.documentElement.classList.contains('dark')).toBe(true);
        expect(document.documentElement.classList.contains('light')).toBe(false);
    });

    it('setTheme("light") 호출 시 <html>에 light 클래스가 추가된다', () => {
        getThemeStore().actions.setTheme('dark');
        getThemeStore().actions.setTheme('light');

        expect(document.documentElement.classList.contains('light')).toBe(true);
        expect(document.documentElement.classList.contains('dark')).toBe(false);
    });

    it('dark → light 전환 시 dark 클래스가 제거된다', () => {
        getThemeStore().actions.setTheme('dark');
        getThemeStore().actions.setTheme('light');

        expect(document.documentElement.classList.contains('dark')).toBe(false);
    });
});
