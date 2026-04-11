import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ThemeButton from './theme-button';
import { getThemeStore } from '@/store/theme';

beforeEach(() => {
    document.documentElement.classList.remove('light', 'dark');
    getThemeStore().actions.setTheme('light');
});

describe('ThemeButton', () => {
    it('light 테마일 때 트리거 버튼이 렌더링된다', () => {
        render(<ThemeButton />);

        expect(document.querySelector('[data-slot="popover-trigger"]')).toBeInTheDocument();
    });

    it('dark 테마일 때 트리거 버튼이 렌더링된다', () => {
        getThemeStore().actions.setTheme('dark');
        render(<ThemeButton />);

        expect(document.querySelector('[data-slot="popover-trigger"]')).toBeInTheDocument();
    });

    it('트리거 클릭 시 light, dark 옵션이 표시된다', async () => {
        const user = userEvent.setup();
        render(<ThemeButton />);

        await user.click(document.querySelector('[data-slot="popover-trigger"]')!);

        expect(await screen.findByText('light')).toBeInTheDocument();
        expect(await screen.findByText('dark')).toBeInTheDocument();
    });

    it('현재 테마(light)에 체크 아이콘이 표시된다', async () => {
        const user = userEvent.setup();
        render(<ThemeButton />);

        await user.click(document.querySelector('[data-slot="popover-trigger"]')!);
        await screen.findByText('light');

        const lightItem = screen.getByText('light').closest('div')!;
        expect(lightItem.querySelector('svg')).toBeInTheDocument();
    });

    it('dark 옵션 클릭 시 theme이 "dark"로 변경된다', async () => {
        const user = userEvent.setup();
        render(<ThemeButton />);

        await user.click(document.querySelector('[data-slot="popover-trigger"]')!);
        await user.click(await screen.findByText('dark'));

        expect(getThemeStore().theme).toBe('dark');
    });

    it('dark 옵션 클릭 시 <html>에 dark 클래스가 적용된다', async () => {
        const user = userEvent.setup();
        render(<ThemeButton />);

        await user.click(document.querySelector('[data-slot="popover-trigger"]')!);
        await user.click(await screen.findByText('dark'));

        expect(document.documentElement.classList.contains('dark')).toBe(true);
        expect(document.documentElement.classList.contains('light')).toBe(false);
    });
});
