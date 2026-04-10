import axios from 'axios';

export function generateErrorMessage(error: unknown): string {
    if (axios.isAxiosError(error)) {
        return error.response?.data?.message ?? '알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
    return '문제가 발생했습니다. 잠시 후 다시 시도해주세요.';
}
