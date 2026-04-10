import { Button } from '@/components/ui/button';
import { useOpenCreatePostModal } from '@/store/post-editor-modal';
import { PlusIcon } from 'lucide-react';

export default function CreatePostButton() {
    const openCreate = useOpenCreatePostModal();

    return (
        <Button className="w-full" variant="outline" onClick={openCreate}>
            <PlusIcon className="h-4 w-4" />
            포스트 작성
        </Button>
    );
}
