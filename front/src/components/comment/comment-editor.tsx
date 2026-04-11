import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { useCreateComment } from '@/hooks/mutations/comment/use-create-comment';
import { useUpdateComment } from '@/hooks/mutations/comment/use-update-comment';
import { useEffect, useState } from 'react';
import { toast } from 'sonner';

type CreateMode = {
    type: 'CREATE';
    postId: number;
};

type EditMode = {
    type: 'EDIT';
    commentId: number;
    initialContent: string;
    onClose: () => void;
};

type ReplyMode = {
    type: 'REPLY';
    postId: number;
    parentCommentId: number;
    rootCommentId: number;
    onClose: () => void;
};

type Props = CreateMode | EditMode | ReplyMode;

export default function CommentEditor(props: Props) {
    const [content, setContent] = useState('');

    const { mutate: createComment, isPending: isCreatePending } = useCreateComment({
        onSuccess: () => {
            setContent('');
            if (props.type === 'REPLY') props.onClose();
        },
        onError: () => {
            toast.error('댓글 추가에 실패했습니다.', { position: 'top-center' });
        },
    });

    const { mutate: updateComment, isPending: isUpdatePending } = useUpdateComment({
        onSuccess: () => {
            (props as EditMode).onClose();
        },
        onError: () => {
            toast.error('댓글 수정에 실패했습니다.', { position: 'top-center' });
        },
    });

    useEffect(() => {
        if (props.type === 'EDIT') {
            setContent(props.initialContent);
        }
    }, []);

    const handleSubmit = () => {
        if (content.trim() === '') return;

        if (props.type === 'CREATE') {
            createComment({ postId: props.postId, content });
        } else if (props.type === 'REPLY') {
            createComment({
                postId: props.postId,
                content,
                parentCommentId: props.parentCommentId,
                rootCommentId: props.rootCommentId,
            });
        } else {
            updateComment({ id: props.commentId, content });
        }
    };

    const isPending = isCreatePending || isUpdatePending;

    return (
        <div className="flex flex-col gap-2">
            <Textarea
                disabled={isPending}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="댓글을 입력하세요"
            />
            <div className="flex justify-end gap-2">
                {(props.type === 'EDIT' || props.type === 'REPLY') && (
                    <Button variant="outline" onClick={() => props.onClose()}>
                        취소
                    </Button>
                )}
                <Button onClick={handleSubmit} disabled={isPending || content.trim() === ''}>
                    작성
                </Button>
            </div>
        </div>
    );
}
