import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { useCreatePost } from '@/hooks/mutations/post/use-create-post';
import { useUpdatePost } from '@/hooks/mutations/post/use-update-post';
import { usePostEditorModal } from '@/store/post-editor-modal';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

export default function PostEditorModal() {
    const postEditorModal = usePostEditorModal();
    const [content, setContent] = useState('');
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    useEffect(() => {
        if (!postEditorModal.isOpen) return;

        if (postEditorModal.type === 'CREATE') {
            setContent('');
        } else {
            setContent(postEditorModal.content);
        }
        textareaRef.current?.focus();
    }, [postEditorModal.isOpen]);

    const { mutate: createPost, isPending: isCreating } = useCreatePost({
        onSuccess: () => postEditorModal.actions.close(),
        onError: () => toast.error('포스트 작성에 실패했습니다.', { position: 'top-center' }),
    });

    const { mutate: updatePost, isPending: isUpdating } = useUpdatePost({
        onSuccess: () => postEditorModal.actions.close(),
        onError: () => toast.error('포스트 수정에 실패했습니다.', { position: 'top-center' }),
    });

    const isPending = isCreating || isUpdating;

    const handleSave = () => {
        if (content.trim() === '' || !postEditorModal.isOpen) return;

        if (postEditorModal.type === 'CREATE') {
            createPost({ content: content.trim(), imageUrls: [] });
        } else {
            updatePost({ postId: postEditorModal.postId, content: content.trim(), imageUrls: [] });
        }
    };

    return (
        <Dialog open={postEditorModal.isOpen} onOpenChange={postEditorModal.actions.close}>
            <DialogContent>
                <DialogTitle>
                    {postEditorModal.isOpen && postEditorModal.type === 'CREATE' ? '포스트 작성' : '포스트 수정'}
                </DialogTitle>
                <Textarea
                    ref={textareaRef}
                    disabled={isPending}
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    className="min-h-32 resize-none focus-visible:ring-0"
                    placeholder="무슨 일이 있었나요?"
                />
                <Button disabled={isPending || content.trim() === ''} onClick={handleSave}>
                    저장
                </Button>
            </DialogContent>
        </Dialog>
    );
}
