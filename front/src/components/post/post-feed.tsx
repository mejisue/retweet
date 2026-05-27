import PostItem from '@/components/post/post-item';
import { usePosts } from '@/hooks/queries/post/use-posts';
import { useWindowVirtualizer } from '@tanstack/react-virtual';
import { useEffect, useRef } from 'react';
import { useInView } from 'react-intersection-observer';

export default function PostFeed() {
    const { data, error, isPending, fetchNextPage, isFetchingNextPage } = usePosts();
    const { ref: bottomRef, inView } = useInView();

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
        <VirtualFeed
            postIds={allPostIds}
            isFetchingNextPage={isFetchingNextPage}
            bottomRef={bottomRef}
        />
    );
}

// ─── 가상화 로직을 별도 컴포넌트로 분리 ─────────────────────────────────────
// postIds가 확정된 이후에만 virtualizer를 초기화해야 하기 때문에
// 로딩/에러 분기가 끝난 뒤의 컴포넌트로 분리했습니다.
type VirtualFeedProps = {
    postIds: number[];
    isFetchingNextPage: boolean;
    bottomRef: (node?: Element | null) => void;
};

function VirtualFeed({ postIds, isFetchingNextPage, bottomRef }: VirtualFeedProps) {
    // ─── 1. 피드 컨테이너 ref ─────────────────────────────────────────────
    // useWindowVirtualizer는 window 전체의 스크롤을 감지하므로
    // 스크롤 컨테이너 ref가 필요 없습니다.
    // 단, 피드 컨테이너가 페이지 상단에서 얼마나 떨어져 있는지(offset)를
    // 알기 위해 ref를 사용합니다.
    // 예: 헤더가 60px이면 피드는 y:60 부터 시작 → 이걸 감안해서 계산해야 함
    const listRef = useRef<HTMLDivElement>(null);

    // ─── 2. useWindowVirtualizer 초기화 ──────────────────────────────────
    // useVirtualizer: 특정 div(overflow:auto)가 스크롤되는 구조
    // useWindowVirtualizer: window 전체가 스크롤되는 구조 ← 이 앱은 여기 해당
    const virtualizer = useWindowVirtualizer({
        // 총 아이템 수. 이 숫자 기반으로 전체 높이를 계산합니다.
        count: postIds.length,

        // 아이템 높이 초기 추정값(px).
        // PostItem은 높이가 가변적(텍스트 길이, 이미지 유무)이므로
        // measureElement로 실측하기 전까지 이 값을 임시로 사용합니다.
        estimateSize: () => 200,

        // 화면 위아래로 미리 렌더링할 여유 아이템 수.
        // 3이면 화면 위 3개, 아래 3개를 미리 DOM에 만들어둡니다.
        // 스크롤 시 빈 화면이 잠깐 보이는 flash 현상을 방지합니다.
        overscan: 3,

        // 피드 컨테이너의 페이지 상단 기준 y 오프셋.
        // virtualizer가 "지금 화면에 보이는 아이템"을 계산할 때
        // 피드가 y:0 에서 시작하지 않는다는 걸 알려줘야 합니다.
        // eslint-disable-next-line react-hooks/refs
        scrollMargin: listRef.current?.offsetTop ?? 0,
    });

    return (
        // ─── 3. 피드 컨테이너 ────────────────────────────────────────────
        <div ref={listRef}>
            {/*
             * ─── 4. 전체 높이 확보용 내부 컨테이너 ─────────────────────────
             * virtualizer.getTotalSize()는 모든 아이템 높이의 합계입니다.
             * 이 높이를 잡아둬야 스크롤바 길이가 실제 아이템 수에 맞게 보입니다.
             * 예) 100개 × 200px = 20,000px → 스크롤바가 그 길이만큼 생김
             *
             * position: relative 로 설정하는 이유:
             * 자식 요소들이 position: absolute 로 배치되는데,
             * absolute는 가장 가까운 relative 조상을 기준으로 위치가 결정됩니다.
             */}
            <div
                style={{
                    height: virtualizer.getTotalSize(),
                    position: 'relative',
                }}
            >
                {/*
                 * ─── 5. 실제로 DOM에 렌더할 아이템들 ───────────────────────
                 * virtualizer.getVirtualItems()는 현재 화면에 보여야 하는
                 * 아이템 목록만 반환합니다. 스크롤 위치에 따라 계속 바뀝니다.
                 *
                 * virtualItem의 구조:
                 * - index: 전체 목록에서의 인덱스 (0~N)
                 * - key:   고유 키
                 * - start: 이 아이템 상단의 y 좌표 (px)
                 */}
                {virtualizer.getVirtualItems().map((virtualItem) => (
                    <div
                        key={virtualItem.key}
                        // data-index를 달아두면 measureElement가
                        // 이 요소가 몇 번 아이템인지 식별할 수 있습니다.
                        data-index={virtualItem.index}
                        // 이 요소가 DOM에 마운트되면 실제 높이를 측정해서
                        // virtualizer에게 전달합니다(estimateSize 대체).
                        ref={virtualizer.measureElement}
                        style={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            width: '100%',
                            // start에서 scrollMargin(피드 오프셋)을 빼야
                            // 페이지 상단 기준이 아닌 피드 컨테이너 기준으로 배치됩니다.
                            transform: `translateY(${virtualItem.start - virtualizer.options.scrollMargin}px)`,
                            // 아이템 사이 간격 (기존 gap-10 = 40px 대체)
                            paddingBottom: '40px',
                        }}
                    >
                        <PostItem postId={postIds[virtualItem.index]} type="FEED" />
                    </div>
                ))}
            </div>

            {/* 무한 스크롤 트리거 + 로딩 표시 */}
            {isFetchingNextPage && <div className="h-32 animate-pulse rounded-xl bg-muted" />}
            <div ref={bottomRef} />
        </div>
    );
}
