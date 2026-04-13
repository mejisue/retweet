import MemberPostFeed from '@/components/post/member-post-feed';
import ProfileInfo from '@/components/profile/profile-info';
import { useEffect } from 'react';
import { useParams } from 'react-router-dom';

export default function ProfileDetailPage() {
    const { userId } = useParams<{ userId: string }>();
    const memberId = Number(userId);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [memberId]);

    if (!memberId) return null;

    return (
        <div className="flex flex-col gap-10">
            <ProfileInfo userId={memberId} />
            <MemberPostFeed memberId={memberId} />
        </div>
    );
}
