import { useEffect, useMemo, useRef, useState } from 'react'
import { useStored } from './store'
import { BoxConfig, CostConfig, CURRENCIES, DEFAULT_COST, ExportItem, fmtMoney, LASER_TYPES, LaserType, MATERIALS, Quote, SOFTWARE, Software, STYLES, EnclosureStyle, currencyOf } from './models'
import { calculatePricing } from './pricing'
import { exportBoxDxf, exportBoxSvg, generateBoxLayout } from './box'
import { createSampleArtwork, DEFAULT_VECTOR, generateCleanSvg, PRESETS, Preset, processArtwork, VectorConfig } from './vector'
import { download, nodeToPdf, nodeToPng, shareOrDownload } from './share'

type Tool = 'vector' | 'box' | 'cost'
const uid = () => Math.random().toString(36).slice(2) + Date.now().toString(36)
const nowLabel = () => { const d = new Date(); const p = (n: number) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}` }
const DEFAULT_BOX: BoxConfig = { widthMm: 120, heightMm: 75, depthMm: 90, thicknessMm: 3, tabSizeMm: 15, kerfMm: 0.12, style: 'CLOSED_SIX_PANEL' }

export default function App() {
  const [ar, setAr] = useStored('ar', true)
  const [tool, setTool] = useState<Tool>('vector')
  const [workshop, setWorkshop] = useStored('workshop', '')
  const [currency, setCurrency] = useStored('currency', 'SAR')
  const [quotes, setQuotes] = useStored<Quote[]>('quotes', [])
  const [history, setHistory] = useStored<ExportItem[]>('history', [])
  const [cost, setCost] = useStored<CostConfig>('cost', DEFAULT_COST)
  const [box, setBox] = useStored<BoxConfig>('box', DEFAULT_BOX)
  const [showSettings, setShowSettings] = useState(false)
  const [modal, setModal] = useState<ExportItem | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const T = (a: string, e: string) => (ar ? a : e)

  useEffect(() => { document.documentElement.dir = ar ? 'rtl' : 'ltr'; document.documentElement.lang = ar ? 'ar' : 'en' }, [ar])
  useEffect(() => { if (toast) { const t = setTimeout(() => setToast(null), 3500); return () => clearTimeout(t) } }, [toast])

  const addExport = (item: ExportItem) => { setHistory(h => [item, ...h].slice(0, 25)); setModal(item) }
  const boxLayout = useMemo(() => generateBoxLayout(box), [box])

  return (
    <>
      <header className="header">
        <div className="brand">
          <div className="logo">⚡</div>
          <div><h1>{T('ليزر فورج', 'LASERFORGE')}<span className="tag">CAD•CAM</span></h1>
            <small>{workshop || T('اضغط ⚙ لإضافة اسم ورشتك', 'Tap ⚙ to add your workshop name')}</small></div>
        </div>
        <div className="row" style={{ flex: '0 0 auto' }}>
          <button className="pill" onClick={() => setAr(!ar)} data-testid="lang">🌐 {ar ? 'EN' : 'عربي'}</button>
          <button className="icon-btn" onClick={() => setShowSettings(true)} title={T('إعدادات الورشة', 'Workshop settings')} data-testid="settings">⚙</button>
        </div>
      </header>
      <nav className="tabs">
        {([['vector', 'محول الفيكتور', 'Vectorizer'], ['box', 'مولد الصناديق', 'Box CAD'], ['cost', 'حاسبة التسعير', 'Pricing']] as const).map(([k, a, e]) =>
          <button key={k} className={'tab' + (tool === k ? ' on' : '')} onClick={() => setTool(k)} data-testid={'tab-' + k}>{T(a, e)}</button>)}
      </nav>
      <main>
        {tool === 'vector' && <VectorTool ar={ar} onExport={addExport} onSync={(name, w, m) => { setCost(c => ({ ...c, projectName: ar ? `قص فيكتور: ${name}` : `${name} Vector Cut`, itemWidthMm: w, itemLengthMm: w, cutLengthMeters: Math.max(0.15, Math.trunc(m * 100) / 100) })); setTool('cost'); setToast(T('تم استيراد بيانات الفيكتور إلى حاسبة التكلفة', 'Vector job synced into Cost Calculator')) }} />}
        {tool === 'box' && <BoxTool ar={ar} box={box} setBox={setBox} layout={boxLayout} onExport={addExport} onSync={() => {
          setCost(c => ({ ...c, projectName: ar ? `${STYLES[box.style].ar} (${box.widthMm}×${box.heightMm}×${box.depthMm} مم)` : `${STYLES[box.style].en} (${box.widthMm}×${box.heightMm}×${box.depthMm}mm)`, itemWidthMm: Math.ceil(boxLayout.sheetW), itemLengthMm: Math.ceil(boxLayout.sheetH), cutLengthMeters: Math.max(0.2, Math.trunc(boxLayout.cutM * 100) / 100) }))
          setTool('cost'); setToast(T('تم استيراد أبعاد الصندوق إلى حاسبة التكلفة', 'Box layout synced into Cost Calculator'))
        }} />}
        {tool === 'cost' && <CostTool ar={ar} cost={cost} setCost={setCost} currency={currency} workshop={workshop} quotes={quotes} setQuotes={setQuotes} toast={setToast} />}
      </main>
      {showSettings && <Settings ar={ar} workshop={workshop} setWorkshop={setWorkshop} currency={currency} setCurrency={setCurrency} history={history} clear={() => setHistory([])} open={i => { setShowSettings(false); setModal(i) }} close={() => setShowSettings(false)} />}
      {modal && <ExportModal ar={ar} item={modal} close={() => setModal(null)} toast={setToast} />}
      {toast && <div className="toast">{toast}</div>}
    </>
  )
}

function Banner({ badge, title, sub }: { badge: string; title: string; sub: string }) {
  return <div className="banner"><span className="badge">{badge}</span><h2>{title}</h2><p>{sub}</p></div>
}
function Num({ label, value, onChange, step = 1, testid }: { label: string; value: number; onChange: (v: number) => void; step?: number; testid?: string }) {
  const [txt, setTxt] = useState(String(value))
  useEffect(() => { if (parseFloat(txt) !== value) setTxt(String(value)) }, [value]) // eslint-disable-line
  return <label className="f">{label}<input type="number" inputMode="decimal" step={step} min={0} value={txt} data-testid={testid}
    onChange={e => { setTxt(e.target.value); const v = parseFloat(e.target.value); if (!isNaN(v) && v >= 0) onChange(v) }} /></label>
}
function Slider({ label, value, min, max, step = 1, show, onChange, testid }: { label: string; value: number; min: number; max: number; step?: number; show: string; onChange: (v: number) => void; testid?: string }) {
  return <div><div className="row" style={{ justifyContent: 'space-between' }}><span style={{ fontSize: 13 }}>{label}</span><b style={{ flex: '0 0 auto', color: 'var(--cyan)', fontSize: 13 }}>{show}</b></div>
    <input type="range" min={min} max={max} step={step} value={value} data-testid={testid} onChange={e => onChange(parseFloat(e.target.value))} /></div>
}

// ---------------- Tool 1: Vector ----------------
function VectorTool({ ar, onExport, onSync }: { ar: boolean; onExport: (i: ExportItem) => void; onSync: (name: string, w: number, m: number) => void }) {
  const T = (a: string, e: string) => (ar ? a : e)
  const [preset, setPreset] = useState<Preset>('GEAR_MANDALA')
  const [img, setImg] = useState<{ el: HTMLImageElement; name: string } | null>(null)
  const [cfg, setCfg] = useState<VectorConfig>(DEFAULT_VECTOR)
  const fileRef = useRef<HTMLInputElement>(null)
  const result = useMemo(() => processArtwork(img ? img.el : createSampleArtwork(preset), cfg), [img, preset, cfg])
  const title = img ? img.name : (ar ? PRESETS[preset].ar : PRESETS[preset].en)
  const up = (p: Partial<VectorConfig>) => setCfg(c => ({ ...c, ...p }))
  const onFile = (f: File) => { const url = URL.createObjectURL(f); const el = new Image(); el.onload = () => setImg({ el, name: f.name }); el.src = url }
  const gen = () => {
    const svg = generateCleanSvg(result, cfg, img ? img.name : PRESETS[preset].en)
    const safe = (img ? img.name.replace(/\.[^.]+$/, '') : PRESETS[preset].en).toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '') || 'laser_vector'
    onExport({ id: Math.random().toString(36).slice(2), title: `${title} (${ar ? SOFTWARE[cfg.software].ar : SOFTWARE[cfg.software].en})`, toolEn: 'Vector Optimizer', toolAr: 'محول الفيكتور', fileName: `${safe}_cut.svg`, time: nowLabel(), svg })
  }
  return <>
    <Banner badge={T('الأداة 01 • منصة قص الليزر والـ CNC', 'TOOL 01 • CNC & LASER WORKBENCH')} title={T('محول ومحسن الصور إلى فيكتور', 'Vector & Image Optimizer')} sub={T('ارفع صورة أو اختر نموذجاً، اضبط العتبة والتنعيم، ثم صدّر ملف SVG جاهز للقص.', 'Upload artwork or pick a sample, tune threshold & smoothing, export a cut-ready SVG.')} />
    <div className="card">
      <div className="lbl">{T('نماذج جاهزة للتجربة:', 'SAMPLE ARTWORK:')}</div>
      <div className="chips">{(Object.keys(PRESETS) as Preset[]).map(p => <button key={p} className={'chip' + (!img && preset === p ? ' on' : '')} onClick={() => { setImg(null); setPreset(p); up({ threshold: PRESETS[p].threshold }) }}>{ar ? PRESETS[p].ar : PRESETS[p].en}</button>)}
        <button className="chip" onClick={() => fileRef.current?.click()} data-testid="upload">⬆ {T('رفع صورة', 'Upload image')}</button>
        <input ref={fileRef} type="file" accept="image/*" hidden onChange={e => e.target.files?.[0] && onFile(e.target.files[0])} data-testid="file" /></div>
    </div>
    <div className="grid2">
      <div className="card">
        <img className="preview" src={result.previewDataUrl} alt="preview" />
        <div className="muted" style={{ margin: '6px 0' }}>{title} • {cfg.targetWidthMm}×{cfg.targetWidthMm}mm</div>
        <div className="stats">
          <div className="stat"><b>{result.loops}</b><span>{T('مسارات', 'Paths')}</span></div>
          <div className="stat"><b>{result.nodeCount}</b><span>{T('نقاط', 'Nodes')}</span></div>
          <div className="stat"><b>{(result.cutMm / 1000).toFixed(2)}m</b><span>{T('طول القص', 'Cut')}</span></div>
          <div className="stat"><b>{result.seconds}s</b><span>{T('وقت تقديري', 'Est. time')}</span></div>
        </div>
      </div>
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <Slider label={T('عتبة التباين', 'Threshold')} value={cfg.threshold} min={20} max={235} show={`${cfg.threshold} / 255`} onChange={v => up({ threshold: v })} testid="threshold" />
        <Slider label={T('إزالة النقاط الشاردة', 'Speckle suppression')} value={cfg.speckleSuppression} min={0} max={4} show={String(cfg.speckleSuppression)} onChange={v => up({ speckleSuppression: v })} />
        <Slider label={T('تنعيم المسار', 'Path smoothing')} value={cfg.pathSmoothing} min={0} max={1} step={0.05} show={`${Math.round(cfg.pathSmoothing * 100)}%`} onChange={v => up({ pathSmoothing: v })} />
        <Slider label={T('العرض المستهدف', 'Target width')} value={cfg.targetWidthMm} min={20} max={400} show={`${cfg.targetWidthMm} mm`} onChange={v => up({ targetWidthMm: v })} />
        <div className="chips">
          <button className={'chip' + (cfg.edgeDetection ? ' on' : '')} onClick={() => up({ edgeDetection: !cfg.edgeDetection })}>{T('كشف الحواف', 'Edge detection')}</button>
          <button className={'chip' + (cfg.invertColors ? ' on' : '')} onClick={() => up({ invertColors: !cfg.invertColors })}>{T('عكس الألوان', 'Invert')}</button>
        </div>
        <div className="lbl">{T('برنامج القص المستهدف:', 'TARGET SOFTWARE:')}</div>
        <div className="chips">{(Object.keys(SOFTWARE) as Software[]).map(s => <button key={s} className={'chip' + (cfg.software === s ? ' on' : '')} onClick={() => up({ software: s })}>{ar ? SOFTWARE[s].ar : SOFTWARE[s].en}</button>)}</div>
        <button className="btn" onClick={gen} data-testid="gen-svg">✨ {T('توليد وتصدير ملف الفيكتور SVG', 'Generate SVG / Vector')}</button>
        <button className="btn ghost" onClick={() => onSync(title, cfg.targetWidthMm, result.cutMm / 1000)}>🧮 {T('احسب تكلفة هذا التصميم', 'Calculate cost for this design')}</button>
      </div>
    </div>
  </>
}

// ---------------- Tool 2: Box ----------------
function BoxTool({ ar, box, setBox, layout, onExport, onSync }: { ar: boolean; box: BoxConfig; setBox: (f: (b: BoxConfig) => BoxConfig) => void; layout: ReturnType<typeof generateBoxLayout>; onExport: (i: ExportItem) => void; onSync: () => void }) {
  const T = (a: string, e: string) => (ar ? a : e)
  const up = (p: Partial<BoxConfig>) => setBox(b => ({ ...b, ...p }))
  const exp = () => {
    const prefix = box.style === 'DRAWER' ? 'drawer' : box.style === 'PHONE_STAND' ? 'phone_stand' : box.style === 'PICTURE_FRAME' ? 'frame' : 'box'
    const dims = `${Math.trunc(box.widthMm)}x${Math.trunc(box.heightMm)}x${Math.trunc(box.depthMm)}mm`
    onExport({ id: Math.random().toString(36).slice(2), title: `${ar ? STYLES[box.style].ar : STYLES[box.style].en} ${dims}`, toolEn: 'Box Generator', toolAr: 'مولد الصناديق', fileName: `${prefix}_${dims}_${box.thicknessMm}mm.svg`, time: nowLabel(), svg: exportBoxSvg(box, layout), dxf: exportBoxDxf(box, layout) })
  }
  const vb = `0 0 ${layout.sheetW.toFixed(1)} ${layout.sheetH.toFixed(1)}`
  return <>
    <Banner badge={T('الأداة 02 • منصة قص الليزر والـ CNC', 'TOOL 02 • CNC & LASER WORKBENCH')} title={T('مولد الصناديق والتعشيق بالليزر', 'Box & Enclosure Generator')} sub={T('صناديق بتعشيق الأصابع، درج، حامل جوال وبرواز — تصدير SVG و DXF بالمليمتر.', 'Finger-joint boxes, drawer, phone stand & frame — export SVG + DXF in mm.')} />
    <div className="card">
      <div className="lbl">{T('الخامة:', 'MATERIAL:')}</div>
      <div className="chips">{MATERIALS.map(m => <button key={m.en} className={'chip' + (box.thicknessMm === m.t ? ' on' : '')} onClick={() => up({ thicknessMm: m.t })}>{ar ? m.ar : m.en}</button>)}</div>
      <div className="lbl">{T('القالب / نمط التصميم:', 'TEMPLATE / TOPOLOGY:')}</div>
      <div className="chips">{(Object.keys(STYLES) as EnclosureStyle[]).map(s => <button key={s} data-testid={'style-' + s} className={'chip' + (box.style === s ? ' on' : '')} onClick={() => up({ style: s })}>{ar ? STYLES[s].ar : STYLES[s].en}</button>)}</div>
    </div>
    <div className="grid2">
      <div className="card">
        <svg className="sheet" viewBox={vb} preserveAspectRatio="xMidYMid meet" style={{ direction: 'ltr' }}>
          {layout.panels.map(p => <g key={p.id}>
            <path d={p.pts.map((q, i) => `${i ? 'L' : 'M'}${(p.x + q[0]).toFixed(2)} ${(p.y + q[1]).toFixed(2)}`).join(' ') + 'Z'} fill="rgba(34,211,238,.08)" stroke="#EF4444" strokeWidth={Math.max(0.4, layout.sheetW / 500)} />
            <text x={p.x + p.w / 2} y={p.y + p.h / 2} fill="#38BDF8" fontSize={Math.max(3, layout.sheetW / 90)} textAnchor="middle">{p.label}</text>
          </g>)}
        </svg>
        <div className="muted" style={{ margin: '6px 0' }}>{T('مساحة اللوح', 'Sheet')}: {Math.trunc(layout.sheetW)}×{Math.trunc(layout.sheetH)} mm • Kerf {box.kerfMm} mm</div>
        <div className="stats">
          <div className="stat"><b>{layout.panels.length}</b><span>{T('قطع', 'Parts')}</span></div>
          <div className="stat"><b>{layout.tabs}</b><span>{T('ألسنة', 'Tabs')}</span></div>
          <div className="stat"><b>{layout.cutM.toFixed(2)}m</b><span>{T('طول القص', 'Cut')}</span></div>
          <div className="stat"><b>{Math.round(layout.volumeCm3)}cm³</b><span>{T('الحجم', 'Volume')}</span></div>
        </div>
      </div>
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <Slider label={T('العرض', 'Width')} value={box.widthMm} min={30} max={500} show={`${box.widthMm} mm`} onChange={v => up({ widthMm: v })} />
        <Slider label={T('الارتفاع', 'Height')} value={box.heightMm} min={25} max={400} show={`${box.heightMm} mm`} onChange={v => up({ heightMm: v })} />
        <Slider label={T('العمق', 'Depth')} value={box.depthMm} min={30} max={500} show={`${box.depthMm} mm`} onChange={v => up({ depthMm: v })} />
        <Slider label={T('سماكة الخامة', 'Thickness')} value={box.thicknessMm} min={1.5} max={15} step={0.1} show={`${box.thicknessMm} mm`} onChange={v => up({ thicknessMm: v })} />
        <Slider label={T('حجم لسان التعشيق', 'Tab size')} value={box.tabSizeMm} min={6} max={60} show={`${box.tabSizeMm} mm`} onChange={v => up({ tabSizeMm: v })} />
        <Slider label={T('خلوص الشعاع (Kerf)', 'Kerf')} value={box.kerfMm} min={0} max={0.5} step={0.01} show={`${box.kerfMm} mm`} onChange={v => up({ kerfMm: v })} />
        <button className="btn" onClick={exp} data-testid="export-box">⬇ {T(`تصدير المخطط SVG + DXF (${layout.panels.length} قطع)`, `Export SVG + DXF (${layout.panels.length} Parts)`)}</button>
        <button className="btn ghost" onClick={onSync}>🧮 {T('احسب تكلفة التصنيع وسعر البيع', 'Calculate production cost & price')}</button>
      </div>
    </div>
  </>
}

// ---------------- Tool 3: Cost ----------------
function CostTool({ ar, cost, setCost, currency, workshop, quotes, setQuotes, toast }: { ar: boolean; cost: CostConfig; setCost: (f: (c: CostConfig) => CostConfig) => void; currency: string; workshop: string; quotes: Quote[]; setQuotes: (f: (q: Quote[]) => Quote[]) => void; toast: (s: string) => void }) {
  const T = (a: string, e: string) => (ar ? a : e)
  const r = calculatePricing(cost)
  const m = (v: number, s = false) => fmtMoney(v, currency, ar, s)
  const sym = ar ? currencyOf(currency).ar : currencyOf(currency).en
  const up = (p: Partial<CostConfig>) => setCost(c => ({ ...c, ...p }))
  const [docQuote, setDocQuote] = useState<{ q: Quote; kind: 'pdf' | 'png' } | null>(null)
  const docRef = useRef<HTMLDivElement>(null)
  const snapshot = (): Quote => ({ id: uid(), projectName: cost.projectName || T('مشروع قص ليزر مخصص', 'Custom Laser Job'), retailPrice: r.retail, unitCost: r.total, marginPercent: Math.trunc(cost.profitMarginPercent), currencyCode: currency, createdAt: Date.now(), itemWidthMm: cost.itemWidthMm, itemLengthMm: cost.itemLengthMm, batchQuantity: Math.max(1, Math.round(cost.batchQuantity)), materialCost: r.material, machineCost: r.machine, electricityCost: r.electricity, setupCost: r.setup })
  useEffect(() => {
    if (!docQuote || !docRef.current) return
    const node = docRef.current
    ;(async () => {
      try {
        await document.fonts.ready
        const base = 'quote_' + (docQuote.q.projectName.replace(/[^\p{L}\p{N}]+/gu, '_').replace(/^_|_$/g, '').slice(0, 40) || 'laser')
        const blob = docQuote.kind === 'pdf' ? await nodeToPdf(node) : await nodeToPng(node)
        const how = await shareOrDownload(`${base}.${docQuote.kind}`, blob, docQuote.q.projectName)
        toast(how === 'shared' ? T('تمت المشاركة', 'Shared') : T('تم تنزيل الملف', 'File downloaded'))
      } catch (e) { toast(T('تعذر التصدير: ', 'Export failed: ') + (e as Error).message) }
      setDocQuote(null)
    })()
  }, [docQuote]) // eslint-disable-line
  const total = max1(r.retail)
  const frac = (v: number, min: number) => Math.min(1, Math.max(min, v / total))
  return <>
    <Banner badge={T('الأداة 03 • منصة قص الليزر والـ CNC', 'TOOL 03 • CNC & LASER WORKBENCH')} title={T('حاسبة التكاليف وتسعير منتجات الورشة', 'Workshop Cost & Pricing Calculator')} sub={T('تكلفة الخامة، وقت الليزر حسب الماكينة، الكهرباء، وسعر البيع المقترح.', 'Material, machine-aware laser time, electricity and a profitable retail price.')} />
    <div className="card">
      <div className="lbl">{T('قوالب أسعار الألواح وسرعة القص الافتراضية:', 'MATERIAL SHEET COST & SPEED PRESETS:')}</div>
      <div className="chips">{MATERIALS.map(mt => <button key={mt.en} className={'chip' + (Math.abs(cost.materialCostPerSheet - mt.cost) < 0.1 ? ' on' : '')} onClick={() => up({ materialCostPerSheet: mt.cost, sheetWidthMm: mt.w, sheetLengthMm: mt.h, cuttingSpeedMmPerSec: mt.speed })}>{ar ? mt.ar : mt.en} ({m(mt.cost)})</button>)}</div>
    </div>
    <div className="grid2">
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <div className="price" data-testid="retail">
          <div className="row" style={{ justifyContent: 'space-between' }}>
            <div><div className="green" style={{ fontSize: 11 }}>{T('سعر البيع المقترح للقطعة الواحدة', 'RECOMMENDED RETAIL PRICE / ITEM')}</div><div className="big">{m(r.retail)}</div></div>
            <div style={{ flex: '0 0 auto', textAlign: 'end' }}><b className="green">{m(r.profit, true)}</b><div className="muted">{T('صافي الربح للقطعة', 'Net profit / unit')}</div></div>
          </div>
          <div className="metrics">
            <div className="metric"><span className="muted">{T('تكلفة الخامة', 'MATERIAL')}</span><b style={{ color: 'var(--cyan)' }}>{m(r.material)}</b><span className="muted">{r.itemsPerSheet} {T('قطع / لوح', 'pcs / sheet')}</span></div>
            <div className="metric"><span className="muted">{T('تكلفة التشغيل', 'MACHINE')}</span><b style={{ color: 'var(--orange)' }}>{m(r.machine + r.electricity)}</b><span className="muted">{fmtSec(Math.round(r.cutSec), ar)}</span></div>
            <div className="metric"><span className="muted">{T('إجمالي التكلفة', 'UNIT COGS')}</span><b style={{ color: 'var(--blue)' }}>{m(r.total)}</b><span className="muted">{T('جملة', 'Wholesale')} {m(r.wholesale)}</span></div>
          </div>
        </div>
        <div>
          <div className="lbl">{T('تحليل هيكل السعر والتكلفة', 'UNIT ECONOMICS BREAKDOWN')}</div>
          <div className="bar">{[[r.material, .04, 'var(--cyan)'], [r.machine + r.electricity, .04, 'var(--orange)'], [r.setup, .03, 'var(--amber)'], [r.profit, .05, 'var(--green)']].map(([v, mn, c], i) => <div key={i} style={{ flex: frac(v as number, mn as number), background: c as string }} />)}</div>
        </div>
        <div className="row" style={{ justifyContent: 'space-between', background: 'rgba(30,41,59,.5)', borderRadius: 12, padding: 12 }}>
          <div><div className="muted">{T(`إجمالي دفعة الإنتاج (${cost.batchQuantity} قطعة)`, `BATCH RUN (${cost.batchQuantity} UNITS)`)}</div><b>{T('إجمالي المبيعات: ', 'Total revenue: ')}{m(r.batchRevenue)}</b></div>
          <b className="green" style={{ flex: '0 0 auto' }}>{T('صافي الربح: ', 'Net profit: ')}{m(r.batchProfit, true)}</b>
        </div>
        <div className="row">
          <button className="btn" data-testid="save-quote" onClick={() => { const q = snapshot(); setQuotes(qs => [q, ...qs]); toast(T(`تم حفظ عرض السعر "${q.projectName}"`, `Saved quote "${q.projectName}"`)) }}>🔖 {T('حفظ عرض السعر', 'Save Quote')}</button>
          <button className="btn ghost" data-testid="quote-pdf" onClick={() => setDocQuote({ q: snapshot(), kind: 'pdf' })}>📄 {T('عرض سعر PDF', 'Quote PDF')}</button>
          <button className="btn ghost" data-testid="quote-png" onClick={() => setDocQuote({ q: snapshot(), kind: 'png' })}>🖼 {T('عرض سعر صورة', 'Quote Image')}</button>
        </div>
        {quotes.length > 0 && <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <div className="lbl">{T(`عروض الأسعار المحفوظة (${quotes.length})`, `SAVED QUOTES (${quotes.length})`)}</div>
          {quotes.map(q => <div className="quote" key={q.id} data-testid="saved-quote">
            <div style={{ minWidth: 0 }}><b>{q.projectName}</b><div className="muted">{new Date(q.createdAt).toLocaleDateString(ar ? 'ar-SA-u-nu-latn' : 'en-GB')} • {q.batchQuantity} {T('قطعة', 'pcs')}</div></div>
            <div style={{ textAlign: 'end', flex: '0 0 auto' }}><b className="green">{fmtMoney(q.retailPrice, q.currencyCode, ar)}</b>
              <div className="row" style={{ gap: 4 }}><button className="mini" onClick={() => setDocQuote({ q, kind: 'pdf' })}>PDF</button><button className="mini" onClick={() => setDocQuote({ q, kind: 'png' })}>PNG</button><button className="mini" onClick={() => setQuotes(qs => qs.filter(x => x.id !== q.id))}>🗑</button></div></div>
          </div>)}
        </div>}
      </div>
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <label className="f">{T('اسم المشروع / عرض السعر', 'Job / client quote title')}<input type="text" value={cost.projectName} onChange={e => up({ projectName: e.target.value })} /></label>
        <div className="row"><Num label={T(`سعر اللوح (${sym})`, `Sheet cost (${sym})`)} value={cost.materialCostPerSheet} step={0.5} onChange={v => up({ materialCostPerSheet: v })} testid="sheet-cost" /><Num label={T('عرض اللوح (مم)', 'Sheet W (mm)')} value={cost.sheetWidthMm} onChange={v => up({ sheetWidthMm: v })} /><Num label={T('طول اللوح (مم)', 'Sheet L (mm)')} value={cost.sheetLengthMm} onChange={v => up({ sheetLengthMm: v })} /></div>
        <div className="row"><Num label={T('عرض القطعة (مم)', 'Part W (mm)')} value={cost.itemWidthMm} onChange={v => up({ itemWidthMm: v })} /><Num label={T('طول القطعة (مم)', 'Part L (mm)')} value={cost.itemLengthMm} onChange={v => up({ itemLengthMm: v })} /><Num label={T('طول القص (متر)', 'Cut length (m)')} value={cost.cutLengthMeters} step={0.05} onChange={v => up({ cutLengthMeters: v })} /></div>
        <div className="row"><Num label={T('السرعة (مم/ث)', 'Speed (mm/s)')} value={cost.cuttingSpeedMmPerSec} onChange={v => up({ cuttingSpeedMmPerSec: v })} /><Num label={T(`ساعة الماكينة (${sym})`, `Shop rate (${sym}/hr)`)} value={cost.hourlyMachineRate} onChange={v => up({ hourlyMachineRate: v })} /><Num label={T(`رسوم التجهيز (${sym})`, `Setup fee (${sym})`)} value={cost.setupAndHandlingFee} step={0.5} onChange={v => up({ setupAndHandlingFee: v })} /></div>
        <div className="lbl">{T('ملف الماكينة (نوع الليزر والقدرة)', 'MACHINE PROFILE (LASER TYPE & POWER)')}</div>
        <div className="chips">{(Object.keys(LASER_TYPES) as LaserType[]).map(t => <button key={t} data-testid={'laser-' + t} className={'chip' + (cost.laserType === t ? ' on' : '')} onClick={() => up({ laserType: t, laserPowerW: LASER_TYPES[t].defW, machineWatts: LASER_TYPES[t].defMachine })}>{ar ? LASER_TYPES[t].ar : LASER_TYPES[t].en}</button>)}</div>
        <div className="row"><Num label={T('قدرة الليزر (واط)', 'Laser power (W)')} value={cost.laserPowerW} onChange={v => up({ laserPowerW: v })} /><Num label={T('استهلاك الماكينة (واط)', 'Machine draw (W)')} value={cost.machineWatts} onChange={v => up({ machineWatts: v })} /><Num label={T(`سعر الكيلوواط (${sym})`, `kWh price (${sym})`)} value={cost.kwhPrice} step={0.01} onChange={v => up({ kwhPrice: v })} /></div>
        <div className="muted">{T(`السرعة الفعلية المقدّرة: ${Math.round(r.speed)} مم/ث (السرعة المرجعية للخامة على CO2 بقدرة 60 واط) • الكهرباء: ${m(r.electricity)} للقطعة`, `Estimated effective speed: ${Math.round(r.speed)} mm/s (material reference = 60W CO2) • Electricity: ${m(r.electricity)}/unit`)}</div>
        <Slider label={T('هامش الربح المطلوب', 'Desired profit margin')} value={cost.profitMarginPercent} min={15} max={80} show={`${Math.round(cost.profitMarginPercent)}%`} onChange={v => up({ profitMarginPercent: v })} />
        <Slider label={T('نسبة الهدر', 'Kerf/scrap waste')} value={cost.wasteFactorPercent} min={0} max={50} show={`${Math.round(cost.wasteFactorPercent)}%`} onChange={v => up({ wasteFactorPercent: v })} />
        <Slider label={T('كمية الدفعة', 'Batch run size')} value={cost.batchQuantity} min={1} max={100} show={T(`${cost.batchQuantity} قطعة`, `${cost.batchQuantity} units`)} onChange={v => up({ batchQuantity: Math.max(1, Math.round(v)) })} testid="qty" />
      </div>
    </div>
    {docQuote && <QuoteDoc ref_={docRef} q={docQuote.q} workshop={workshop} ar={ar} />}
  </>
}
const max1 = (v: number) => Math.max(0.01, v)
function fmtSec(t: number, ar: boolean) { const mi = Math.floor(t / 60), s = t % 60; return ar ? (mi ? `${mi}د ${s}ث قص` : `${s}ث قص`) : (mi ? `${mi}m ${s}s cut` : `${s}s cut`) }

function QuoteDoc({ q, workshop, ar, ref_ }: { q: Quote; workshop: string; ar: boolean; ref_: React.RefObject<HTMLDivElement> }) {
  const T = (a: string, e: string) => (ar ? a : e)
  const d = new Date(q.createdAt); const p = (n: number) => String(n).padStart(2, '0')
  const rows: [string, string][] = []
  if (q.itemWidthMm > 0 && q.itemLengthMm > 0) rows.push([T('مقاس القطعة', 'Part size'), T(`${Math.trunc(q.itemWidthMm)}×${Math.trunc(q.itemLengthMm)} مم`, `${Math.trunc(q.itemWidthMm)}×${Math.trunc(q.itemLengthMm)} mm`)])
  rows.push([T('الكمية', 'Quantity'), T(`${q.batchQuantity} قطعة`, `${q.batchQuantity} pcs`)], [T('سعر القطعة', 'Unit price'), fmtMoney(q.retailPrice, q.currencyCode, ar)])
  return <div className="qdoc" ref={ref_} dir={ar ? 'rtl' : 'ltr'}>
    <div className="hd"><h1>{workshop || T('ورشة الليزر', 'Laser Workshop')}</h1><div>{T('عرض سعر', 'Price Quote')}</div></div>
    <div className="bd">
      <div style={{ color: '#64748B', fontSize: 12 }}>{T('التاريخ: ', 'Date: ')}{`${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`}</div>
      <h2 style={{ fontSize: 19, margin: '10px 0 14px' }}>{q.projectName}</h2>
      {rows.map(([a, b]) => <div className="r" key={a}><span>{a}</span><b>{b}</b></div>)}
      <div className="tot"><span>{T('الإجمالي', 'Total')}</span><b>{fmtMoney(q.retailPrice * Math.max(1, q.batchQuantity), q.currencyCode, ar)}</b></div>
      <div style={{ color: '#64748B', fontSize: 11 }}>{T('الأسعار تقديرية وقد تتغير حسب التصميم النهائي والخامة.', 'Prices are estimates and may change with the final design and material.')}</div>
    </div>
  </div>
}

function Settings({ ar, workshop, setWorkshop, currency, setCurrency, history, clear, open, close }: { ar: boolean; workshop: string; setWorkshop: (s: string) => void; currency: string; setCurrency: (s: string) => void; history: ExportItem[]; clear: () => void; open: (i: ExportItem) => void; close: () => void }) {
  const T = (a: string, e: string) => (ar ? a : e)
  return <div className="modal-bg" onClick={close}><div className="modal" onClick={e => e.stopPropagation()}>
    <div className="row" style={{ justifyContent: 'space-between' }}><h2 style={{ margin: 0 }}>{T('إعدادات الورشة', 'Workshop Settings')}</h2><button className="icon-btn" style={{ flex: '0 0 36px' }} onClick={close}>✕</button></div>
    <label className="f" style={{ marginTop: 12 }}>{T('اسم الورشة (يظهر في عروض الأسعار)', 'Workshop name (shown on quotes)')}<input type="text" maxLength={60} value={workshop} onChange={e => setWorkshop(e.target.value)} data-testid="workshop-input" /></label>
    <div className="muted" style={{ marginTop: 4 }}>{T('يُحفظ على جهازك فقط، بدون حساب أو تسجيل دخول.', 'Stored on this device only — no account or sign-in.')}</div>
    <div className="lbl">{T('العملة', 'CURRENCY')}</div>
    <div className="chips">{CURRENCIES.map(c => <button key={c.code} data-testid={'cur-' + c.code} className={'chip' + (c.code === currency ? ' on' : '')} onClick={() => setCurrency(c.code)}>{ar ? c.ar : c.en} • {ar ? c.nameAr : c.nameEn}</button>)}</div>
    <div className="row" style={{ justifyContent: 'space-between', marginTop: 12 }}><div className="lbl">{T(`سجل الملفات المصدرة (${history.length})`, `EXPORT HISTORY (${history.length})`)}</div>{history.length > 0 && <button className="mini" style={{ flex: '0 0 auto' }} onClick={clear}>🗑 {T('مسح السجل', 'Clear')}</button>}</div>
    {history.length === 0 ? <div className="muted">{T('لا توجد ملفات مصدرة بعد. ولّد مسار فيكتور في الأداة 1 أو صدّر صندوقاً في الأداة 2.', 'No exports yet. Generate a vector in Tool 1 or export a box in Tool 2.')}</div> :
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>{history.map(h => <div key={h.id} className="quote" style={{ cursor: 'pointer' }} onClick={() => open(h)}><div><b style={{ color: 'var(--cyan)', fontSize: 13 }}>{h.fileName}</b><div className="muted">{ar ? h.toolAr : h.toolEn} • {h.time} • {h.dxf ? 'SVG + DXF' : 'SVG'}</div></div></div>)}</div>}
  </div></div>
}

function ExportModal({ ar, item, close, toast }: { ar: boolean; item: ExportItem; close: () => void; toast: (s: string) => void }) {
  const T = (a: string, e: string) => (ar ? a : e)
  const dxfName = item.fileName.replace(/\.svg$/, '') + '.dxf'
  const svgBlob = () => new Blob([item.svg], { type: 'image/svg+xml' })
  const dxfBlob = () => new Blob([item.dxf ?? ''], { type: 'application/dxf' })
  return <div className="modal-bg" onClick={close}><div className="modal" onClick={e => e.stopPropagation()}>
    <div className="row" style={{ justifyContent: 'space-between' }}><div><h2 style={{ margin: 0 }}>✅ {item.dxf ? T('ملفات القص SVG و DXF جاهزة', 'SVG & DXF Cut Files Ready') : T('ملف الفيكتور SVG جاهز للقص', 'Clean SVG Path Ready')}</h2><div className="muted" style={{ color: 'var(--orange)' }}>{item.fileName}{item.dxf ? ` • ${dxfName}` : ''}</div></div><button className="icon-btn" style={{ flex: '0 0 36px' }} onClick={close}>✕</button></div>
    <div className="lbl">{T('كود المسار المصدري', 'PRODUCTION SVG SOURCE')} • {item.svg.length} B</div>
    <pre className="code">{item.svg}</pre>
    <div className="row" style={{ marginTop: 10 }}>
      <button className="btn" data-testid="dl-svg" onClick={() => download(item.fileName, svgBlob())}>⬇ {T('تنزيل SVG', 'Download SVG')}</button>
      <button className="btn ghost" onClick={async () => { await shareOrDownload(item.fileName, svgBlob(), item.fileName) }}>↗ {T('مشاركة SVG', 'Share SVG')}</button>
    </div>
    {item.dxf && <div className="row" style={{ marginTop: 8 }}>
      <button className="btn orange" data-testid="dl-dxf" onClick={() => download(dxfName, dxfBlob())}>⬇ {T('تنزيل DXF', 'Download DXF')}</button>
      <button className="btn ghost" onClick={async () => { await shareOrDownload(dxfName, dxfBlob(), dxfName) }}>↗ {T('مشاركة DXF', 'Share DXF')}</button>
    </div>}
    <button className="btn ghost" style={{ width: '100%', marginTop: 8 }} onClick={() => { navigator.clipboard?.writeText(item.svg); toast(T('تم نسخ كود SVG إلى الحافظة!', 'Copied SVG code to clipboard!')) }}>📋 {T('نسخ كود SVG', 'Copy SVG XML')}</button>
  </div></div>
}
