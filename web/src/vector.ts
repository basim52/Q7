// Ported from app/src/main/java/com/example/engine/SvgVectorEngine.kt
import { Pt, SOFTWARE, Software } from './models'

export const PRESETS = {
  GEAR_MANDALA: { en: 'Mechanical Gear Mandala', ar: 'ماندالا التروس الميكانيكية', threshold: 135 },
  WOLF_EMBLEM: { en: 'Geometric Wolf Crest', ar: 'شعار الذئب الهندسي', threshold: 120 },
  SPINDLE_BADGE: { en: 'Laser Workshop Seal', ar: 'ختم ورشة الليزر الصناعي', threshold: 140 },
  FILIGREE_COASTER: { en: 'Botanical Lattice Coaster', ar: 'قاعدة أكواب زخرفية مفرغة', threshold: 128 },
} as const
export type Preset = keyof typeof PRESETS

export interface VectorConfig { threshold: number; edgeDetection: boolean; invertColors: boolean; speckleSuppression: number; pathSmoothing: number; targetWidthMm: number; software: Software }
export const DEFAULT_VECTOR: VectorConfig = { threshold: 135, edgeDetection: true, invertColors: false, speckleSuppression: 2, pathSmoothing: 0.45, targetWidthMm: 120, software: 'LIGHTBURN' }
export interface VectorResult { previewDataUrl: string; contours: Pt[][]; nodeCount: number; loops: number; cutMm: number; seconds: number }

const PROCESS_SIZE = 160

