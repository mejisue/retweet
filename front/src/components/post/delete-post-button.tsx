import { Button } from '@/components/ui/button';
import { useDeletePost } from '@/hooks/mutations/post/use-delete-post';
import { Trash2Icon } from 'lucide-react';
import { toast } from 'sonner';

type Props = {
    postId: number;
    onDeleted?: () => void;
};

export default function DeletePostButton({ postId, onDeleted }: Props) {
    const { mutate: deletePost, isPending } = useDeletePost({
        onSuccess: onDeleted,
        onError: () => toast.error('게시글 삭제에 실패했습니다.', { position: 'top-center' }),
    });

    const handleClick = () => {
        if (window.confirm('게시글을 삭제하시겠습니까?')) {
            deletePost(postId);
        }
    };

    return (
        <Button
            variant="ghost"
            size="icon-sm"
            disabled={isPending}
            onClick={handleClick}
            className="text-muted-foreground hover:text-destructive"
        >
            <Trash2Icon className="h-4 w-4" />
        </Button>
    );
}
