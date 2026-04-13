import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import React from 'react';
import CommentEditorModal from './comment-editor-modal';
import { useOpenCommentEditorModal } from '@/store/comment-editor-modal';

// ────────────────────────────────────────────
// Mocks
// ────────────────────────────────────────────

const createCommentMutateMock = vi.fn();
const alertOpenMock = vi.fn();

vi.mock('@/hooks/mutations/comment/use-create-comment', () => ({
    useCreateComment: ({ onSuccess }: { onSuccess?: () => void }) => ({
        mutate: createCommentMutateMock.mockImplementation(() => onSuccess?.()),
        isPending: false,
    }),
}));

vi.mock('@/hooks/queries/post/use-post', () => ({
    usePost: () => ({
        data: {
            id: 1,
            content: '테스트 게시글 내용',
            author: { memberId: 1, nickname: '게시글 작성자', avatarUrl: null },
            createdAt: '2026-01-01T00:00:00',
            imageUrls: [],
            likeCount: 0,
            updatedAt: '2026-01-01T00:00:00',
        },
    }),
}));

vi.mock('@/hooks/queries/use-my-profile', () => ({
    useMyProfile: () => ({ data: { memberId: 2, nickname: '나', avatarUrl: null } }),
}));

vi.mock('@/store/alert-modal', () => ({
    useAlertModal: () => ({
        isOpen: false,
        actions: { open: alertOpenMock, close: vi.fn() },
    }),
    useOpenAlertModal: () => alertOpenMock,
}));

vi.mock('@/lib/time', () => ({
    formatTimeAgo: () => '방금 전',
}));

vi.mock('sonner', () => ({ toast: { error: vi.fn() } }));

// ────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────

beforeEach(() => {
    createCommentMutateMock.mockClear();
    alertOpenMock.mockClear();
});

function OpenModalWrapper({ postId = 1 }: { postId?: number } = {}) {
    const open = useOpenCommentEditorModal();
    useEffect(() => { open(postId); }, []);
    return <CommentEditorModal />;
}

// ────────────────────────────────────────────
// Tests
// ────────────────────────────────────────────

describe('CommentEditorModal', () => {
    it('닫힌 상태에서는 렌더링되지 않는다', () => {
        render(<CommentEditorModal />);

        expect(screen.queryByText('댓글 작성')).not.toBeInTheDocument();
    });

    it('열리면 원글 내용과 댓글 입력창이 렌더링된다', async () => {
        render(<OpenModalWrapper />);
        await screen.findByText('댓글 작성');

        expect(screen.getByText('테스트 게시글 내용')).toBeInTheDocument();
        expect(screen.getByText('게시글 작성자')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('댓글을 입력하세요')).toBeInTheDocument();
    });

    it('내용이 없으면 작성 버튼이 비활성화된다', async () => {
        render(<OpenModalWrapper />);
        await screen.findByText('댓글 작성');

        expect(screen.getByRole('button', { name: '작성' })).toBeDisabled();
    });

    it('내용을 입력하면 작성 버튼이 활성화된다', async () => {
        const user = userEvent.setup();
        render(<OpenModalWrapper />);
        await screen.findByText('댓글 작성');

        await user.type(screen.getByPlaceholderText('댓글을 입력하세요'), '새 댓글');

        expect(screen.getByRole('button', { name: '작성' })).toBeEnabled();
    });

    it('작성 버튼 클릭 시 createComment가 올바른 postId와 content로 호출된다', async () => {
        const user = userEvent.setup();
        render(<OpenModalWrapper postId={3} />);
        await screen.findByText('댓글 작성');

        await user.type(screen.getByPlaceholderText('댓글을 입력하세요'), '새 댓글');
        await user.click(screen.getByRole('button', { name: '작성' }));

        expect(createCommentMutateMock).toHaveBeenCalledWith(
            expect.objectContaining({ postId: 3, content: '새 댓글' })
        );
    });

    it('내용이 있을 때 Escape로 닫으면 경고 알림이 표시된다', async () => {
        const user = userEvent.setup();
        render(<OpenModalWrapper />);
        await screen.findByText('댓글 작성');

        await user.type(screen.getByPlaceholderText('댓글을 입력하세요'), '작성 중');
        await user.keyboard('{Escape}');

        await waitFor(() => {
            expect(alertOpenMock).toHaveBeenCalledWith(
                expect.objectContaining({ title: expect.stringContaining('마무리') })
            );
        });
    });

    it('내용이 없을 때 Escape로 닫으면 경고 없이 닫힌다', async () => {
        const user = userEvent.setup();
        render(<OpenModalWrapper />);
        await screen.findByText('댓글 작성');

        await user.keyboard('{Escape}');

        await waitFor(() => {
            expect(alertOpenMock).not.toHaveBeenCalled();
        });
    });
});
