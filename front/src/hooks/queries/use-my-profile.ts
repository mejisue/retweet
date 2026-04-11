import { fetchMyProfile } from '@/api/profile';
import { QUERY_KEYS } from '@/lib/constants';
import { useSession } from '@/store/session';
import { useQuery } from '@tanstack/react-query';

export function useMyProfile() {
    const session = useSession();

    return useQuery({
        queryKey: QUERY_KEYS.profile.me(session?.member.memberId ?? 0),
        queryFn: fetchMyProfile,
        enabled: !!session,
    });
}
