// Ported from app/src/main/java/com/example/engine/BoxSvgGenerator.kt
import { BoxConfig, BoxLayout, Panel, Pt, clamp } from './models'

const rect = (w: number, h: number): Pt[] => [[0, 0], [w, 0], [w, h], [0, h], [0, 0]]
const mm = (a: number, b: number) => `${Math.trunc(a)}×${Math.trunc(b)}mm`

function fingerOutline(width: number, height: number, t: number, kerf: number, topOut: boolean, topFlat: boolean,
  bottomOut: boolean, leftOut: boolean, rightOut: boolean, segX: number, segY: number): Pt[] {
  const pts: Pt[] = []
  const sx = width / segX, sy = height / segY, hk = kerf / 2
  const cx = (v: number) => clamp(v, t, width - t), cy = (v: number) => clamp(v, t, height - t)
  pts.push([t, t])
  if (topFlat) pts.push([width - t, t])
  else {
    for (let i = 0; i < segX; i++) {
      const tab = topOut ? i % 2 === 1 : i % 2 === 0
      const y = tab ? 0 : t
      pts.push([cx(i * sx) - (tab ? hk : 0), y], [cx((i + 1) * sx) + (tab ? hk : 0), y])
    }
    pts.push([width - t, t])
  }
  for (let i = 0; i < segY; i++) {
    const tab = rightOut ? i % 2 === 1 : i % 2 === 0
    const x = tab ? width : width - t
    pts.push([x, cy(i * sy) - (tab ? hk : 0)], [x, cy((i + 1) * sy) + (tab ? hk : 0)])
  }
  pts.push([width - t, height - t])
  for (let i = segX - 1; i >= 0; i--) {
    const tab = bottomOut ? i % 2 === 1 : i % 2 === 0
    const y = tab ? height : height - t
    pts.push([cx((i + 1) * sx) + (tab ? hk : 0), y], [cx(i * sx) - (tab ? hk : 0), y])
  }
  pts.push([t, height - t])
  for (let i = segY - 1; i >= 0; i--) {
    const tab = leftOut ? i % 2 === 1 : i % 2 === 0
    const x = tab ? 0 : t
    pts.push([x, cy((i + 1) * sy) + (tab ? hk : 0)], [x, cy(i * sy) - (tab ? hk : 0)])
  }
  pts.push([t, t])
  return pts
}

function phoneStand(w: number, h: number, d: number, t: number, sp: number): Panel[] {
  const lipH = clamp(h * 0.18, 10, 25)
  const side: Pt[] = [[0, h], [d, h], [d * 0.42, 0], [d * 0.42 - t * 2.5, 0], [d * 0.18, h - lipH], [d * 0.18, h - lipH - t],
    [d * 0.08, h - lipH - t], [d * 0.08, h - lipH], [0, h - lipH], [0, h]]
  const restH = Math.max(h * 0.75, 20), row2 = sp * 2 + h
  return [
    { id: 'stand_left', label: 'LEFT CHEEK', dims: mm(d, h), x: sp, y: sp, w: d, h, pts: side },
    { id: 'stand_right', label: 'RIGHT CHEEK', dims: mm(d, h), x: sp + d + sp, y: sp, w: d, h, pts: side },
    { id: 'stand_back', label: 'BACK REST', dims: mm(w, restH), x: sp, y: row2, w, h: restH, pts: rect(w, restH) },
    { id: 'stand_lip', label: 'FRONT LIP', dims: mm(w, lipH), x: sp * 2 + w, y: row2, w, h: lipH, pts: rect(w, lipH) },
  ]
}

function pictureFrame(w: number, h: number, t: number, sp: number): Panel[] {
  const b = clamp(Math.min(w, h) * 0.15, 12, 40)
  const top: Pt[] = [[0, 0], [w, 0], [w - b, b], [b, b], [0, 0]]
  const side: Pt[] = [[0, 0], [b, b], [b, h - b], [0, h], [0, 0]]
  const row2 = sp * 2 + h
  const legW = Math.max(w * 0.25, 20), legH = h * 0.6
  const P = (id: string, label: string, pw: number, ph: number, x: number, y: number, pts = rect(pw, ph)): Panel => ({ id, label, dims: mm(pw, ph), x, y, w: pw, h: ph, pts })
  return [
    P('frame_top', 'FRAME TOP', w, b, sp, sp, top),
    P('frame_bottom', 'FRAME BOTTOM', w, b, sp, sp * 2 + b, top),
    P('frame_left', 'FRAME LEFT', b, h, sp * 2 + w, sp, side),
    P('frame_right', 'FRAME RIGHT', b, h, sp * 3 + w + b, sp, side),
    P('frame_back', 'BACK PLATE', w - t, h - t, sp, row2),
    P('frame_leg', 'EASEL LEG', legW, legH, sp * 2 + w, row2, [[0, 0], [legW, 0], [legW * 0.7, legH], [legW * 0.3, legH], [0, 0]]),
  ]
}

