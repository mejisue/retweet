import defaultAvatar from '@/assets/default-avatar.png';
import CommentEditor from '@/components/comment/comment-editor';
import { useDeleteComment } from '@/hooks/mutations/comment/use-delete-comment';
import { formatTimeAgo } from '@/lib/time';
import { useOpenAlertModal } from '@/store/alert-modal';
import { useSession } from '@/store/session';
import type { NestedComment } from '@/types';
import { useState } from 'react';
import { toast } from 'sonner';

export default function CommentItem(props: NestedComment) {
    const session = useSession();
    const openAlertModal = useOpenAlertModal();

    const [isEditing, setIsEditing] = useState(false);
    const [isReplying, setIsReplying] = useState(false);

    const { mutate: deleteComment } = useDeleteComment({
        onError: () => {
            toast.error('댓글 삭제에 실패했습니다.', { position: 'top-center' });
        },
    });

    const handleDeleteClick = () => {
        openAlertModal({
            title: '댓글 삭제',
            description: '삭제된 댓글은 되돌릴 수 없습니다. 정말 삭제하시겠습니까?',
            onPositive: () => deleteComment({ id: props.id, postId: props.postId }),
        });
    };

    const isMine = session?.member.memberId === props.author.memberId;
    const isRootComment = props.parentComment === undefined;
    const isOverTwoLevels = props.parentCommentId !== props.rootCommentId;

    return (
        <div className={`flex flex-col gap-8 pb-5 ${isRootComment ? 'border-b' : 'ml-6'}`}>
            <div className="flex items-start gap-4">
                <img
                    className="h-10 w-10 rounded-full object-cover"
                    src={props.author.avatarUrl ?? defaultAvatar}
                    alt={props.author.nickname}
                />
                <div className="flex w-full flex-col gap-2">
                    <div className="font-bold">{props.author.nickname}</div>
                    {isEditing ? (
                        <CommentEditor
                            type="EDIT"
                            commentId={props.id}
                            initialContent={props.content}
                            onClose={() => setIsEditing(false)}
                        />
                    ) : (
                        <div>
                            {isOverTwoLevels && (
                                <span className="font-bold text-blue-500">
                                    @{props.parentComment?.author.nickname}&nbsp;
                                </span>
                            )}
                            {props.content}
                        </div>
                    )}
                    <div className="text-muted-foreground flex justify-between text-sm">
                        <div className="flex items-center gap-2">
                            <div
                                onClick={() => setIsReplying(!isReplying)}
                                className="cursor-pointer hover:underline"
                            >
                                댓글
                            </div>
                            <div className="bg-border h-[13px] w-[2px]" />
                            <div>{formatTimeAgo(props.createdAt)}</div>
                        </div>
                        {isMine && (
                            <div className="flex items-center gap-2">
                                <div
                                    onClick={() => setIsEditing(!isEditing)}
                                    className="cursor-pointer hover:underline"
                                >
                                    수정
                                </div>
                                <div className="bg-border h-[13px] w-[2px]" />
                                <div onClick={handleDeleteClick} className="cursor-pointer hover:underline">
                                    삭제
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
            {isReplying && (
                <CommentEditor
                    type="REPLY"
                    postId={props.postId}
                    parentCommentId={props.id}
                    rootCommentId={props.rootCommentId ?? props.id}
                    onClose={() => setIsReplying(false)}
                />
            )}
            {props.children.map((child) => (
                <CommentItem key={child.id} {...child} />
            ))}
        </div>
    );
}
