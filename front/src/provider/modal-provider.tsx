import AlertModal from "@/components/modal/alert-modal";
import CommentEditorModal from "@/components/modal/comment-editor-modal";
import PostEditorModal from "@/components/modal/post-editor-modal";
import ProfileEditorModal from "@/components/modal/profile-editor-modal";
import type { ReactNode } from "react";
import { createPortal } from "react-dom";

export default function ModalProvider({ children }: { children: ReactNode }) {
    return (
        <>
            {createPortal(
                <><PostEditorModal /><CommentEditorModal /><AlertModal /><ProfileEditorModal /></>,
                document.getElementById("modal-root")!
            )}
            {children}
        </>
    );
}