function summarize(panels: Panel[], sp: number, w: number, h: number, d: number, t: number, tabs: number): BoxLayout {
  let mx = 0, my = 0, per = 0
  for (const p of panels) {
    mx = Math.max(mx, p.x + p.w + sp, p.x + Math.max(...p.pts.map(q => q[0])) + sp)
    my = Math.max(my, p.y + p.h + sp, p.y + Math.max(...p.pts.map(q => q[1])) + sp)
    for (let i = 1; i < p.pts.length; i++) per += Math.hypot(p.pts[i][0] - p.pts[i - 1][0], p.pts[i][1] - p.pts[i - 1][1])
  }
  const vol = (Math.max(1, w - 2 * t) * Math.max(1, h - 2 * t) * Math.max(1, d - 2 * t)) / 1000
  return { panels, sheetW: mx, sheetH: my, cutM: per / 1000, tabs, volumeCm3: vol }
}

export function generateBoxLayout(c: BoxConfig): BoxLayout {
  const w = clamp(c.widthMm, 30, 500), h = clamp(c.heightMm, 25, 400), d = clamp(c.depthMm, 30, 500)
  const t = clamp(c.thicknessMm, 1.5, 15), tab = clamp(c.tabSizeMm, 6, 60), kerf = clamp(c.kerfMm, 0, 0.5)
  const sp = 10
  if (c.style === 'PHONE_STAND') return summarize(phoneStand(w, h, d, t, sp), sp, w, h, d, t, 0)
  if (c.style === 'PICTURE_FRAME') return summarize(pictureFrame(w, h, t, sp), sp, w, h, t * 3, 0, 0)
  const openTop = c.style === 'OPEN_TOP_TRAY' || c.style === 'DRAWER'
  const odd = (len: number) => { const r = Math.max(3, Math.round(len / tab)); return r % 2 === 0 ? r + 1 : r }
  const segW = odd(w), segH = odd(h), segD = odd(d)
  const tabs = (segW + segH + segD) * 4
  const panels: Panel[] = []
  const wall = (pw: number, side: boolean, sx: number) => fingerOutline(pw, h, t, kerf, !openTop, openTop, true, !side, !side, sx, segH)
  panels.push({ id: 'front', label: 'FRONT PANEL', dims: mm(w, h), x: sp, y: sp, w, h, pts: wall(w, false, segW) })
  panels.push({ id: 'back', label: 'BACK PANEL', dims: mm(w, h), x: sp * 2 + w, y: sp, w, h, pts: wall(w, false, segW) })
  panels.push({ id: 'bottom', label: 'BOTTOM BASE', dims: mm(w, d), x: sp * 3 + w * 2, y: sp, w, h: d, pts: fingerOutline(w, d, t, kerf, false, false, false, false, false, segW, segD) })
  const row2 = sp * 2 + Math.max(h, d)
  panels.push({ id: 'left', label: 'LEFT WALL', dims: mm(d, h), x: sp, y: row2, w: d, h, pts: wall(d, true, segD) })
  panels.push({ id: 'right', label: 'RIGHT WALL', dims: mm(d, h), x: sp * 2 + d, y: row2, w: d, h, pts: wall(d, true, segD) })
  if (c.style === 'CLOSED_SIX_PANEL') {
    panels.push({ id: 'top', label: 'TOP LID', dims: mm(w, d), x: sp * 3 + d * 2, y: row2, w, h: d, pts: fingerOutline(w, d, t, kerf, false, false, false, false, false, segW, segD) })
  } else if (c.style === 'DIVIDED_TRAY') {
    panels.push({ id: 'divider', label: 'CENTER DIVIDER', dims: mm(d - 2 * t, h - t), x: sp * 3 + d * 2, y: row2, w: d - 2 * t, h: h - t,
      pts: fingerOutline(d - 2 * t, h - t, t, kerf, false, true, true, true, true, segD, segH) })
  } else if (c.style === 'DRAWER') {
    const fw = w + 2 * t, fh = h + t
    panels.push({ id: 'drawer_face', label: 'DRAWER FACE', dims: mm(fw, fh), x: sp * 3 + d * 2, y: row2, w: fw, h: fh, pts: rect(fw, fh) })
  }
  return summarize(panels, sp, w, h, d, t, tabs)
}

