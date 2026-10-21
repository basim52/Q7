// Ported from calculateWorkshopPricing() in LaserForgeViewModel.kt
import { CostConfig, speedMultiplier, clamp } from './models'
export function calculatePricing(c: CostConfig) {
  const max = Math.max
  const sheetArea = max(1, c.sheetWidthMm * c.sheetLengthMm)
  const itemArea = max(1, c.itemWidthMm * c.itemLengthMm)
  const colsA = Math.floor(c.sheetWidthMm / max(1, c.itemWidthMm)), rowsA = Math.floor(c.sheetLengthMm / max(1, c.itemLengthMm))
  const colsB = Math.floor(c.sheetWidthMm / max(1, c.itemLengthMm)), rowsB = Math.floor(c.sheetLengthMm / max(1, c.itemWidthMm))
  const itemsPerSheet = max(1, max(colsA * rowsA, colsB * rowsB))
  const material = c.materialCostPerSheet * (itemArea / sheetArea) * (1 + clamp(c.wasteFactorPercent, 0, 100) / 100)
  const cutMm = max(10, c.cutLengthMeters * 1000)
  const speed = max(0.5, c.cuttingSpeedMmPerSec * speedMultiplier(c.laserType, c.laserPowerW))
  const cutSec = (cutMm / speed) * 1.15
  const machine = cutSec * (c.hourlyMachineRate / 3600)
  const electricity = (cutSec / 3600) * (max(0, c.machineWatts) / 1000) * max(0, c.kwhPrice)
  const qty = max(1, Math.round(c.batchQuantity))
  const setup = c.setupAndHandlingFee / qty
  const total = material + machine + electricity + setup
  const margin = clamp(c.profitMarginPercent, 5, 90) / 100
  const retail = total / (1 - margin)
  const profit = retail - total
  return {
    sheetAreaCm2: sheetArea / 100, itemAreaCm2: itemArea / 100, itemsPerSheet, material, cutSec, machine, electricity,
    speed, setup, total, wholesale: total * 1.35, retail, profit, batchRevenue: retail * qty, batchProfit: profit * qty,
  }
}
export type Pricing = ReturnType<typeof calculatePricing>
