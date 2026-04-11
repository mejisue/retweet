import CommentEditor from '@/components/comment/comment-editor';
import CommentList from '@/components/comment/comment-list';
import PostItem from '@/components/post/post-item';
import { Navigate, useParams } from 'react-router-dom';

export default function PostDetailPage() {
    const { postId } = useParams();

    if (!postId) return <Navigate to="/" />;

    return (
        <div className="flex flex-col gap-5">
            <PostItem postId={Number(postId)} type="DETAIL" />
            <CommentEditor type="CREATE" postId={Number(postId)} />
            <CommentList postId={Number(postId)} />
        </div>
    );
}
