import { refreshSession } from '@/api/auth';
import { identifyUser, resetUser } from '@/lib/analytics';
import { useClearSession, useIsSessionLoaded, useSetSession } from '@/store/session';
import { useEffect, type ReactNode } from 'react';
import GlobalLoader from '@/components/global-loader';

export default function SessionProvider({ children }: { children: ReactNode }) {
    const setSession = useSetSession();
    const clearSession = useClearSession();
    const isLoaded = useIsSessionLoaded();

    useEffect(() => {
        refreshSession()
            .then((session) => {
                setSession(session);
                identifyUser(session.member.memberId);
            })
            .catch(() => {
                clearSession();
                resetUser();
            });
    }, []);

    if (!isLoaded) return <GlobalLoader />;

    return <>{children}</>;
}
