import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import CommentList from './comment-list';
import type { Comment } from '@/types';

// ────────────────────────────────────────────
// Mocks
// ────────────────────────────────────────────

vi.mock('@/hooks/queries/use-comments-data', () => ({
    useCommentsData: vi.fn(),
}));

vi.mock('@/components/comment/comment-item', () => ({
    default: (props: { id: number; content: string; children: unknown[] }) => (
        <div
            data-testid="comment-item"
            data-id={String(props.id)}
            data-children-count={String(props.children.length)}
        >
            {props.content}
        </div>
    ),
}));

import { useCommentsData } from '@/hooks/queries/use-comments-data';

// ────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────

const mockComment = (overrides: Partial<Comment> = {}): Comment => ({
    id: 1,
    postId: 1,
    content: '댓글',
    author: { memberId: 1, nickname: '작성자', avatarUrl: null },
    parentCommentId: null,
    rootCommentId: null,
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
    ...overrides,
});

beforeEach(() => {
    vi.mocked(useCommentsData).mockReset();
});

// ────────────────────────────────────────────
// Tests
// ────────────────────────────────────────────

describe('CommentList', () => {
    it('로딩 중에는 스켈레톤을 렌더링한다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: true, error: null, data: undefined,
        } as any);

        const { container } = render(<CommentList postId={1} />);

        expect(container.querySelector('.animate-pulse')).toBeInTheDocument();
    });

    it('에러 발생 시 아무것도 렌더링하지 않는다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: false, error: new Error('fail'), data: undefined,
        } as any);

        const { container } = render(<CommentList postId={1} />);

        expect(container.firstChild).toBeNull();
    });

    it('최상위 댓글 목록을 렌더링한다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: false, error: null,
            data: [
                mockComment({ id: 1, content: '첫 번째 댓글' }),
                mockComment({ id: 2, content: '두 번째 댓글' }),
            ],
        } as any);

        render(<CommentList postId={1} />);

        const items = screen.getAllByTestId('comment-item');
        expect(items).toHaveLength(2);
        expect(items[0]).toHaveTextContent('첫 번째 댓글');
        expect(items[1]).toHaveTextContent('두 번째 댓글');
    });

    it('댓글이 없으면 아무것도 렌더링하지 않는다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: false, error: null, data: [],
        } as any);

        render(<CommentList postId={1} />);

        expect(screen.queryByTestId('comment-item')).not.toBeInTheDocument();
    });

    it('대댓글은 최상위 댓글의 children에 포함되어 별도 항목으로 렌더링되지 않는다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: false, error: null,
            data: [
                mockComment({ id: 1, content: '최상위 댓글', rootCommentId: null, parentCommentId: null }),
                mockComment({ id: 2, content: '대댓글', rootCommentId: 1, parentCommentId: 1 }),
            ],
        } as any);

        render(<CommentList postId={1} />);

        // toNestedComments 결과로 최상위 1개만 렌더링, 대댓글은 children으로 전달
        const items = screen.getAllByTestId('comment-item');
        expect(items).toHaveLength(1);
        expect(items[0]).toHaveAttribute('data-children-count', '1');
    });

    it('여러 최상위 댓글에 각각 대댓글이 있으면 children이 올바르게 분배된다', () => {
        vi.mocked(useCommentsData).mockReturnValue({
            isPending: false, error: null,
            data: [
                mockComment({ id: 1, content: '최상위1', rootCommentId: null, parentCommentId: null }),
                mockComment({ id: 2, content: '최상위2', rootCommentId: null, parentCommentId: null }),
                mockComment({ id: 3, content: '대댓글(1의 자식)', rootCommentId: 1, parentCommentId: 1 }),
                mockComment({ id: 4, content: '대댓글(2의 자식)', rootCommentId: 2, parentCommentId: 2 }),
            ],
        } as any);

        render(<CommentList postId={1} />);

        const items = screen.getAllByTestId('comment-item');
        expect(items).toHaveLength(2);
        expect(items[0]).toHaveAttribute('data-children-count', '1');
        expect(items[1]).toHaveAttribute('data-children-count', '1');
    });
});
