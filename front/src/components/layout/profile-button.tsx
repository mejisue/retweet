import defaultAvatar from '@/assets/default-avatar.png';
import { useSignOut } from '@/hooks/mutations/auth/use-sign-out';
import { useMyProfile } from '@/hooks/queries/use-my-profile';
import { useSession } from '@/store/session';
import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';

export default function ProfileButton() {
    const session = useSession();
    const { data: profile } = useMyProfile();
    const { mutate: signOut } = useSignOut();
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div className="relative" ref={ref}>
            <img
                className="h-8 w-8 cursor-pointer rounded-full object-cover"
                src={profile?.avatarUrl ?? defaultAvatar}
                alt="프로필"
                onClick={() => setOpen((prev) => !prev)}
            />
            {open && (
                <div className="absolute right-0 top-10 z-50 w-36 overflow-hidden rounded-xl border bg-background shadow-md">
                    <Link
                        to={`/profile/${session?.member.memberId}`}
                        className="block px-4 py-3 text-sm hover:bg-muted"
                        onClick={() => setOpen(false)}
                    >
                        프로필
                    </Link>
                    <button
                        onClick={() => { signOut(); setOpen(false); }}
                        className="w-full cursor-pointer px-4 py-3 text-left text-sm hover:bg-muted"
                    >
                        로그아웃
                    </button>
                </div>
            )}
        </div>
    );
}
