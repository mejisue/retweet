import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useSignInWithPassword } from '@/hooks/mutations/auth/use-sign-in-with-password';
import { useSignInWithOAuth } from '@/hooks/mutations/auth/use-sign-in-with-oauth';
import { generateErrorMessage } from '@/lib/error';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import githubLogo from '@/assets/github-mark.svg';

export default function SignInPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const { mutate: signIn, isPending: isSignInPending } = useSignInWithPassword({
        onSuccess: () => navigate('/'),
        onError: (error) => {
            toast.error(generateErrorMessage(error), { position: 'top-center' });
            setPassword('');
        },
    });

    const { mutate: signInWithOAuth, isPending: isOAuthPending } = useSignInWithOAuth({
        onError: (error) => {
            toast.error(generateErrorMessage(error), { position: 'top-center' });
        },
    });

    const isPending = isSignInPending || isOAuthPending;

    const handleSignInClick = () => {
        if (email.trim() === '' || password.trim() === '') return;
        signIn({ email, password });
    };

    return (
        <div className="flex flex-col gap-8">
            <div className="text-xl font-bold">로그인</div>
            <div className="flex flex-col gap-2">
                <Input
                    disabled={isPending}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="py-6"
                    type="email"
                    placeholder="example@example.com"
                />
                <Input
                    disabled={isPending}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="py-6"
                    type="password"
                    placeholder="password"
                />
            </div>
            <div className="flex flex-col gap-2">
                <Button disabled={isPending} className="w-full" onClick={handleSignInClick}>
                    로그인
                </Button>
                <Button
                    disabled={isPending}
                    className="w-full"
                    variant="outline"
                    onClick={() => signInWithOAuth()}
                >
                    <img src={githubLogo} className="h-4 w-4" />
                    GitHub 계정으로 로그인
                </Button>
            </div>
            <div className="flex flex-col gap-2">
                <Link className="text-muted-foreground hover:underline" to="/sign-up">
                    계정이 없으시다면? 회원가입
                </Link>
                <Link className="text-muted-foreground hover:underline" to="/forget-password">
                    비밀번호를 잊으셨나요?
                </Link>
            </div>
        </div>
    );
}
