import { fetchProfile } from '@/api/profile';
import { QUERY_KEYS } from '@/lib/constants';
import { useQuery } from '@tanstack/react-query';

export function useProfile(userId: number) {
    return useQuery({
        queryKey: QUERY_KEYS.profile.byId(userId),
        queryFn: () => fetchProfile(userId),
        enabled: !!userId,
    });
}