export function createSampleArtwork(preset: Preset, size = 320): HTMLCanvasElement {
  const cv = document.createElement('canvas'); cv.width = cv.height = size
  const g = cv.getContext('2d')!
  g.fillStyle = '#fff'; g.fillRect(0, 0, size, size)
  g.strokeStyle = '#000'; g.fillStyle = '#000'; g.lineCap = 'round'; g.lineJoin = 'round'
  const cx = size / 2, cy = size / 2, r = size * 0.43
  const circle = (x: number, y: number, rad: number, mode: 'stroke' | 'fill' | 'clear') => {
    g.beginPath(); g.arc(x, y, rad, 0, Math.PI * 2)
    if (mode === 'stroke') g.stroke(); else { g.fillStyle = mode === 'clear' ? '#fff' : '#000'; g.fill(); g.fillStyle = '#000' }
  }
  const line = (a: number, b: number, c: number, d: number) => { g.beginPath(); g.moveTo(a, b); g.lineTo(c, d); g.stroke() }
  const poly = (pts: Pt[], mode: 'stroke' | 'fill') => { g.beginPath(); pts.forEach(([x, y], i) => i ? g.lineTo(x, y) : g.moveTo(x, y)); g.closePath(); mode === 'fill' ? g.fill() : g.stroke() }
  const C = Math.cos, S = Math.sin, PI = Math.PI
  if (preset === 'GEAR_MANDALA') {
    const teeth = 24, root = r * 0.86, pts: Pt[] = []
    for (let i = 0; i < teeth; i++) {
      const a = [i, i + 0.35, i + 0.5, i + 0.85].map(k => (k * 2 * PI) / teeth)
      pts.push([cx + root * C(a[0]), cy + root * S(a[0])], [cx + r * C(a[1]), cy + r * S(a[1])], [cx + r * C(a[2]), cy + r * S(a[2])], [cx + root * C(a[3]), cy + root * S(a[3])])
    }
    poly(pts, 'fill'); circle(cx, cy, r * 0.72, 'clear')
    g.lineWidth = size * 0.032; circle(cx, cy, r * 0.6, 'stroke')
    for (let i = 0; i < 8; i++) {
      const a = (i * PI) / 4
      line(cx, cy, cx + r * 0.72 * C(a), cy + r * 0.72 * S(a))
      circle(cx + r * 0.42 * C(a + PI / 8), cy + r * 0.42 * S(a + PI / 8), r * 0.11, 'stroke')
    }
    circle(cx, cy, r * 0.24, 'fill'); circle(cx, cy, r * 0.11, 'clear')
  } else if (preset === 'WOLF_EMBLEM') {
    g.lineWidth = size * 0.035
    poly([0, 1, 2, 3, 4, 5].map(i => { const a = (i * PI) / 3 - PI / 2; return [cx + r * C(a), cy + r * S(a)] as Pt }), 'stroke')
    g.lineWidth = size * 0.025
    const L: number[][] = [[-.5, -.15, -.38, -.65], [-.38, -.65, -.15, -.35], [.5, -.15, .38, -.65], [.38, -.65, .15, -.35], [-.15, -.35, .15, -.35],
      [-.5, -.15, -.56, .22], [.5, -.15, .56, .22], [-.56, .22, 0, .72], [.56, .22, 0, .72], [-.15, -.35, 0, .45], [.15, -.35, 0, .45],
      [-.5, -.15, -.18, .12], [.5, -.15, .18, .12], [-.18, .12, 0, .72], [.18, .12, 0, .72]]
    for (const s of L) line(cx + r * s[0], cy + r * s[1], cx + r * s[2], cy + r * s[3])
    poly([[cx - r * 0.14, cy + r * 0.44], [cx + r * 0.14, cy + r * 0.44], [cx, cy + r * 0.68]], 'fill')
  } else if (preset === 'SPINDLE_BADGE') {
    g.lineWidth = size * 0.036
    poly([0, 1, 2, 3, 4, 5, 6, 7].map(i => { const a = (i * PI) / 4 + PI / 8; return [cx + r * C(a), cy + r * S(a)] as Pt }), 'stroke')
    const ho = r * 0.68, hr = size * 0.035
    for (const [sx, sy] of [[-1, -1], [1, -1], [-1, 1], [1, 1]]) circle(cx + sx * ho, cy + sy * ho * 0.4, hr, 'fill')
    poly([[cx - r * .25, cy - r * .55], [cx + r * .25, cy - r * .55], [cx + r * .25, cy - r * .2], [cx + r * .08, cy + r * .08], [cx - r * .08, cy + r * .08], [cx - r * .25, cy - r * .2]], 'fill')
    g.lineWidth = size * 0.028
    line(cx, cy + r * .08, cx, cy + r * .55); line(cx, cy + r * .45, cx - r * .35, cy + r * .22)
    line(cx, cy + r * .45, cx + r * .35, cy + r * .22); line(cx - r * .45, cy + r * .55, cx + r * .45, cy + r * .55)
  } else {
    g.lineWidth = size * 0.032
    circle(cx, cy, r, 'stroke'); circle(cx, cy, r * 0.88, 'stroke')
    for (let i = 0; i < 12; i++) { const a = (i * 2 * PI) / 12; circle(cx + r * .46 * C(a), cy + r * .46 * S(a), r * .32, 'stroke') }
    circle(cx, cy, r * 0.16, 'fill')
  }
  return cv
}

