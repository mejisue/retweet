export type Member = {
    memberId: number;
    email: string | null;
    provider: 'LOCAL' | 'GITHUB';
};

export type Session = {
    accessToken: string;
    member: Member;
};

export type UseMutationCallback = {
    onSuccess?: () => void;
    onError?: (error: Error) => void;
    onMutate?: () => void;
    onSettled?: () => void;
};

export type AuthorInfo = {
    memberId: number;
    nickname: string;
    avatarUrl: string | null;
};

export type Post = {
    id: number;
    content: string;
    imageUrls: string[];
    likeCount: number;
    author: AuthorInfo;
    createdAt: string;
    updatedAt: string;
};

export type Comment = {
    id: number;
    postId: number;
    content: string;
    author: AuthorInfo;
    parentCommentId: number | null;
    rootCommentId: number | null;
    createdAt: string;
    updatedAt: string;
};

export type NestedComment = Comment & {
    parentComment?: Comment;
    children: NestedComment[];
};

export type PageSlice<T> = {
    content: T[];
    hasNext: boolean;
    number: number;
    size: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
    empty: boolean;
};
