import { fetchMyProfile } from '@/api/profile';
import { useSession } from '@/store/session';
import { useQuery } from '@tanstack/react-query';

export function useMyProfile() {
    const session = useSession();

    return useQuery({
        queryKey: ['profile', 'me', session?.member.memberId],
        queryFn: fetchMyProfile,
        enabled: !!session,
    });
}
