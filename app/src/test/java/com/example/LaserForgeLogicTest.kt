package com.example

import com.example.data.LocalStore
import com.example.engine.BoxSvgGenerator
import com.example.model.AppCurrency
import com.example.model.BoxGeneratorConfig
import com.example.model.CostCalculatorConfig
import com.example.model.EnclosureStyle
import com.example.model.LaserType
import com.example.model.SavedWorkshopQuote
import com.example.viewmodel.calculateWorkshopPricing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LaserForgeLogicTest {

  @Test
  fun electricityCostMatchesTimeTimesPower() {
    val cfg = CostCalculatorConfig(machineWatts = 1000f, kwhPrice = 0.18f)
    val r = calculateWorkshopPricing(cfg)
    val expected = r.cutTimeSecondsPerItem / 3600f * 1f * 0.18f
    assertEquals(expected, r.electricityCostPerItem, 1e-5f)
  }

  @Test
  fun weakerDiodeIsSlowerThanCo2() {
    val co2 = calculateWorkshopPricing(CostCalculatorConfig(laserType = LaserType.CO2, laserPowerW = 60f))
    val diode = calculateWorkshopPricing(CostCalculatorConfig(laserType = LaserType.DIODE, laserPowerW = 10f))
    assertTrue(diode.cutTimeSecondsPerItem > co2.cutTimeSecondsPerItem)
  }

  @Test
  fun currencyFormatsSarInArabic() {
    assertEquals("24.80 ر.س", AppCurrency.SAR.format(24.8f, true))
    assertEquals("$24.80", AppCurrency.USD.format(24.8f, false))
  }

  @Test
  fun allTemplatesProduceDxf() {
    EnclosureStyle.entries.forEach { style ->
      val cfg = BoxGeneratorConfig(enclosureStyle = style)
      val layout = BoxSvgGenerator.generateBoxLayout(cfg)
      assertTrue(layout.panels.isNotEmpty())
      val dxf = BoxSvgGenerator.exportBoxDxf(cfg, layout)
      assertTrue(dxf.contains("AC1009") && dxf.contains("LINE") && dxf.trimEnd().endsWith("EOF"))
    }
  }

  @Test
  fun quotesRoundTripThroughJson() {
    val q = SavedWorkshopQuote("1", "صندوق", 10f, 5f, 50, "en", "ar", currencyCode = "KWD", batchQuantity = 3)
    val back = LocalStore.decodeQuotes(LocalStore.encodeQuotes(listOf(q))).single()
    assertEquals(q, back)
  }
}
