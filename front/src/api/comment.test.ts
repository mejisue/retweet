import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchComments, createComment, updateComment, deleteComment } from './comment';

const { getMock, postMock, putMock, deleteMock } = vi.hoisted(() => ({
    getMock: vi.fn(),
    postMock: vi.fn(),
    putMock: vi.fn(),
    deleteMock: vi.fn(),
}));

vi.mock('./client', () => ({
    default: { get: getMock, post: postMock, put: putMock, delete: deleteMock },
}));

const mockComment = {
    id: 1,
    postId: 1,
    content: '댓글',
    author: { memberId: 1, nickname: '작성자', avatarUrl: null },
    parentCommentId: null,
    rootCommentId: null,
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
};

beforeEach(() => {
    getMock.mockClear();
    postMock.mockClear();
    putMock.mockClear();
    deleteMock.mockClear();
});

// ────────────────────────────────────────────
// fetchComments
// ────────────────────────────────────────────

describe('fetchComments', () => {
    it('GET /api/posts/:postId/comments 호출 후 댓글 목록을 반환한다', async () => {
        getMock.mockResolvedValue({ data: [mockComment] });

        const result = await fetchComments(1);

        expect(result).toEqual([mockComment]);
        expect(getMock).toHaveBeenCalledWith('/api/posts/1/comments');
    });

    it('빈 배열도 정상 반환된다', async () => {
        getMock.mockResolvedValue({ data: [] });

        const result = await fetchComments(1);

        expect(result).toEqual([]);
    });
});

// ────────────────────────────────────────────
// createComment
// ────────────────────────────────────────────

describe('createComment', () => {
    it('최상위 댓글 생성 시 올바른 엔드포인트로 POST 요청을 보낸다', async () => {
        postMock.mockResolvedValue({ data: mockComment });

        const result = await createComment({ postId: 1, content: '새 댓글' });

        expect(result).toEqual(mockComment);
        const [url, body] = postMock.mock.calls[0];
        expect(url).toBe('/api/posts/1/comments');
        expect(body).toMatchObject({ content: '새 댓글' });
    });

    it('대댓글 생성 시 parentCommentId와 rootCommentId를 함께 전송한다', async () => {
        const replyComment = { ...mockComment, id: 3, parentCommentId: 2, rootCommentId: 2 };
        postMock.mockResolvedValue({ data: replyComment });

        await createComment({ postId: 1, content: '대댓글', parentCommentId: 2, rootCommentId: 2 });

        const [, body] = postMock.mock.calls[0];
        expect(body.parentCommentId).toBe(2);
        expect(body.rootCommentId).toBe(2);
    });

    it('서버 에러 시 예외가 전파된다', async () => {
        postMock.mockRejectedValue(new Error('Network Error'));

        await expect(createComment({ postId: 1, content: '댓글' })).rejects.toThrow('Network Error');
    });
});

// ────────────────────────────────────────────
// updateComment
// ────────────────────────────────────────────

describe('updateComment', () => {
    it('PUT /api/comments/:id 호출 후 수정된 댓글을 반환한다', async () => {
        const updated = { ...mockComment, content: '수정된 댓글' };
        putMock.mockResolvedValue({ data: updated });

        const result = await updateComment({ id: 1, content: '수정된 댓글' });

        expect(result).toEqual(updated);
        expect(putMock).toHaveBeenCalledWith('/api/comments/1', { content: '수정된 댓글' });
    });
});

// ────────────────────────────────────────────
// deleteComment
// ────────────────────────────────────────────

describe('deleteComment', () => {
    it('DELETE /api/comments/:id 호출한다', async () => {
        deleteMock.mockResolvedValue({});

        await deleteComment({ id: 1, postId: 1 });

        expect(deleteMock).toHaveBeenCalledWith('/api/comments/1');
    });

    it('postId는 API 경로에 포함되지 않는다 (캐시 업데이트용)', async () => {
        deleteMock.mockResolvedValue({});

        await deleteComment({ id: 5, postId: 99 });

        expect(deleteMock).toHaveBeenCalledWith('/api/comments/5');
    });
});
