import { Button } from '@/components/ui/button';
import { Carousel, CarouselContent, CarouselItem } from '@/components/ui/carousel';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { useCreatePost } from '@/hooks/mutations/post/use-create-post';
import { useUpdatePost } from '@/hooks/mutations/post/use-update-post';
import { useAlertModal } from '@/store/alert-modal';
import { usePostEditorModal } from '@/store/post-editor-modal';
import { ImageIcon, XIcon } from 'lucide-react';
import { useEffect, useRef, useState, type ChangeEvent } from 'react';
import { toast } from 'sonner';

type ImagePreview = {
    file: File;
    previewUrl: string;
};

export default function PostEditorModal() {
    const postEditorModal = usePostEditorModal();
    const alertModal = useAlertModal();

    const [content, setContent] = useState('');
    const [images, setImages] = useState<ImagePreview[]>([]);

    const textareaRef = useRef<HTMLTextAreaElement>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.style.height = 'auto';
            textareaRef.current.style.height = textareaRef.current.scrollHeight + 'px';
        }
    }, [content]);

    useEffect(() => {
        if (!postEditorModal.isOpen) {
            images.forEach((image) => URL.revokeObjectURL(image.previewUrl));
            return;
        }

        if (postEditorModal.type === 'CREATE') {
            setContent('');
            setImages([]);
        } else {
            setContent(postEditorModal.content);
            setImages([]);
        }
        textareaRef.current?.focus();
    }, [postEditorModal.isOpen]);

    const { mutate: createPost, isPending: isCreating } = useCreatePost({
        onSuccess: () => postEditorModal.actions.close(),
        onError: () => toast.error('포스트 작성에 실패했습니다.', { position: 'top-center' }),
    });

    const { mutate: updatePost, isPending: isUpdating } = useUpdatePost({
        onSuccess: () => postEditorModal.actions.close(),
        onError: () => toast.error('포스트 수정에 실패했습니다.', { position: 'top-center' }),
    });

    const isPending = isCreating || isUpdating;

    const handleCloseModal = () => {
        if (content !== '' || images.length !== 0) {
            alertModal.actions.open({
                title: '게시글 작성이 마무리 되지 않았습니다.',
                description: '이 화면에서 나가면 작성 중이던 내용이 사라집니다.',
                onPositive: () => postEditorModal.actions.close(),
            });
            return;
        }
        postEditorModal.actions.close();
    };

    const handleSave = () => {
        if (content.trim() === '' || !postEditorModal.isOpen) return;

        if (postEditorModal.type === 'CREATE') {
            createPost({ content: content.trim(), images: images.map((img) => img.file) });
        } else {
            updatePost({
                postId: postEditorModal.postId,
                content: content.trim(),
                imageUrls: postEditorModal.imageUrls,
            });
        }
    };

    const handleSelectImages = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            Array.from(e.target.files).forEach((file) => {
                setImages((prev) => [...prev, { file, previewUrl: URL.createObjectURL(file) }]);
            });
        }
        e.target.value = '';
    };

    const handleDeleteImage = (image: ImagePreview) => {
        setImages((prev) => prev.filter((item) => item.previewUrl !== image.previewUrl));
        URL.revokeObjectURL(image.previewUrl);
    };

    return (
        <Dialog open={postEditorModal.isOpen} onOpenChange={handleCloseModal}>
            <DialogContent className="max-h-[90vh]">
                <DialogTitle>
                    {postEditorModal.isOpen && postEditorModal.type === 'CREATE' ? '포스트 작성' : '포스트 수정'}
                </DialogTitle>
                <Textarea
                    ref={textareaRef}
                    disabled={isPending}
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    className="min-h-32 resize-none focus-visible:ring-0"
                    placeholder="무슨 일이 있었나요?"
                />
                <input
                    ref={fileInputRef}
                    onChange={handleSelectImages}
                    type="file"
                    accept="image/*"
                    multiple
                    className="hidden"
                />

                {/* 수정 모드: 기존 이미지 (읽기 전용) */}
                {postEditorModal.isOpen &&
                    postEditorModal.type === 'EDIT' &&
                    postEditorModal.imageUrls.length > 0 && (
                        <Carousel>
                            <CarouselContent>
                                {postEditorModal.imageUrls.map((url) => (
                                    <CarouselItem className="basis-2/5" key={url}>
                                        <img src={url} className="h-full w-full rounded-sm object-cover" />
                                    </CarouselItem>
                                ))}
                            </CarouselContent>
                        </Carousel>
                    )}

                {/* 새로 추가한 이미지 미리보기 */}
                {images.length > 0 && (
                    <Carousel>
                        <CarouselContent>
                            {images.map((image) => (
                                <CarouselItem className="basis-2/5" key={image.previewUrl}>
                                    <div className="relative">
                                        <img
                                            src={image.previewUrl}
                                            className="h-full w-full rounded-sm object-cover"
                                        />
                                        <div
                                            onClick={() => handleDeleteImage(image)}
                                            className="absolute top-0 right-0 m-1 cursor-pointer rounded-full bg-black/30 p-1"
                                        >
                                            <XIcon className="h-4 w-4 text-white" />
                                        </div>
                                    </div>
                                </CarouselItem>
                            ))}
                        </CarouselContent>
                    </Carousel>
                )}

                {/* 작성 모드: 이미지 추가 버튼 */}
                {postEditorModal.isOpen && postEditorModal.type === 'CREATE' && (
                    <Button
                        onClick={() => fileInputRef.current?.click()}
                        disabled={isPending}
                        variant="outline"
                        className="cursor-pointer"
                    >
                        <ImageIcon />
                        이미지 추가
                    </Button>
                )}

                <Button disabled={isPending || content.trim() === ''} onClick={handleSave} className="cursor-pointer">
                    저장
                </Button>
            </DialogContent>
        </Dialog>
    );
}
