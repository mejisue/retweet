import apiClient from './client';

export async function uploadImages(files: File[]): Promise<string[]> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    const { data } = await apiClient.post('/api/images', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data.urls;
}
