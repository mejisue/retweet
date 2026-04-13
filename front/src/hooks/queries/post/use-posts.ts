import { fetchPosts } from '@/api/post';
import { QUERY_KEYS } from '@/lib/constants';
import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query';

export function usePosts() {
    const queryClient = useQueryClient();

    return useInfiniteQuery({
        queryKey: QUERY_KEYS.post.list,
        queryFn: async ({ pageParam }) => {
            const page = await fetchPosts(pageParam);
            // 개별 포스트를 캐시에 저장
            page.content.forEach((post) => {
                queryClient.setQueryData(QUERY_KEYS.post.byId(post.id), post);
            });
            return page.content.map((post) => post.id);
        },
        initialPageParam: 0,
        getNextPageParam: (lastPage, allPages) => {
            if (lastPage.length < 10) return undefined;
            return allPages.length;
        },
        staleTime: Infinity,
    });
}
