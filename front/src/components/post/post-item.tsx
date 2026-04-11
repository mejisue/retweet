import defaultAvatar from '@/assets/default-avatar.png';
import DeletePostButton from '@/components/post/delete-post-button';
import EditPostButton from '@/components/post/edit-post-button';
import LikePostButton from '@/components/post/like-post-button';
import { usePost } from '@/hooks/queries/post/use-post';
import { formatTimeAgo } from '@/lib/time';
import { useOpenCommentEditorModal } from '@/store/comment-editor-modal';
import { useSession } from '@/store/session';
import { MessageCircleIcon } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

type Props = {
    postId: number;
    type: 'FEED' | 'DETAIL';
};

export default function PostItem({ postId, type }: Props) {
    const navigate = useNavigate();
    const session = useSession();
    const openCommentEditor = useOpenCommentEditorModal();
    const { data: post, isPending, error } = usePost(postId);

    if (isPending) return <div className="h-32 animate-pulse rounded-xl bg-muted" />;
    if (error || !post) return null;

    const isMine = session?.member.memberId === post.author.memberId;

    return (
        <div className={`flex flex-col gap-4 pb-8 ${type === 'FEED' && 'border-b'}`}>
            {/* 작성자 정보 + 수정/삭제 */}
            <div className="flex justify-between">
                <div className="flex items-start gap-3">
                    <Link to={`/profile/${post.author.memberId}`}>
                        <img
                            src={post.author.avatarUrl ?? defaultAvatar}
                            alt={`${post.author.nickname}의 프로필`}
                            className="h-10 w-10 rounded-full object-cover"
                        />
                    </Link>
                    <div>
                        <Link to={`/profile/${post.author.memberId}`} className="font-bold hover:underline">
                            {post.author.nickname}
                        </Link>
                        <div className="text-muted-foreground text-sm">{formatTimeAgo(post.createdAt)}</div>
                    </div>
                </div>

                {isMine && (
                    <div className="flex text-muted-foreground">
                        <EditPostButton postId={post.id} content={post.content} imageUrls={post.imageUrls} />
                        <DeletePostButton
                            postId={post.id}
                            onDeleted={type === 'DETAIL' ? () => navigate('/') : undefined}
                        />
                    </div>
                )}
            </div>

            {/* 본문 */}
            {type === 'FEED' ? (
                <Link to={`/post/${post.id}`}>
                    <p className="line-clamp-3 break-words whitespace-pre-wrap">{post.content}</p>
                </Link>
            ) : (
                <p className="break-words whitespace-pre-wrap">{post.content}</p>
            )}

            {/* 이미지 */}
            {post.imageUrls.length > 0 && (
                <div className="flex gap-2 flex-wrap">
                    {post.imageUrls.map((url, i) => (
                        <img key={i} src={url} className="h-48 w-48 rounded-xl object-cover" alt="" />
                    ))}
                </div>
            )}

            {/* 좋아요 / 댓글 */}
            <div className="flex gap-2">
                <LikePostButton postId={post.id} likeCount={post.likeCount} />
                {type === 'FEED' && (
                    <button
                        onClick={() => openCommentEditor(post.id)}
                        className="hover:bg-muted flex cursor-pointer items-center gap-2 rounded-xl border p-2 px-4 text-sm"
                    >
                        <MessageCircleIcon className="h-4 w-4" />
                        <span>댓글 달기</span>
                    </button>
                )}
            </div>
        </div>
    );
}
