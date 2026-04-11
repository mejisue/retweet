import AlertModal from './components/modal/alert-modal';
import CommentEditorModal from './components/modal/comment-editor-modal';
import PostEditorModal from './components/modal/post-editor-modal';
import ProfileEditorModal from './components/modal/profile-editor-modal';
import SessionProvider from './provider/session-provider';
import RootRoute from './root-route';

export default function App() {
    return (
        <SessionProvider>
            <RootRoute />
            <PostEditorModal />
            <CommentEditorModal />
            <ProfileEditorModal />
            <AlertModal />
        </SessionProvider>
    );
}
