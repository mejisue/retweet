import defaultAvatar from '@/assets/default-avatar.png';
import { Button } from '@/components/ui/button';
import { useProfile } from '@/hooks/queries/use-profile';
import { useSession } from '@/store/session';
import { useOpenProfileEditorModal } from '@/store/profile-editor-modal';

type Props = {
    userId: number;
};

export default function ProfileInfo({ userId }: Props) {
    const session = useSession();
    const { data: profile, isPending, error } = useProfile(userId);
    const openProfileEditor = useOpenProfileEditorModal();

    if (isPending) return <div className="h-48 animate-pulse rounded-xl bg-muted" />;
    if (error || !profile) return <p className="text-muted-foreground">프로필을 불러오지 못했습니다.</p>;

    const isMine = session?.member.memberId === userId;

    return (
        <div className="flex flex-col items-center gap-4 pb-8 border-b">
            <img
                src={profile.avatarUrl ?? defaultAvatar}
                alt={`${profile.nickname}의 프로필`}
                className="h-28 w-28 rounded-full object-cover"
            />
            <div className="flex flex-col items-center gap-2">
                <span className="font-bold text-xl">{profile.nickname}</span>
                {profile.bio && <p className="text-muted-foreground text-sm">{profile.bio}</p>}
            </div>
            {isMine && (
                <Button variant="outline" onClick={openProfileEditor} className="cursor-pointer">
                    프로필 수정
                </Button>
            )}
        </div>
    );
}
