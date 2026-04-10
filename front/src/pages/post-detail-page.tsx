import PostItem from '@/components/post/post-item';
import { Navigate, useParams } from 'react-router-dom';

export default function PostDetailPage() {
    const { postId } = useParams();

    if (!postId) return <Navigate to="/" />;

    return (
        <div className="flex flex-col gap-5">
            <PostItem postId={Number(postId)} type="DETAIL" />
        </div>
    );
}
