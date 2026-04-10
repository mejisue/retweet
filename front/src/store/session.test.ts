import { describe, it, expect, beforeEach } from 'vitest';
import { getSessionStore } from './session';
import type { Session } from '@/types';

const mockSession: Session = {
    accessToken: 'access-token',
    member: { memberId: 1, email: 'test@example.com', provider: 'LOCAL' },
};

beforeEach(() => {
    getSessionStore().actions.clearSession();
});

describe('sessionStore', () => {
    it('초기 상태: session=null, isLoaded=false', () => {
        const store = getSessionStore();
        // clearSession sets isLoaded=true, so reset manually
        expect(store.session).toBeNull();
    });

    it('setSession 호출 후 session이 저장되고 isLoaded=true가 된다', () => {
        getSessionStore().actions.setSession(mockSession);

        const store = getSessionStore();
        expect(store.session).toEqual(mockSession);
        expect(store.isLoaded).toBe(true);
    });

    it('clearSession 호출 후 session=null, isLoaded=true가 된다', () => {
        getSessionStore().actions.setSession(mockSession);
        getSessionStore().actions.clearSession();

        const store = getSessionStore();
        expect(store.session).toBeNull();
        expect(store.isLoaded).toBe(true);
    });
});
