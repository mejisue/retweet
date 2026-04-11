export const QUERY_KEYS = {
    post: {
        all: ['post'],
        list: ['post', 'list'],
        memberList: (memberId: number) => ['post', 'memberList', memberId],
        byId: (postId: number) => ['post', 'byId', postId],
    },
    comment: {
        post: (postId: number) => ['comment', 'post', postId],
    },
} as const;
