import { fetchPost } from '@/api/post';
import { QUERY_KEYS } from '@/lib/constants';
import { useQuery } from '@tanstack/react-query';

export function usePost(postId: number) {
    return useQuery({
        queryKey: QUERY_KEYS.post.byId(postId),
        queryFn: () => fetchPost(postId),
    });
}
