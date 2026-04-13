import { useTogglePostLike } from '@/hooks/mutations/post/use-toggle-post-like';
import { HeartIcon } from 'lucide-react';
import { toast } from 'sonner';

type Props = {
    postId: number;
    likeCount: number;
    isLiked: boolean;
};

export default function LikePostButton({ postId, likeCount, isLiked }: Props) {
    const { mutate: toggleLike, isPending } = useTogglePostLike(postId, {
        onError: () => toast.error('좋아요 요청에 실패했습니다.', { position: 'top-center' }),
    });

    return (
        <button
            onClick={toggleLike}
            disabled={isPending}
            className="hover:bg-muted flex cursor-pointer items-center gap-2 rounded-xl border p-2 px-4 text-sm disabled:opacity-50"
        >
            <HeartIcon
                className="h-4 w-4"
                fill={isLiked ? "currentColor" : "none"}
            />
            <span>{likeCount}</span>
        </button>
    );
}
