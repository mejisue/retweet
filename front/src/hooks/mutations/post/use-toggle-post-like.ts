import { togglePostLike } from '@/api/post';
import { QUERY_KEYS } from '@/lib/constants';
import type { Post, UseMutationCallback } from '@/types';
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useTogglePostLike(postId: number, callbacks?: UseMutationCallback) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: () => togglePostLike(postId),
        onMutate: async () => {
            await queryClient.cancelQueries({ queryKey: QUERY_KEYS.post.byId(postId) });

            const prevPost = queryClient.getQueryData<Post>(QUERY_KEYS.post.byId(postId));

            queryClient.setQueryData<Post>(QUERY_KEYS.post.byId(postId), (post) => {
                if (!post) return post;
                return {
                    ...post,
                    isLiked: !post.isLiked,
                    likeCount: post.isLiked ? post.likeCount - 1 : post.likeCount + 1,
                };
            });

            return { prevPost };
        },
        onSuccess: () => {
            // 서버 확정 값으로 갱신
            queryClient.invalidateQueries({ queryKey: QUERY_KEYS.post.byId(postId) });
            queryClient.invalidateQueries({ queryKey: QUERY_KEYS.post.list });
            callbacks?.onSuccess?.();
        },
        onError: (error, _, context) => {
            if (context?.prevPost) {
                queryClient.setQueryData(QUERY_KEYS.post.byId(postId), context.prevPost);
            }
            callbacks?.onError?.(error);
        },
    });
}
