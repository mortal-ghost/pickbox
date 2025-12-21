import { API_BASE_URL } from '@/lib/constants';
import { ItemDto } from '@/lib/types';
import Cookies from 'js-cookie';

class FileService {
    private getHeaders() {
        const token = Cookies.get('token');
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        };
    }

    async listFiles(parentId?: string): Promise<ItemDto[]> {
        const endpoint = parentId
            ? `${API_BASE_URL}/files/${parentId}/list`
            : `${API_BASE_URL}/files/list`;

        const res = await fetch(endpoint, {
            headers: this.getHeaders()
        });

        if (!res.ok) throw new Error("Failed to fetch files");
        return await res.json();
    }

    async createFolder(name: string, parentId?: string | null): Promise<ItemDto> {
        const res = await fetch(`${API_BASE_URL}/files/folder`, {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify({
                name,
                parentId: parentId || null
            })
        });

        if (!res.ok) throw new Error("Failed to create folder");
        return await res.json();
    }
}

export const fileService = new FileService();