export function processArtwork(source: CanvasImageSource, cfg: VectorConfig): VectorResult {
  const w = PROCESS_SIZE, h = PROCESS_SIZE
  const cv = document.createElement('canvas'); cv.width = w; cv.height = h
  const g = cv.getContext('2d', { willReadFrequently: true })!
  g.clearRect(0, 0, w, h); g.drawImage(source, 0, 0, w, h)
  const px = g.getImageData(0, 0, w, h).data
  const thr = Math.min(254, Math.max(1, cfg.threshold))
  const bin = new Uint8Array(w * h)
  for (let i = 0; i < w * h; i++) {
    const a = px[i * 4 + 3]
    const lum = a < 40 ? 255 : Math.round(0.299 * px[i * 4] + 0.587 * px[i * 4 + 1] + 0.114 * px[i * 4 + 2])
    let dark = lum < thr
    if (cfg.invertColors) dark = !dark
    bin[i] = dark ? 1 : 0
  }
  let clean = new Uint8Array(w * h)
  if (cfg.speckleSuppression > 0) {
    for (let y = 1; y < h - 1; y++) for (let x = 1; x < w - 1; x++) {
      let nb = 0
      for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) if ((dx || dy) && bin[(y + dy) * w + x + dx]) nb++
      clean[y * w + x] = bin[y * w + x] ? (nb >= Math.min(4, cfg.speckleSuppression) ? 1 : 0) : (nb >= 7 ? 1 : 0)
    }
  } else clean = bin.slice()
  const edge = new Uint8Array(w * h)
  for (let y = 1; y < h - 1; y++) for (let x = 1; x < w - 1; x++) {
    const i = y * w + x
    if (clean[i]) edge[i] = !clean[i - w] || !clean[i + w] || !clean[i - 1] || !clean[i + 1] ? 1 : 0
  }
  const out = g.createImageData(w, h)
  for (let i = 0; i < w * h; i++) {
    const c = cfg.edgeDetection && edge[i] ? [239, 68, 68] : !cfg.edgeDetection && clean[i] ? [15, 23, 42] : [248, 250, 252]
    out.data.set([c[0], c[1], c[2], 255], i * 4)
  }
  g.putImageData(out, 0, 0)
  const visited = new Uint8Array(w * h)
  const contours: Pt[][] = []
  const DX = [1, 1, 0, -1, -1, -1, 0, 1], DY = [0, 1, 1, 1, 0, -1, -1, -1]
  const stride = cfg.pathSmoothing > 0.6 ? 3 : cfg.pathSmoothing > 0.25 ? 2 : 1
  for (let y = 1; y < h - 1; y++) for (let x = 1; x < w - 1; x++) {
    if (!edge[y * w + x] || visited[y * w + x]) continue
    const chain: Pt[] = []
    let cx = x, cy = y
    for (let step = 0; step < 600; step++) {
      visited[cy * w + cx] = 1
      if (step % stride === 0) chain.push([cx / w, cy / h])
      let found = false
      for (let d = 0; d < 8; d++) {
        const nx = cx + DX[d], ny = cy + DY[d]
        if (nx >= 1 && nx < w - 1 && ny >= 1 && ny < h - 1 && edge[ny * w + nx] && !visited[ny * w + nx]) { cx = nx; cy = ny; found = true; break }
      }
      if (!found) break
    }
    if (chain.length >= 4) contours.push(chain)
  }
  let nodes = 0, len = 0
  for (const p of contours) { nodes += p.length; for (let i = 1; i < p.length; i++) len += Math.hypot(p[i][0] - p[i - 1][0], p[i][1] - p[i - 1][1]) }
  const cutMm = Math.max(12, len * cfg.targetWidthMm)
  return { previewDataUrl: cv.toDataURL(), contours, nodeCount: nodes, loops: contours.length, cutMm, seconds: Math.max(4, Math.round(cutMm / 22)) }
}

export function generateCleanSvg(r: VectorResult, cfg: VectorConfig, title: string): string {
  const s = cfg.targetWidthMm, sw = SOFTWARE[cfg.software]
  const o: string[] = []
  o.push('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
  o.push(`<!-- Generated by LaserForge Studio • Target: ${sw.en} -->`)
  o.push(`<!-- Artwork: ${title.replace(/--/g, '-')} | Loops: ${r.loops} | Nodes: ${r.nodeCount} -->`)
  o.push(`<svg xmlns="http://www.w3.org/2000/svg" width="${s.toFixed(2)}mm" height="${s.toFixed(2)}mm" viewBox="0 0 ${s.toFixed(2)} ${s.toFixed(2)}">`)
  o.push(`  <g id="laser_cut_layer" fill="none" stroke="${sw.color}" stroke-width="${sw.width.toFixed(3)}" stroke-linecap="round" stroke-linejoin="round">`)
  r.contours.forEach((c, idx) => {
    if (c.length < 2) return
    let d = c.map((p, i) => `${i ? ' L' : 'M'} ${(p[0] * s).toFixed(2)} ${(p[1] * s).toFixed(2)}`).join('')
    const f = c[0], l = c[c.length - 1]
    if (Math.hypot((f[0] - l[0]) * s, (f[1] - l[1]) * s) < 3.5) d += ' Z'
    o.push(`    <path id="path_${idx}" d="${d}" />`)
  })
  o.push('  </g>', '</svg>')
  return o.join('\n') + '\n'
}
