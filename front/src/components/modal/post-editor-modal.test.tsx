import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import React from 'react';
import PostEditorModal from './post-editor-modal';
import { useOpenCreatePostModal, useOpenEditPostModal } from '@/store/post-editor-modal';

// ────────────────────────────────────────────
// Mocks
// ────────────────────────────────────────────

const createPostMutateMock = vi.fn();
const updatePostMutateMock = vi.fn();
const alertOpenMock = vi.fn();

vi.mock('@/hooks/mutations/post/use-create-post', () => ({
    useCreatePost: ({ onSuccess }: { onSuccess?: () => void }) => ({
        mutate: createPostMutateMock.mockImplementation(() => onSuccess?.()),
        isPending: false,
    }),
}));

vi.mock('@/hooks/mutations/post/use-update-post', () => ({
    useUpdatePost: ({ onSuccess }: { onSuccess?: () => void }) => ({
        mutate: updatePostMutateMock.mockImplementation(() => onSuccess?.()),
        isPending: false,
    }),
}));

vi.mock('@/components/ui/carousel', () => ({
    Carousel: ({ children }: { children: React.ReactNode }) => <div data-testid="carousel">{children}</div>,
    CarouselContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    CarouselItem: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock('@/store/alert-modal', () => ({
    useAlertModal: () => ({
        isOpen: false,
        actions: { open: alertOpenMock, close: vi.fn() },
    }),
    useOpenAlertModal: () => alertOpenMock,
}));

vi.mock('sonner', () => ({ toast: { error: vi.fn() } }));

// ────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────

beforeEach(() => {
    createPostMutateMock.mockClear();
    updatePostMutateMock.mockClear();
    alertOpenMock.mockClear();

    vi.stubGlobal('URL', {
        createObjectURL: vi.fn((file: File) => `blob:mock-${file.name}`),
        revokeObjectURL: vi.fn(),
    });
});

/** 모달을 CREATE 모드로 열어주는 래퍼 */
function CreateModeWrapper() {
    const openCreate = useOpenCreatePostModal();
    useEffect(() => { openCreate(); }, []);
    return <PostEditorModal />;
}

/** 모달을 EDIT 모드로 열어주는 래퍼 */
function EditModeWrapper({
    postId = 1,
    content = '기존 내용',
    imageUrls = [] as string[],
} = {}) {
    const openEdit = useOpenEditPostModal();
    useEffect(() => { openEdit({ postId, content, imageUrls }); }, []);
    return <PostEditorModal />;
}

// ────────────────────────────────────────────
// Tests — CREATE 모드
// ────────────────────────────────────────────

describe('PostEditorModal - CREATE 모드', () => {
    it('"이미지 추가" 버튼이 렌더링된다', async () => {
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');
        expect(screen.getByRole('button', { name: /이미지 추가/ })).toBeInTheDocument();
    });

    it('파일 선택 시 이미지 미리보기가 표시된다', async () => {
        const user = userEvent.setup();
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        const file = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        await user.upload(fileInput, file);

        expect(screen.getAllByRole('img').length).toBeGreaterThan(0);
    });

    it('이미지 X 버튼 클릭 시 미리보기가 제거된다', async () => {
        const user = userEvent.setup();
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        const file = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        await user.upload(fileInput, file);
        expect(screen.getAllByRole('img').length).toBeGreaterThan(0);

        const removeBtn = document.querySelector('[class*="cursor-pointer rounded-full"]') as HTMLElement;
        await user.click(removeBtn);

        expect(screen.queryByRole('img')).not.toBeInTheDocument();
        expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-photo.jpg');
    });

    it('내용과 이미지를 입력하고 저장하면 createPost가 올바른 인자로 호출된다', async () => {
        const user = userEvent.setup();
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        await user.type(screen.getByPlaceholderText('무슨 일이 있었나요?'), '새 게시글');

        const file = new File(['img'], 'photo.jpg', { type: 'image/jpeg' });
        const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
        await user.upload(fileInput, file);

        await user.click(screen.getByRole('button', { name: '저장' }));

        expect(createPostMutateMock).toHaveBeenCalledWith(
            expect.objectContaining({
                content: '새 게시글',
                images: expect.arrayContaining([expect.any(File)]),
            })
        );
    });

    it('이미지 없이 저장하면 images가 빈 배열로 createPost가 호출된다', async () => {
        const user = userEvent.setup();
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        await user.type(screen.getByPlaceholderText('무슨 일이 있었나요?'), '이미지 없는 게시글');
        await user.click(screen.getByRole('button', { name: '저장' }));

        expect(createPostMutateMock).toHaveBeenCalledWith(
            expect.objectContaining({ content: '이미지 없는 게시글', images: [] })
        );
    });

    it('내용이 비어있으면 저장 버튼이 비활성화된다', async () => {
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();
    });

    it('내용이 있을 때 모달을 닫으면 경고 알림이 표시된다', async () => {
        const user = userEvent.setup();
        render(<CreateModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        await user.type(screen.getByPlaceholderText('무슨 일이 있었나요?'), '작성 중');
        await user.keyboard('{Escape}');

        await waitFor(() => {
            expect(alertOpenMock).toHaveBeenCalledWith(
                expect.objectContaining({ title: expect.stringContaining('마무리') })
            );
        });
    });
});

// ────────────────────────────────────────────
// Tests — EDIT 모드
// ────────────────────────────────────────────

describe('PostEditorModal - EDIT 모드', () => {
    it('기존 이미지가 표시된다', async () => {
        render(
            <EditModeWrapper
                imageUrls={[
                    'https://cdn.cloudfront.net/posts/1/a.jpg',
                    'https://cdn.cloudfront.net/posts/1/b.png',
                ]}
            />
        );
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        const images = screen.getAllByRole('img');
        expect(images).toHaveLength(2);
        expect(images[0]).toHaveAttribute('src', 'https://cdn.cloudfront.net/posts/1/a.jpg');
    });

    it('"이미지 추가" 버튼이 없다', async () => {
        render(<EditModeWrapper />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        expect(screen.queryByRole('button', { name: /이미지 추가/ })).not.toBeInTheDocument();
    });

    it('저장 시 기존 imageUrls를 포함해 updatePost가 호출된다', async () => {
        const user = userEvent.setup();
        const existingUrls = ['https://cdn.cloudfront.net/posts/1/a.jpg'];
        render(<EditModeWrapper postId={1} content="기존 내용" imageUrls={existingUrls} />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        const textarea = screen.getByPlaceholderText('무슨 일이 있었나요?');
        await user.clear(textarea);
        await user.type(textarea, '수정된 내용');
        await user.click(screen.getByRole('button', { name: '저장' }));

        expect(updatePostMutateMock).toHaveBeenCalledWith(
            expect.objectContaining({
                postId: 1,
                content: '수정된 내용',
                imageUrls: existingUrls,
            })
        );
    });

    it('이미지 없이 저장 시 빈 imageUrls로 updatePost가 호출된다', async () => {
        const user = userEvent.setup();
        render(<EditModeWrapper postId={2} content="내용" imageUrls={[]} />);
        await screen.findByPlaceholderText('무슨 일이 있었나요?');

        const textarea = screen.getByPlaceholderText('무슨 일이 있었나요?');
        await user.clear(textarea);
        await user.type(textarea, '수정된 내용');
        await user.click(screen.getByRole('button', { name: '저장' }));

        expect(updatePostMutateMock).toHaveBeenCalledWith(
            expect.objectContaining({ postId: 2, imageUrls: [] })
        );
    });
});
