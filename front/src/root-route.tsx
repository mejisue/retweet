import * as Sentry from '@sentry/react';
import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

const SentryRoutes = Sentry.withSentryReactRouterV7Routing(Routes);
import GlobalLayout from '@/components/layout/global-layout';
import GuestOnlyLayout from '@/components/layout/guest-only-layout';
import MemberOnlyLayout from '@/components/layout/member-only-layout';

const IndexPage = lazy(() => import('./pages/index-page'));
const SignInPage = lazy(() => import('./pages/sign-in-page'));
const SignUpPage = lazy(() => import('./pages/sign-up-page'));
const ForgetPasswordPage = lazy(() => import('./pages/forget-password-page'));
const ResetPasswordPage = lazy(() => import('./pages/reset-password-page'));
const PostDetailPage = lazy(() => import('./pages/post-detail-page'));
const ProfileDetailPage = lazy(() => import('./pages/profile-detail-page'));
const GithubCallbackPage = lazy(() => import('./pages/github-callback-page'));

export default function RootRoute() {
    return (
        <Suspense fallback={null}>
        <SentryRoutes>
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
        </SentryRoutes>
        </Suspense>
    );
}
