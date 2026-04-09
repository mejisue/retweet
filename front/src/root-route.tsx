import { Navigate, Route, Routes } from "react-router-dom";
import SignInPage from "./pages/sign-in-page";
import SignUpPage from "./pages/sign-up-page";
import ForgetPasswordPage from "./pages/forget-password-page";
import PostDetailPage from "./pages/post-detail-page";
import ProfileDetailPage from "./pages/profile-detail-page";
import ResetPasswordPage from "./pages/reset-password-page";
import GlobalLayout from "@/components/layout/global-layout";
import IndexPage from "./pages/index-page";

export default function RootRoute() {
    return (
        <Routes>
            <Route element={<GlobalLayout />}>
                <Route path="/sign-in" element={<SignInPage />} />
                <Route path="/sign-up" element={<SignUpPage />} />
                <Route path="/forget-password" element={<ForgetPasswordPage />} />

                <Route path="/post/:postId" element={<PostDetailPage />} />
                <Route path="/profile/:userId" element={<ProfileDetailPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="*" element={<IndexPage />} />
            </Route>
        </Routes>
    )
}