const f3 = (v: number) => v.toFixed(3)
export function exportBoxSvg(c: BoxConfig, L: BoxLayout): string {
  const out: string[] = []
  out.push('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
  out.push(`<!-- Generated by LaserForge Studio • Box ${Math.trunc(c.widthMm)}x${Math.trunc(c.heightMm)}x${Math.trunc(c.depthMm)}mm | Thickness: ${c.thicknessMm}mm | Tab: ${c.tabSizeMm}mm | Kerf: ${c.kerfMm}mm -->`)
  out.push(`<svg xmlns="http://www.w3.org/2000/svg" width="${L.sheetW.toFixed(2)}mm" height="${L.sheetH.toFixed(2)}mm" viewBox="0 0 ${L.sheetW.toFixed(2)} ${L.sheetH.toFixed(2)}">`)
  out.push('  <g id="CUT_PANELS" fill="none" stroke="#FF0000" stroke-width="0.08" stroke-linejoin="miter">')
  for (const p of L.panels) {
    const d = p.pts.map((q, i) => `${i ? ' L' : 'M'} ${f3(p.x + q[0])} ${f3(p.y + q[1])}`).join('') + ' Z'
    out.push(`    <path id="panel_${p.id}" d="${d}" />`)
  }
  out.push('  </g>')
  out.push('  <g id="SCORE_LABELS" fill="#0066FF" font-family="monospace" font-size="4.5" text-anchor="middle">')
  for (const p of L.panels) out.push(`    <text x="${(p.x + p.w / 2).toFixed(2)}" y="${(p.y + p.h / 2).toFixed(2)}">${p.label} (${p.dims})</text>`)
  out.push('  </g>', '</svg>')
  return out.join('\n') + '\n'
}

export function exportBoxDxf(c: BoxConfig, L: BoxLayout): string {
  const s: string[] = []
  const pair = (code: number, v: string) => s.push(String(code), v)
  const n = (v: number) => v.toFixed(4)
  const H = L.sheetH
  pair(999, `LaserForge Studio ${c.style} ${Math.trunc(c.widthMm)}x${Math.trunc(c.heightMm)}x${Math.trunc(c.depthMm)}mm t=${c.thicknessMm}`)
  pair(0, 'SECTION'); pair(2, 'HEADER')
  pair(9, '$ACADVER'); pair(1, 'AC1009')
  pair(9, '$INSUNITS'); pair(70, '4')
  pair(9, '$EXTMIN'); pair(10, '0.0'); pair(20, '0.0')
  pair(9, '$EXTMAX'); pair(10, n(L.sheetW)); pair(20, n(H))
  pair(0, 'ENDSEC')
  pair(0, 'SECTION'); pair(2, 'TABLES')
  pair(0, 'TABLE'); pair(2, 'LAYER'); pair(70, '1')
  pair(0, 'LAYER'); pair(2, 'CUT'); pair(70, '0'); pair(62, '1'); pair(6, 'CONTINUOUS')
  pair(0, 'ENDTAB'); pair(0, 'ENDSEC')
  pair(0, 'SECTION'); pair(2, 'ENTITIES')
  for (const p of L.panels) for (let i = 1; i < p.pts.length; i++) {
    const x0 = p.x + p.pts[i - 1][0], y0 = H - (p.y + p.pts[i - 1][1]), x1 = p.x + p.pts[i][0], y1 = H - (p.y + p.pts[i][1])
    if (Math.abs(x0 - x1) < 1e-4 && Math.abs(y0 - y1) < 1e-4) continue
    pair(0, 'LINE'); pair(8, 'CUT')
    pair(10, n(x0)); pair(20, n(y0)); pair(30, '0.0'); pair(11, n(x1)); pair(21, n(y1)); pair(31, '0.0')
  }
  pair(0, 'ENDSEC'); pair(0, 'EOF')
  return s.join('\n') + '\n'
}
