import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import type { Theme } from './types.ts';

const stored = localStorage.getItem("ThemeStore");
const theme: Theme = stored ? (JSON.parse(stored)?.state?.theme ?? "light") : "light";
document.documentElement.classList.add(theme);
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { BrowserRouter } from 'react-router-dom';
import { Toaster } from '@/components/ui/sonner';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: false,
            refetchOnWindowFocus: false,
        },
    },
});

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <BrowserRouter>
            <QueryClientProvider client={queryClient}>
                <ReactQueryDevtools />
                <App />
                <Toaster />
            </QueryClientProvider>
        </BrowserRouter>
    </StrictMode>
);
