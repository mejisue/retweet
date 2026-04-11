import { Outlet, Link } from "react-router-dom";
import cat from "@/assets/cat.png";
import { SunIcon } from "lucide-react";
import ProfileButton from "@/components/layout/profile-button";

export default function GlobalLayout() {
    return (
        <div className="min-h-[100vh] flex flex-col">
            <header className="h-15 border-b">
                <div className="flex justify-between h-full max-w-175 m-auto px-4">
                    <Link to={"/"} className="flex items-center gap-2">
                        <img src={cat}
                            className="h-7"
                            alt="미지 로그의 로고, 검은 고양이다" />
                        <div className="font-bold">미지 로그</div>
                    </Link>
                    <div className="flex items-center gap-5">
                        <div className="hover:bg-muted rounded-full p-2 cursor-pointer">
                            <SunIcon />
                        </div>
                        <ProfileButton />
                    </div>
                </div>
            </header>
            <main className="max-w-175 w-full m-auto px-4 py-6 border-x flex-1">
                <Outlet />
            </main>
            <footer className="border-t py-10 text-muted-foreground text-center">
                @jisue</footer>
        </div>
    )
}