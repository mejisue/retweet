import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import SignUpPage from './sign-up-page';

const mutateMock = vi.fn();

vi.mock('@/hooks/mutations/auth/use-sign-up', () => ({
    useSignUp: () => ({ mutate: mutateMock, isPending: false }),
}));

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}));

const renderPage = () =>
    render(
        <MemoryRouter>
            <SignUpPage />
        </MemoryRouter>
    );

beforeEach(() => {
    mutateMock.mockClear();
});

describe('SignUpPage', () => {
    it('이메일, 비밀번호 입력 후 회원가입 버튼 클릭 시 signUp이 호출된다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByPlaceholderText('example@example.com'), 'test@example.com');
        await user.type(screen.getByPlaceholderText('password'), 'password123');
        await user.click(screen.getByRole('button', { name: '회원가입' }));

        expect(mutateMock).toHaveBeenCalledWith({ email: 'test@example.com', password: 'password123' });
    });

    it('이메일이 비어있으면 signUp이 호출되지 않는다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByPlaceholderText('password'), 'password123');
        await user.click(screen.getByRole('button', { name: '회원가입' }));

        expect(mutateMock).not.toHaveBeenCalled();
    });

    it('비밀번호가 비어있으면 signUp이 호출되지 않는다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByPlaceholderText('example@example.com'), 'test@example.com');
        await user.click(screen.getByRole('button', { name: '회원가입' }));

        expect(mutateMock).not.toHaveBeenCalled();
    });

    it('"이미 계정이 있다면? 로그인" 링크가 렌더링된다', () => {
        renderPage();
        expect(screen.getByText('이미 계정이 있다면? 로그인')).toBeInTheDocument();
    });
});
