import { authWithGithub } from '@/api/auth';
import GlobalLoader from '@/components/global-loader';
import { AMPLITUDE_EVENTS, identifyUser, trackEvent } from '@/lib/analytics';
import { useSetSession } from '@/store/session';
import { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function GithubCallbackPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const setSession = useSetSession();
    const called = useRef(false);

    useEffect(() => {
        if (called.current) return;
        called.current = true;

        const code = searchParams.get('code');
        if (!code) {
            navigate('/sign-in', { replace: true });
            return;
        }

        authWithGithub(code)
            .then((session) => {
                setSession(session);
                identifyUser(session.member.memberId);
                trackEvent(AMPLITUDE_EVENTS.SIGN_IN, { method: 'github' });
                navigate('/', { replace: true });
            })
            .catch(() => {
                toast.error('GitHub 로그인에 실패했습니다.', { position: 'top-center' });
                navigate('/sign-in', { replace: true });
            });
    }, []);

    return <GlobalLoader />;
}
