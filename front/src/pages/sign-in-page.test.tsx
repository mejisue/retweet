import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import SignInPage from './sign-in-page';

const signInMutateMock = vi.fn();
const oAuthMutateMock = vi.fn();

vi.mock('@/hooks/mutations/auth/use-sign-in-with-password', () => ({
    useSignInWithPassword: () => ({ mutate: signInMutateMock, isPending: false }),
}));

vi.mock('@/hooks/mutations/auth/use-sign-in-with-oauth', () => ({
    useSignInWithOAuth: () => ({ mutate: oAuthMutateMock, isPending: false }),
}));

vi.mock('sonner', () => ({
    toast: { error: vi.fn() },
}));

// github-mark.svg 모킹
vi.mock('@/assets/github-mark.svg', () => ({ default: 'github-mark.svg' }));

const renderPage = () =>
    render(
        <MemoryRouter>
            <SignInPage />
        </MemoryRouter>
    );

beforeEach(() => {
    signInMutateMock.mockClear();
    oAuthMutateMock.mockClear();
});

describe('SignInPage', () => {
    it('이메일, 비밀번호 입력 후 로그인 버튼 클릭 시 signIn이 호출된다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByPlaceholderText('example@example.com'), 'test@example.com');
        await user.type(screen.getByPlaceholderText('password'), 'password123');
        await user.click(screen.getByRole('button', { name: '로그인' }));

        expect(signInMutateMock).toHaveBeenCalledWith({ email: 'test@example.com', password: 'password123' });
    });

    it('이메일이 비어있으면 signIn이 호출되지 않는다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.type(screen.getByPlaceholderText('password'), 'password123');
        await user.click(screen.getByRole('button', { name: '로그인' }));

        expect(signInMutateMock).not.toHaveBeenCalled();
    });

    it('GitHub 계정으로 로그인 버튼 클릭 시 OAuth 함수가 호출된다', async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(screen.getByRole('button', { name: /GitHub 계정으로 로그인/ }));

        expect(oAuthMutateMock).toHaveBeenCalled();
    });

    it('"계정이 없으시다면? 회원가입" 링크가 렌더링된다', () => {
        renderPage();
        expect(screen.getByText('계정이 없으시다면? 회원가입')).toBeInTheDocument();
    });

    it('"비밀번호를 잊으셨나요?" 링크가 렌더링된다', () => {
        renderPage();
        expect(screen.getByText('비밀번호를 잊으셨나요?')).toBeInTheDocument();
    });
});
