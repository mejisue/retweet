import cat from '@/assets/cat.png';

export default function GlobalLoader() {
    return (
        <div className="bg-muted h-[100vh] w-[100vw] flex flex-col items-center justify-center">
            <div className="mb-15 flex animate-bounce items-center gap-4">
                <img src={cat} alt="미지 로그 로고" className="w-10" />
                <div className="text-2xl font-bold">미지 로그</div>
            </div>
        </div>
    );
}
