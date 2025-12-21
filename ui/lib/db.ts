import { openDB, DBSchema, IDBPDatabase } from 'idb';

interface UploadRecord {
  uploadId: string;
  file: File;
  chunkSize: number;
  progress: number; // 0-100
  status: 'PENDING' | 'UPLOADING' | 'PAUSED' | 'COMPLETED' | 'ERROR';
  createdAt: number;
}

interface PickboxDB extends DBSchema {
  uploads: {
    key: string;
    value: UploadRecord;
  };
}

const DB_NAME = 'pickbox-uploads';
const DB_VERSION = 1;

let dbPromise: Promise<IDBPDatabase<PickboxDB>> | null = null;

export const getDB = () => {
  if (!dbPromise) {
    dbPromise = openDB<PickboxDB>(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('uploads')) {
          db.createObjectStore('uploads', { keyPath: 'uploadId' });
        }
      },
    });
  }
  return dbPromise;
};

export const addUpload = async (upload: UploadRecord) => {
  const db = await getDB();
  return db.put('uploads', upload);
};

export const getUpload = async (uploadId: string) => {
  const db = await getDB();
  return db.get('uploads', uploadId);
};

export const getAllUploads = async () => {
  const db = await getDB();
  return db.getAll('uploads');
};

export const updateUpload = async (upload: UploadRecord) => {
  const db = await getDB();
  return db.put('uploads', upload);
};

export const deleteUpload = async (uploadId: string) => {
  const db = await getDB();
  return db.delete('uploads', uploadId);
};
