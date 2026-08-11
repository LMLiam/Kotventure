import * as yauzl from 'yauzl';

export async function countClassEntries(buffer: Buffer): Promise<number | null> {
  try {
    const zipfile = await yauzl.fromBufferPromise(buffer);
    let classes = 0;
    for await (const entry of zipfile.eachEntry()) {
      if (entry.fileName.endsWith('.class')) classes += 1;
    }
    return classes;
  } catch {
    return null;
  }
}
