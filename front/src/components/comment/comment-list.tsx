import CommentItem from '@/components/comment/comment-item';
import { useCommentsData } from '@/hooks/queries/use-comments-data';
import type { Comment, NestedComment } from '@/types';

function toNestedComments(comments: Comment[]): NestedComment[] {
    const result: NestedComment[] = [];

    comments.forEach((comment) => {
        if (!comment.rootCommentId) {
            result.push({ ...comment, children: [] });
        } else {
            const rootIndex = result.findIndex((item) => item.id === comment.rootCommentId);
            const parentComment = comments.find((item) => item.id === comment.parentCommentId);

            if (rootIndex === -1 || !parentComment) return;

            result[rootIndex].children.push({ ...comment, children: [], parentComment });
        }
    });

    return result;
}

export default function CommentList({ postId }: { postId: number }) {
    const { data: comments, error, isPending } = useCommentsData(postId);

    if (isPending) return <div className="h-10 animate-pulse rounded-xl bg-muted" />;
    if (error || !comments) return null;

    const nestedComments = toNestedComments(comments);

    return (
        <div className="flex flex-col gap-5">
            {nestedComments.map((comment) => (
                <CommentItem key={comment.id} {...comment} />
            ))}
        </div>
    );
}
