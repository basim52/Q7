import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

/** Shares a file via the Web Share API when supported (mobile → WhatsApp etc.), otherwise downloads it. */
export async function shareOrDownload(name: string, blob: Blob, title: string): Promise<'shared' | 'downloaded'> {
  const file = new File([blob], name, { type: blob.type })
  const nav = navigator as Navigator & { canShare?: (d: ShareData) => boolean }
  if (nav.share && nav.canShare?.({ files: [file] })) {
    try { await nav.share({ files: [file], title }); return 'shared' } catch (e) { if ((e as Error).name === 'AbortError') return 'shared' }
  }
  download(name, blob)
  return 'downloaded'
}

export function download(name: string, blob: Blob) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = name
  document.body.appendChild(a); a.click(); a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 2000)
}

/** Renders a DOM node (the quote card) to PNG; the browser shapes Arabic text correctly. */
export async function nodeToPng(node: HTMLElement): Promise<Blob> {
  const canvas = await html2canvas(node, { scale: 2, backgroundColor: '#ffffff' })
  return await new Promise<Blob>((res, rej) => canvas.toBlob(b => (b ? res(b) : rej(new Error('PNG failed'))), 'image/png'))
}

export async function nodeToPdf(node: HTMLElement): Promise<Blob> {
  const canvas = await html2canvas(node, { scale: 2, backgroundColor: '#ffffff' })
  const pdf = new jsPDF({ unit: 'pt', format: 'a4' })
  const pw = pdf.internal.pageSize.getWidth()
  const h = (canvas.height * pw) / canvas.width
  pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, pw, h)
  return pdf.output('blob')
}
