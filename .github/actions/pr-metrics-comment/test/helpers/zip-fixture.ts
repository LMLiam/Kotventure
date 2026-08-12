import AdmZip from 'adm-zip';

export function buildZip(names: string[]): Buffer {
  const zip = new AdmZip();
  for (const name of names) {
    zip.addFile(name, Buffer.alloc(0));
  }
  return zip.toBuffer();
}
