import { Navigate, Route, Routes } from 'react-router-dom';
import GlobalLayout from '@/components/layout/global-layout';
import GuestOnlyLayout from '@/components/layout/guest-only-layout';
import MemberOnlyLayout from '@/components/layout/member-only-layout';
import IndexPage from './pages/index-page';
import SignInPage from './pages/sign-in-page';
import SignUpPage from './pages/sign-up-page';
import ForgetPasswordPage from './pages/forget-password-page';
import ResetPasswordPage from './pages/reset-password-page';
import PostDetailPage from './pages/post-detail-page';
import ProfileDetailPage from './pages/profile-detail-page';
import GithubCallbackPage from './pages/github-callback-page';

export default function RootRoute() {
    return (
        <Routes>
            {/* GitHub OAuth 콜백 (레이아웃 없이 독립 처리) */}
            <Route path="/auth/github/callback" element={<GithubCallbackPage />} />

            <Route element={<GlobalLayout />}>
                <Route element={<GuestOnlyLayout />}>
                    <Route path="/sign-in" element={<SignInPage />} />
                    <Route path="/sign-up" element={<SignUpPage />} />
                    <Route path="/forget-password" element={<ForgetPasswordPage />} />
                </Route>

                <Route element={<MemberOnlyLayout />}>
                    <Route path="/" element={<IndexPage />} />
                    <Route path="/post/:postId" element={<PostDetailPage />} />
                    <Route path="/profile/:userId" element={<ProfileDetailPage />} />
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                </Route>

                <Route path="*" element={<Navigate to="/" />} />
            </Route>
        </Routes>
    );
}
