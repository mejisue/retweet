import { useQuery } from '@tanstack/react-query';
import { fetchComments } from '@/api/comment';
import { QUERY_KEYS } from '@/lib/constants';

export function useCommentsData(postId: number) {
    return useQuery({
        queryKey: QUERY_KEYS.comment.post(postId),
        queryFn: () => fetchComments(postId),
    });
}
