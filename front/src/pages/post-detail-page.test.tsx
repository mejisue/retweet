import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import PostDetailPage from './post-detail-page';

vi.mock('@/components/post/post-item', () => ({
    default: ({ postId, type }: { postId: number; type: string }) => (
        <div data-testid="post-item" data-post-id={postId} data-type={type}>
            포스트 아이템
        </div>
    ),
}));

vi.mock('@/components/comment/comment-editor', () => ({
    default: () => <div data-testid="comment-editor" />,
}));

vi.mock('@/components/comment/comment-list', () => ({
    default: () => <div data-testid="comment-list" />,
}));

const renderPage = (postId = '1') =>
    render(
        <MemoryRouter initialEntries={[`/post/${postId}`]}>
            <Routes>
                <Route path="/post/:postId" element={<PostDetailPage />} />
                <Route path="/" element={<div>홈</div>} />
            </Routes>
        </MemoryRouter>
    );

describe('PostDetailPage', () => {
    it('postId에 해당하는 PostItem을 DETAIL 타입으로 렌더링한다', () => {
        renderPage('42');

        const postItem = screen.getByTestId('post-item');
        expect(postItem).toBeInTheDocument();
        expect(postItem).toHaveAttribute('data-post-id', '42');
        expect(postItem).toHaveAttribute('data-type', 'DETAIL');
    });

    it('postId가 없으면 홈으로 리다이렉트된다', () => {
        render(
            <MemoryRouter initialEntries={['/post/']}>
                <Routes>
                    <Route path="/post/" element={<PostDetailPage />} />
                    <Route path="/" element={<div>홈</div>} />
                </Routes>
            </MemoryRouter>
        );
        expect(screen.getByText('홈')).toBeInTheDocument();
    });
});
