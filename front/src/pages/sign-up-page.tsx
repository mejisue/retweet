import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useSignUp } from '@/hooks/mutations/auth/use-sign-up';
import { generateErrorMessage } from '@/lib/error';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';

export default function SignUpPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const { mutate: signUp, isPending } = useSignUp({
        onSuccess: () => navigate('/'),
        onError: (error) => {
            toast.error(generateErrorMessage(error), { position: 'top-center' });
        },
    });

    const handleSignUpClick = () => {
        if (email.trim() === '' || password.trim() === '') return;
        signUp({ email, password });
    };

    return (
        <div className="flex flex-col gap-8">
            <div className="text-xl font-bold">회원가입</div>
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
            <div>
                <Button disabled={isPending} className="w-full" onClick={handleSignUpClick}>
                    회원가입
                </Button>
            </div>
            <div>
                <Link className="text-muted-foreground hover:underline" to="/sign-in">
                    이미 계정이 있다면? 로그인
                </Link>
            </div>
        </div>
    );
}
