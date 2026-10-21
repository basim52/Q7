package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppCurrency
import com.example.model.ExportHistoryItem
import com.example.model.SavedWorkshopQuote
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.laserForgeStore by preferencesDataStore(name = "laserforge_local")

data class PersistedState(
    val workshopName: String,
    val currency: AppCurrency,
    val savedQuotes: List<SavedWorkshopQuote>,
    val exportHistory: List<ExportHistoryItem>
)

/** On-device persistence (DataStore Preferences holding JSON). Nothing leaves the device. */
class LocalStore(private val context: Context) {

    private val keyWorkshop = stringPreferencesKey("workshop_name")
    private val keyCurrency = stringPreferencesKey("currency_code")
    private val keyQuotes = stringPreferencesKey("saved_quotes_json")
    private val keyHistory = stringPreferencesKey("export_history_json")

    suspend fun load(): PersistedState {
        val prefs = context.laserForgeStore.data.first()
        return PersistedState(
            workshopName = prefs[keyWorkshop] ?: "",
            currency = AppCurrency.fromCode(prefs[keyCurrency]),
            savedQuotes = runCatching { decodeQuotes(prefs[keyQuotes]) }.getOrDefault(emptyList()),
            exportHistory = runCatching { decodeHistory(prefs[keyHistory]) }.getOrDefault(emptyList())
        )
    }

    suspend fun saveWorkshopName(name: String) = context.laserForgeStore.edit { it[keyWorkshop] = name }
    suspend fun saveCurrency(currency: AppCurrency) = context.laserForgeStore.edit { it[keyCurrency] = currency.code }
    suspend fun saveQuotes(quotes: List<SavedWorkshopQuote>) =
        context.laserForgeStore.edit { it[keyQuotes] = encodeQuotes(quotes) }
    suspend fun saveHistory(items: List<ExportHistoryItem>) =
        context.laserForgeStore.edit { it[keyHistory] = encodeHistory(items.take(MAX_HISTORY)) }

    companion object {
        const val MAX_HISTORY = 25

        fun encodeQuotes(quotes: List<SavedWorkshopQuote>): String = JSONArray().apply {
            quotes.forEach { q ->
                put(JSONObject().apply {
                    put("id", q.id); put("projectName", q.projectName)
                    put("retailPrice", q.retailPrice.toDouble()); put("unitCost", q.unitCost.toDouble())
                    put("marginPercent", q.marginPercent)
                    put("summaryTextEn", q.summaryTextEn); put("summaryTextAr", q.summaryTextAr)
                    put("currencyCode", q.currencyCode); put("createdAtMillis", q.createdAtMillis)
                    put("itemWidthMm", q.itemWidthMm.toDouble()); put("itemLengthMm", q.itemLengthMm.toDouble())
                    put("batchQuantity", q.batchQuantity)
                    put("materialCost", q.materialCost.toDouble()); put("machineCost", q.machineCost.toDouble())
                    put("electricityCost", q.electricityCost.toDouble()); put("setupCost", q.setupCost.toDouble())
                })
            }
        }.toString()

        fun decodeQuotes(json: String?): List<SavedWorkshopQuote> {
            if (json.isNullOrBlank()) return emptyList()
            val arr = JSONArray(json)
            return (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavedWorkshopQuote(
                    id = o.getString("id"),
                    projectName = o.optString("projectName"),
                    retailPrice = o.optDouble("retailPrice", 0.0).toFloat(),
                    unitCost = o.optDouble("unitCost", 0.0).toFloat(),
                    marginPercent = o.optInt("marginPercent"),
                    summaryTextEn = o.optString("summaryTextEn"),
                    summaryTextAr = o.optString("summaryTextAr"),
                    currencyCode = o.optString("currencyCode", AppCurrency.SAR.code),
                    createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
                    itemWidthMm = o.optDouble("itemWidthMm", 0.0).toFloat(),
                    itemLengthMm = o.optDouble("itemLengthMm", 0.0).toFloat(),
                    batchQuantity = o.optInt("batchQuantity", 1),
                    materialCost = o.optDouble("materialCost", 0.0).toFloat(),
                    machineCost = o.optDouble("machineCost", 0.0).toFloat(),
                    electricityCost = o.optDouble("electricityCost", 0.0).toFloat(),
                    setupCost = o.optDouble("setupCost", 0.0).toFloat()
                )
            }
        }

        fun encodeHistory(items: List<ExportHistoryItem>): String = JSONArray().apply {
            items.forEach { e ->
                put(JSONObject().apply {
                    put("id", e.id); put("title", e.title)
                    put("toolNameEn", e.toolNameEn); put("toolNameAr", e.toolNameAr)
                    put("fileName", e.fileName); put("timestampLabel", e.timestampLabel)
                    put("svgContent", e.svgContent)
                    if (e.dxfContent != null) put("dxfContent", e.dxfContent)
                })
            }
        }.toString()

        fun decodeHistory(json: String?): List<ExportHistoryItem> {
            if (json.isNullOrBlank()) return emptyList()
            val arr = JSONArray(json)
            return (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ExportHistoryItem(
                    id = o.getString("id"),
                    title = o.optString("title"),
                    toolNameEn = o.optString("toolNameEn"),
                    toolNameAr = o.optString("toolNameAr"),
                    fileName = o.optString("fileName"),
                    timestampLabel = o.optString("timestampLabel"),
                    svgContent = o.optString("svgContent"),
                    dxfContent = if (o.has("dxfContent")) o.getString("dxfContent") else null
                )
            }
        }
    }
}
