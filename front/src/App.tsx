import PostEditorModal from './components/modal/post-editor-modal';
import SessionProvider from './provider/session-provider';
import RootRoute from './root-route';

export default function App() {
    return (
        <SessionProvider>
            <RootRoute />
            <PostEditorModal />
        </SessionProvider>
    );
}
