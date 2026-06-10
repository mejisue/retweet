import * as amplitude from '@amplitude/analytics-browser';

export const AMPLITUDE_EVENTS = {
    SIGN_UP: 'sign_up',
    SIGN_IN: 'sign_in',
    SIGN_OUT: 'sign_out',
    POST_CREATED: 'post_created',
    POST_LIKE_TOGGLED: 'post_like_toggled',
    COMMENT_CREATED: 'comment_created',
} as const;

type AmplitudeEvent = (typeof AMPLITUDE_EVENTS)[keyof typeof AMPLITUDE_EVENTS];

export function trackEvent(eventName: AmplitudeEvent, properties?: Record<string, unknown>) {
    amplitude.track(eventName, properties);
}

export function identifyUser(memberId: number) {
    amplitude.setUserId(String(memberId));
}

export function resetUser() {
    amplitude.reset();
}
