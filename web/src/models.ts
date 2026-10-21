// Ported from app/src/main/java/com/example/model/FabricationModels.kt
export type Pt = [number, number]

export const CURRENCIES = [
  { code: 'SAR', ar: 'ر.س', en: 'SAR', nameAr: 'ريال سعودي', nameEn: 'Saudi Riyal' },
  { code: 'AED', ar: 'د.إ', en: 'AED', nameAr: 'درهم إماراتي', nameEn: 'UAE Dirham' },
  { code: 'KWD', ar: 'د.ك', en: 'KWD', nameAr: 'دينار كويتي', nameEn: 'Kuwaiti Dinar' },
  { code: 'QAR', ar: 'ر.ق', en: 'QAR', nameAr: 'ريال قطري', nameEn: 'Qatari Riyal' },
  { code: 'BHD', ar: 'د.ب', en: 'BHD', nameAr: 'دينار بحريني', nameEn: 'Bahraini Dinar' },
  { code: 'OMR', ar: 'ر.ع', en: 'OMR', nameAr: 'ريال عماني', nameEn: 'Omani Rial' },
  { code: 'EGP', ar: 'ج.م', en: 'EGP', nameAr: 'جنيه مصري', nameEn: 'Egyptian Pound' },
  { code: 'USD', ar: '$', en: '$', nameAr: 'دولار أمريكي', nameEn: 'US Dollar' },
  { code: 'EUR', ar: '€', en: '€', nameAr: 'يورو', nameEn: 'Euro' },
] as const
export type CurrencyCode = (typeof CURRENCIES)[number]['code']
export const currencyOf = (code: string) => CURRENCIES.find(c => c.code === code) ?? CURRENCIES[0]
export function fmtMoney(amount: number, code: string, ar: boolean, signed = false): string {
  const c = currencyOf(code)
  const num = amount.toFixed(2)
  const sign = signed && amount >= 0 ? '+' : ''
  if (!ar && (c.code === 'USD' || c.code === 'EUR')) return `${sign}${c.en}${num}`
  return `${sign}${num} ${ar ? c.ar : c.en}`
}

export const LASER_TYPES = {
  CO2: { ar: 'ليزر CO2', en: 'CO2 Laser', refW: 60, base: 1.0, defW: 60, defMachine: 900 },
  DIODE: { ar: 'ليزر ديود', en: 'Diode Laser', refW: 20, base: 0.45, defW: 20, defMachine: 150 },
  FIBER: { ar: 'ليزر فايبر', en: 'Fiber Laser', refW: 50, base: 1.15, defW: 50, defMachine: 600 },
} as const
export type LaserType = keyof typeof LASER_TYPES
export const clamp = (v: number, a: number, b: number) => Math.min(b, Math.max(a, v))
export function speedMultiplier(t: LaserType, powerW: number) {
  const l = LASER_TYPES[t]
  return clamp(l.base * Math.sqrt(clamp(powerW, 1, 500) / l.refW), 0.1, 4)
}

export const STYLES = {
  CLOSED_SIX_PANEL: { en: 'Closed 6-Panel Box', ar: 'صندوق مغلق (6 أوجه)' },
  OPEN_TOP_TRAY: { en: 'Open-Top Organizer', ar: 'صندوق منظم مفتوح من الأعلى (5 أوجه)' },
  DIVIDED_TRAY: { en: 'Open Tray + Center Divider', ar: 'صندوق منظم مع قاطع وسطي (6 قطع)' },
  DRAWER: { en: 'Drawer + Front Face', ar: 'درج مع واجهة أمامية (6 قطع)' },
  PHONE_STAND: { en: 'Phone Stand', ar: 'حامل جوال (4 قطع)' },
  PICTURE_FRAME: { en: 'Picture Frame', ar: 'برواز صور (6 قطع)' },
} as const
export type EnclosureStyle = keyof typeof STYLES

export const MATERIALS = [
  { en: '3.0mm Laser MDF', ar: 'خشب MDF ليزر 3.0 مم', t: 3.0, cost: 14.5, w: 600, h: 400, speed: 28 },
  { en: '3.2mm (1/8") Birch Ply', ar: 'أبلكاش بتولا 3.2 مم', t: 3.2, cost: 19.0, w: 600, h: 400, speed: 32 },
  { en: '3.0mm Cast Acrylic', ar: 'أكريليك صب 3.0 مم', t: 3.0, cost: 29.5, w: 600, h: 400, speed: 20 },
  { en: '5.0mm Baltic Birch', ar: 'خشب بتولا 5.0 مم', t: 5.0, cost: 26.0, w: 600, h: 400, speed: 18 },
  { en: '6.0mm Walnut Veneer', ar: 'خشب جوز طبيعي 6.0 مم', t: 6.0, cost: 42.0, w: 600, h: 400, speed: 15 },
]

export const SOFTWARE = {
  LIGHTBURN: { en: 'LightBurn (C00 Cut)', ar: 'لايت بيرن LightBurn (قص C00)', color: '#FF0000', width: 0.05 },
  SILHOUETTE: { en: 'Silhouette / Cricut', ar: 'سيلويت Silhouette / كريكيت', color: '#000000', width: 0.1 },
  GLOWFORGE: { en: 'Glowforge UI Ready', ar: 'جلوفورج Glowforge SVG', color: '#0066FF', width: 0.08 },
  CNC_GRBL: { en: 'CNC Router / GRBL', ar: 'راوتر CNC / نظام GRBL', color: '#06B6D4', width: 0.15 },
} as const
export type Software = keyof typeof SOFTWARE

export interface BoxConfig { widthMm: number; heightMm: number; depthMm: number; thicknessMm: number; tabSizeMm: number; kerfMm: number; style: EnclosureStyle }
export interface Panel { id: string; label: string; dims: string; x: number; y: number; w: number; h: number; pts: Pt[] }
export interface BoxLayout { panels: Panel[]; sheetW: number; sheetH: number; cutM: number; tabs: number; volumeCm3: number }

export interface CostConfig {
  projectName: string; materialCostPerSheet: number; sheetWidthMm: number; sheetLengthMm: number
  itemWidthMm: number; itemLengthMm: number; cutLengthMeters: number; cuttingSpeedMmPerSec: number
  hourlyMachineRate: number; setupAndHandlingFee: number; wasteFactorPercent: number; profitMarginPercent: number
  batchQuantity: number; laserType: LaserType; laserPowerW: number; machineWatts: number; kwhPrice: number
}
export const DEFAULT_COST: CostConfig = {
  projectName: 'صندوق تعشيق بالليزر مخصص', materialCostPerSheet: 19, sheetWidthMm: 600, sheetLengthMm: 400,
  itemWidthMm: 260, itemLengthMm: 190, cutLengthMeters: 3.85, cuttingSpeedMmPerSec: 28, hourlyMachineRate: 45,
  setupAndHandlingFee: 2.5, wasteFactorPercent: 15, profitMarginPercent: 55, batchQuantity: 10,
  laserType: 'CO2', laserPowerW: 60, machineWatts: 900, kwhPrice: 0.18,
}

export interface ExportItem { id: string; title: string; toolEn: string; toolAr: string; fileName: string; time: string; svg: string; dxf?: string }
export interface Quote {
  id: string; projectName: string; retailPrice: number; unitCost: number; marginPercent: number; currencyCode: string
  createdAt: number; itemWidthMm: number; itemLengthMm: number; batchQuantity: number
  materialCost: number; machineCost: number; electricityCost: number; setupCost: number
}
