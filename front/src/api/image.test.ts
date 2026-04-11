import { describe, it, expect, vi, beforeEach } from 'vitest';
import { uploadImages } from './image';

const { postMock } = vi.hoisted(() => ({ postMock: vi.fn() }));

vi.mock('./client', () => ({
    default: { post: postMock },
}));

beforeEach(() => {
    postMock.mockClear();
});

describe('uploadImages', () => {
    it('파일 목록을 FormData로 묶어 POST /api/images 호출하고 urls를 반환한다', async () => {
        const files = [
            new File(['img1'], 'photo1.jpg', { type: 'image/jpeg' }),
            new File(['img2'], 'photo2.png', { type: 'image/png' }),
        ];
        const mockUrls = [
            'https://cdn.cloudfront.net/posts/1/uuid1.jpg',
            'https://cdn.cloudfront.net/posts/1/uuid2.png',
        ];
        postMock.mockResolvedValue({ data: { urls: mockUrls } });

        const result = await uploadImages(files);

        expect(result).toEqual(mockUrls);

        const [url, formData, config] = postMock.mock.calls[0];
        expect(url).toBe('/api/images');
        expect(formData).toBeInstanceOf(FormData);
        expect(config.headers['Content-Type']).toBe('multipart/form-data');

        const entries = [...(formData as FormData).getAll('files')];
        expect(entries).toHaveLength(2);
        expect((entries[0] as File).name).toBe('photo1.jpg');
        expect((entries[1] as File).name).toBe('photo2.png');
    });

    it('파일이 1개여도 정상 동작한다', async () => {
        const files = [new File(['img'], 'single.webp', { type: 'image/webp' })];
        postMock.mockResolvedValue({ data: { urls: ['https://cdn.cloudfront.net/posts/1/uuid.webp'] } });

        const result = await uploadImages(files);

        expect(result).toHaveLength(1);
        expect(result[0]).toBe('https://cdn.cloudfront.net/posts/1/uuid.webp');
    });

    it('서버 에러 시 예외가 전파된다', async () => {
        const files = [new File(['img'], 'photo.jpg', { type: 'image/jpeg' })];
        postMock.mockRejectedValue(new Error('Upload failed'));

        await expect(uploadImages(files)).rejects.toThrow('Upload failed');
    });
});
