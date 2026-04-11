import { Button } from '@/components/ui/button';
import { useOpenEditPostModal } from '@/store/post-editor-modal';
import { PencilIcon } from 'lucide-react';

type Props = {
    postId: number;
    content: string;
};

export default function EditPostButton({ postId, content }: Props) {
    const openEdit = useOpenEditPostModal();

    return (
        <Button
            variant="ghost"
            size="icon-sm"
            className="text-muted-foreground"
            onClick={() => openEdit({ postId, content })}
        >
            <PencilIcon className="h-4 w-4" />
        </Button>
    );
}
