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
