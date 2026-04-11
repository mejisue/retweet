import { describe, it, expect, beforeEach } from 'vitest';
import { getProfileEditorModalStore } from './profile-editor-modal';

beforeEach(() => {
    getProfileEditorModalStore().actions.close();
});

describe('profileEditorModalStore', () => {
    it('초기 상태: isOpen=false', () => {
        const store = getProfileEditorModalStore();
        expect(store.isOpen).toBe(false);
    });

    it('open() 호출 후 isOpen=true가 된다', () => {
        getProfileEditorModalStore().actions.open();

        expect(getProfileEditorModalStore().isOpen).toBe(true);
    });

    it('close() 호출 후 isOpen=false가 된다', () => {
        getProfileEditorModalStore().actions.open();
        getProfileEditorModalStore().actions.close();

        expect(getProfileEditorModalStore().isOpen).toBe(false);
    });

    it('open → close → open 반복 동작이 정상적으로 된다', () => {
        getProfileEditorModalStore().actions.open();
        expect(getProfileEditorModalStore().isOpen).toBe(true);

        getProfileEditorModalStore().actions.close();
        expect(getProfileEditorModalStore().isOpen).toBe(false);

        getProfileEditorModalStore().actions.open();
        expect(getProfileEditorModalStore().isOpen).toBe(true);
    });
});
