import axios from 'axios';
import { describe, it, expect } from 'vitest';
import { generateErrorMessage } from './error';

describe('generateErrorMessage', () => {
    it('axios 에러에 서버 메시지가 있으면 해당 메시지를 반환한다', () => {
        const error = new axios.AxiosError('Request failed');
        error.response = { data: { message: '이미 사용 중인 이메일입니다.' } } as typeof error.response;

        expect(generateErrorMessage(error)).toBe('이미 사용 중인 이메일입니다.');
    });

    it('axios 에러에 서버 메시지가 없으면 기본 메시지를 반환한다', () => {
        const error = new axios.AxiosError('Network Error');

        expect(generateErrorMessage(error)).toBe('알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    });

    it('일반 Error 객체이면 문제 발생 메시지를 반환한다', () => {
        expect(generateErrorMessage(new Error('unexpected'))).toBe(
            '문제가 발생했습니다. 잠시 후 다시 시도해주세요.'
        );
    });

    it('null 이면 문제 발생 메시지를 반환한다', () => {
        expect(generateErrorMessage(null)).toBe('문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
    });
});
