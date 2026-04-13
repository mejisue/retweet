import { describe, it, expect, vi, beforeEach } from 'vitest';
import { fetchMyProfile, fetchProfile, updateProfile } from './profile';

const { getMock, putMock } = vi.hoisted(() => ({
    getMock: vi.fn(),
    putMock: vi.fn(),
}));

vi.mock('./client', () => ({
    default: { get: getMock, put: putMock },
}));

const uploadImagesMock = vi.hoisted(() => vi.fn());
vi.mock('./image', () => ({ uploadImages: uploadImagesMock }));

const mockProfile = {
    memberId: 1,
    nickname: '닉네임',
    avatarUrl: 'https://avatar.url',
    bio: '소개글',
};

beforeEach(() => {
    getMock.mockClear();
    putMock.mockClear();
    uploadImagesMock.mockClear();
});

// ────────────────────────────────────────────
// fetchMyProfile
// ────────────────────────────────────────────

describe('fetchMyProfile', () => {
    it('GET /api/profiles/me 호출 후 프로필을 반환한다', async () => {
        getMock.mockResolvedValue({ data: mockProfile });

        const result = await fetchMyProfile();

        expect(result).toEqual(mockProfile);
        expect(getMock).toHaveBeenCalledWith('/api/profiles/me');
    });
});

// ────────────────────────────────────────────
// fetchProfile
// ────────────────────────────────────────────

describe('fetchProfile', () => {
    it('GET /api/profiles/:userId 호출 후 프로필을 반환한다', async () => {
        getMock.mockResolvedValue({ data: mockProfile });

        const result = await fetchProfile(1);

        expect(result).toEqual(mockProfile);
        expect(getMock).toHaveBeenCalledWith('/api/profiles/1');
    });

    it('서버 에러 시 예외가 전파된다', async () => {
        getMock.mockRejectedValue(new Error('Not Found'));

        await expect(fetchProfile(999)).rejects.toThrow('Not Found');
    });
});

// ────────────────────────────────────────────
// updateProfile
// ────────────────────────────────────────────

describe('updateProfile', () => {
    it('avatarFile 없을 때 이미지 업로드 없이 PUT 요청을 보낸다', async () => {
        putMock.mockResolvedValue({ data: { ...mockProfile, nickname: '새닉네임' } });

        const result = await updateProfile({ nickname: '새닉네임', bio: '새소개' });

        expect(uploadImagesMock).not.toHaveBeenCalled();
        expect(putMock).toHaveBeenCalledWith('/api/profiles/me', {
            nickname: '새닉네임',
            avatarUrl: undefined,
            bio: '새소개',
        });
        expect(result.nickname).toBe('새닉네임');
    });

    it('avatarFile이 있을 때 이미지를 먼저 업로드한 뒤 PUT 요청을 보낸다', async () => {
        const file = new File(['img'], 'avatar.png', { type: 'image/png' });
        uploadImagesMock.mockResolvedValue(['https://uploaded.url']);
        putMock.mockResolvedValue({ data: { ...mockProfile, avatarUrl: 'https://uploaded.url' } });

        const result = await updateProfile({ nickname: '닉네임', bio: '', avatarFile: file });

        expect(uploadImagesMock).toHaveBeenCalledWith([file]);
        expect(putMock).toHaveBeenCalledWith('/api/profiles/me', {
            nickname: '닉네임',
            avatarUrl: 'https://uploaded.url',
            bio: '',
        });
        expect(result.avatarUrl).toBe('https://uploaded.url');
    });

    it('이미지 업로드 실패 시 예외가 전파된다', async () => {
        const file = new File(['img'], 'avatar.png', { type: 'image/png' });
        uploadImagesMock.mockRejectedValue(new Error('Upload Failed'));

        await expect(updateProfile({ nickname: '닉네임', bio: '', avatarFile: file }))
            .rejects.toThrow('Upload Failed');

        expect(putMock).not.toHaveBeenCalled();
    });
});
