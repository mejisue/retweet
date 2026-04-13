import defaultAvatar from '@/assets/default-avatar.png';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { useCreateComment } from '@/hooks/mutations/comment/use-create-comment';
import { usePost } from '@/hooks/queries/post/use-post';
import { useMyProfile } from '@/hooks/queries/use-my-profile';
import { formatTimeAgo } from '@/lib/time';
import { useAlertModal } from '@/store/alert-modal';
import { useCommentEditorModal } from '@/store/comment-editor-modal';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

function PostPreview({ postId }: { postId: number }) {
    const { data: post } = usePost(postId);
    if (!post) return null;

    return (
        <div className="flex gap-3">
            <div className="flex flex-col items-center">
                <img
                    src={post.author.avatarUrl ?? defaultAvatar}
                    alt={post.author.nickname}
                    className="h-10 w-10 shrink-0 rounded-full object-cover"
                />
                <div className="mt-2 w-0.5 flex-1 bg-border" />
            </div>
            <div className="mb-4 flex flex-col gap-1 pb-2">
                <div className="flex items-center gap-2">
                    <span className="font-bold">{post.author.nickname}</span>
                    <span className="text-muted-foreground text-sm">{formatTimeAgo(post.createdAt)}</span>
                </div>
                <p className="line-clamp-3 break-words whitespace-pre-wrap text-sm">{post.content}</p>
            </div>
        </div>
    );
}

export default function CommentEditorModal() {
    const modal = useCommentEditorModal();
    const alertModal = useAlertModal();
    const { data: myProfile } = useMyProfile();

    const [content, setContent] = useState('');
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.style.height = 'auto';
            textareaRef.current.style.height = textareaRef.current.scrollHeight + 'px';
        }
    }, [content]);

    useEffect(() => {
        if (!modal.isOpen) return;
        setContent('');
        setTimeout(() => textareaRef.current?.focus(), 50);
    }, [modal.isOpen]);

    const { mutate: createComment, isPending } = useCreateComment({
        onSuccess: () => modal.actions.close(),
        onError: () => toast.error('댓글 작성에 실패했습니다.', { position: 'top-center' }),
    });

    const handleClose = () => {
        if (content.trim() !== '') {
            alertModal.actions.open({
                title: '댓글 작성이 마무리 되지 않았습니다.',
                description: '이 화면에서 나가면 작성 중이던 내용이 사라집니다.',
                onPositive: () => modal.actions.close(),
            });
            return;
        }
        modal.actions.close();
    };

    const handleSubmit = () => {
        if (content.trim() === '' || !modal.isOpen) return;
        createComment({ postId: modal.postId, content: content.trim() });
    };

    return (
        <Dialog open={modal.isOpen} onOpenChange={handleClose}>
            <DialogContent className="max-h-[90vh] gap-0 overflow-y-auto">
                <DialogTitle className="mb-4">댓글 작성</DialogTitle>

                {/* 원글 미리보기 */}
                {modal.isOpen && <PostPreview postId={modal.postId} />}

                {/* 댓글 입력 */}
                <div className="flex gap-3">
                    <img
                        src={myProfile?.avatarUrl ?? defaultAvatar}
                        alt="내 프로필"
                        className="h-10 w-10 shrink-0 rounded-full object-cover"
                    />
                    <Textarea
                        ref={textareaRef}
                        disabled={isPending}
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        className="min-h-[80px] resize-none border-none p-0 shadow-none focus-visible:ring-0"
                        placeholder="댓글을 입력하세요"
                    />
                </div>

                <div className="mt-4 flex justify-end">
                    <Button
                        disabled={isPending || content.trim() === ''}
                        onClick={handleSubmit}
                        className="cursor-pointer rounded-full px-6"
                    >
                        작성
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
}
