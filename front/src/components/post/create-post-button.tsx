import { useOpenCreatePostModal } from '@/store/post-editor-modal';
import { PlusCircleIcon } from 'lucide-react';

export default function CreatePostButton() {
    const openCreate = useOpenCreatePostModal();

    return (
        <button
            className="w-full flex items-center justify-between px-5 py-4 rounded-xl bg-muted text-muted-foreground hover:bg-muted/70 transition-colors cursor-pointer"
            onClick={openCreate}
        >
            <span>나누고 싶은 이야기가 있나요?</span>
            <PlusCircleIcon className="h-6 w-6 shrink-0" />
        </button>
    );
}
