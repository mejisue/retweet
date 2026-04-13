import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useResetPassword } from '@/hooks/mutations/auth/use-reset-password';
import { generateErrorMessage } from '@/lib/error';
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';

export default function ResetPasswordPage() {
    const [password, setPassword] = useState('');
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';

    const { mutate: resetPassword, isPending } = useResetPassword({
        onSuccess: () => {
            toast.info('비밀번호가 성공적으로 변경되었습니다.', { position: 'top-center' });
            navigate('/sign-in');
        },
        onError: (error) => {
            toast.error(generateErrorMessage(error), { position: 'top-center' });
            setPassword('');
        },
    });

    const handleUpdatePasswordClick = () => {
        if (password.trim() === '' || !token) return;
        resetPassword({ token, newPassword: password });
    };

    return (
        <div className="flex flex-col gap-8">
            <div className="flex flex-col gap-1">
                <div className="text-xl font-bold">비밀번호 재설정하기</div>
                <div className="text-muted-foreground">새로운 비밀번호를 입력하세요</div>
            </div>
            <Input
                disabled={isPending}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                type="password"
                className="py-6"
                placeholder="password"
            />
            <Button disabled={isPending} onClick={handleUpdatePasswordClick} className="w-full">
                비밀번호 변경하기
            </Button>
        </div>
    );
}
