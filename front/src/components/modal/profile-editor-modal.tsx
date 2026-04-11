import defaultAvatar from '@/assets/default-avatar.png';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useUpdateProfile } from '@/hooks/mutations/profile/use-update-profile';
import { useMyProfile } from '@/hooks/queries/use-my-profile';
import { useProfileEditorModal } from '@/store/profile-editor-modal';
import { useEffect, useRef, useState, type ChangeEvent } from 'react';
import { toast } from 'sonner';

type AvatarPreview = {
    file: File;
    previewUrl: string;
};

export default function ProfileEditorModal() {
    const { isOpen, actions } = useProfileEditorModal();
    const { data: profile } = useMyProfile();

    const [nickname, setNickname] = useState('');
    const [bio, setBio] = useState('');
    const [avatarPreview, setAvatarPreview] = useState<AvatarPreview | null>(null);

    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (!isOpen) {
            if (avatarPreview) URL.revokeObjectURL(avatarPreview.previewUrl);
            return;
        }
        setNickname(profile?.nickname ?? '');
        setBio(profile?.bio ?? '');
        setAvatarPreview(null);
    }, [isOpen]);

    const { mutate: updateProfile, isPending } = useUpdateProfile({
        onSuccess: () => actions.close(),
        onError: () => toast.error('프로필 수정에 실패했습니다.', { position: 'top-center' }),
    });

    const handleSelectAvatar = (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        if (avatarPreview) URL.revokeObjectURL(avatarPreview.previewUrl);
        setAvatarPreview({ file, previewUrl: URL.createObjectURL(file) });
        e.target.value = '';
    };

    const handleSave = () => {
        if (!nickname.trim()) return;
        updateProfile({ nickname: nickname.trim(), bio: bio.trim(), avatarFile: avatarPreview?.file });
    };

    const currentAvatarSrc = avatarPreview?.previewUrl ?? profile?.avatarUrl ?? defaultAvatar;

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && actions.close()}>
            <DialogContent>
                <DialogTitle>프로필 수정</DialogTitle>
                <div className="flex flex-col gap-4">
                    <div className="flex justify-center">
                        <div
                            className="relative cursor-pointer"
                            onClick={() => fileInputRef.current?.click()}
                        >
                            <img
                                src={currentAvatarSrc}
                                alt="프로필 이미지"
                                className="h-20 w-20 rounded-full object-cover"
                            />
                            <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/30 opacity-0 hover:opacity-100 transition-opacity">
                                <span className="text-white text-xs">변경</span>
                            </div>
                        </div>
                    </div>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        className="hidden"
                        onChange={handleSelectAvatar}
                    />
                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium">닉네임</label>
                        <Input
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            disabled={isPending}
                            placeholder="닉네임을 입력하세요"
                        />
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium">소개</label>
                        <Textarea
                            value={bio}
                            onChange={(e) => setBio(e.target.value)}
                            disabled={isPending}
                            placeholder="자기소개를 입력하세요"
                            className="resize-none"
                        />
                    </div>
                    <Button
                        disabled={isPending || !nickname.trim()}
                        onClick={handleSave}
                        className="cursor-pointer"
                    >
                        수정하기
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
}
