import PostItem from '@/components/post/post-item';
import { useMemberPosts } from '@/hooks/queries/post/use-member-posts';
import { useEffect } from 'react';
import { useInView } from 'react-intersection-observer';

type Props = {
    memberId: number;
};

export default function MemberPostFeed({ memberId }: Props) {
    const { data, error, isPending, fetchNextPage, isFetchingNextPage } = useMemberPosts(memberId);
    const { ref, inView } = useInView();

    useEffect(() => {
        if (inView) fetchNextPage();
    }, [inView]);

    if (error) return <p className="text-muted-foreground text-center py-10">게시글을 불러오지 못했습니다.</p>;
    if (isPending) return <div className="flex flex-col gap-4">{Array.from({ length: 3 }).map((_, i) => <div key={i} className="h-32 animate-pulse rounded-xl bg-muted" />)}</div>;

    const allPostIds = data.pages.flat();

    if (allPostIds.length === 0) {
        return <p className="text-muted-foreground text-center py-10">아직 게시글이 없습니다.</p>;
    }

    return (
        <div className="flex flex-col gap-10">
            {allPostIds.map((postId) => (
                <PostItem key={postId} postId={postId} type="FEED" />
            ))}
            {isFetchingNextPage && <div className="h-32 animate-pulse rounded-xl bg-muted" />}
            <div ref={ref} />
        </div>
    );
}
