import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import IndexPage from './index-page';

const openCreateMock = vi.fn();

vi.mock('@/components/post/create-post-button', () => ({
    default: () => (
        <button onClick={openCreateMock}>포스트 작성</button>
    ),
}));

vi.mock('@/components/post/post-feed', () => ({
    default: () => <div data-testid="post-feed">피드</div>,
}));

const renderPage = () =>
    render(
        <MemoryRouter>
            <IndexPage />
        </MemoryRouter>
    );

beforeEach(() => {
    openCreateMock.mockClear();
});

describe('IndexPage', () => {
    it('포스트 작성 버튼이 렌더링된다', () => {
        renderPage();
        expect(screen.getByRole('button', { name: '포스트 작성' })).toBeInTheDocument();
    });

    it('PostFeed가 렌더링된다', () => {
        renderPage();
        expect(screen.getByTestId('post-feed')).toBeInTheDocument();
    });

    it('포스트 작성 버튼 클릭 시 모달 열기 함수가 호출된다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(screen.getByRole('button', { name: '포스트 작성' }));

        expect(openCreateMock).toHaveBeenCalledOnce();
    });
});
