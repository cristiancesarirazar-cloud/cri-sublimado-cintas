import { getStore } from '@netlify/blobs';
import { createHash, randomUUID } from 'node:crypto';

const store = () => getStore('cri-pines-sync');
const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status,
  headers: { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store' }
});
function roomPrefix(req) {
  const code = req.headers.get('x-room-code') || '';
  if (code.length < 8 || code.length > 128) return null;
  return createHash('sha256').update(code).digest('hex').slice(0, 32) + '/';
}

export default async (req) => {
  const prefix = roomPrefix(req);
  if (!prefix) return json({ error: 'Código compartido inválido. Debe tener entre 8 y 128 caracteres.' }, 401);
  const url = new URL(req.url);
  const action = url.searchParams.get('action');
  const blobs = store();

  try {
    if (action === 'upload' && req.method === 'POST') {
      const form = await req.formData();
      const file = form.get('file');
      if (!(file instanceof File) || !file.type.startsWith('image/')) return json({ error: 'Enviá un archivo de imagen.' }, 400);
      if (file.size > 15 * 1024 * 1024) return json({ error: 'La imagen supera el límite de 15 MB.' }, 413);
      const key = prefix + randomUUID();
      await blobs.set(key, file, { metadata: { name: String(file.name || 'foto.jpg').slice(0, 180), type: file.type, size: file.size, createdAt: new Date().toISOString() } });
      return json({ key: key.slice(prefix.length), name: String(file.name || 'foto.jpg') });
    }

    if (action === 'list' && req.method === 'GET') {
      const { blobs: entries } = await blobs.list({ prefix });
      const items = [];
      for (const entry of entries) {
        const meta = await blobs.getMetadata(entry.key);
        items.push({ key: entry.key.slice(prefix.length), name: meta?.metadata?.name || 'foto-sin-nombre.jpg', createdAt: meta?.metadata?.createdAt || '' });
      }
      items.sort((a, b) => a.createdAt.localeCompare(b.createdAt));
      return json({ items });
    }

    if (action === 'download' && req.method === 'GET') {
      const id = url.searchParams.get('key') || '';
      if (!/^[0-9a-f-]{36}$/i.test(id)) return json({ error: 'Identificador inválido.' }, 400);
      const key = prefix + id;
      const meta = await blobs.getMetadata(key);
      if (!meta) return json({ error: 'No se encontró la foto.' }, 404);
      const data = await blobs.get(key, { type: 'blob' });
      if (!data) return json({ error: 'No se encontró la foto.' }, 404);
      return new Response(data, { headers: { 'content-type': meta.metadata?.type || 'application/octet-stream', 'cache-control': 'no-store' } });
    }

    return json({ error: 'Acción no válida.' }, 400);
  } catch (error) {
    console.error('cri-pines-sync:', error);
    return json({ error: 'Error interno de sincronización.' }, 500);
  }
};
