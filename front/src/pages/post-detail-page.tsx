import CommentEditor from '@/components/comment/comment-editor';
import CommentList from '@/components/comment/comment-list';
import PostItem from '@/components/post/post-item';
import { usePost } from '@/hooks/queries/post/use-post';
import { Navigate, useParams } from 'react-router-dom';

export default function PostDetailPage() {
    const { postId } = useParams();

    if (!postId) return <Navigate to="/" />;

    return <PostDetailContent postId={Number(postId)} />;
}

function PostDetailContent({ postId }: { postId: number }) {
    const { isPending, error } = usePost(postId);

    if (isPending) return <div className="h-32 animate-pulse rounded-xl bg-muted" />;

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center gap-3 py-20 text-center text-muted-foreground">
                <p className="text-lg font-semibold">게시물을 찾을 수 없습니다.</p>
                <p className="text-sm">삭제되었거나 존재하지 않는 게시물입니다.</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-5">
            <PostItem postId={postId} type="DETAIL" />
            <CommentEditor type="CREATE" postId={postId} />
            <CommentList postId={postId} />
        </div>
    );
}
