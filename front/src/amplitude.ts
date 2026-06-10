import * as amplitude from '@amplitude/analytics-browser';

amplitude.init(import.meta.env.VITE_AMPLITUDE_API_KEY, {
    autocapture: {
        pageViews: true,
        sessions: true,
        attribution: false,
        formInteractions: false,
        fileDownloads: false,
        elementInteractions: false,
    },
});
