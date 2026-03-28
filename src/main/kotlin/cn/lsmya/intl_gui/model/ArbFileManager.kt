package cn.lsmya.intl_gui.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File

/**
 * ARB 文件数据模型
 */
data class ArbEntry(
    val key: String,
    var value: String,
    val attributes: JsonObject? = null
)

/**
 * ARB 文件管理器
 */
object ArbFileManager {

    private val gson = Gson()

    /**
     * 解析 ARB 内容字符串
     */
    fun parseArbContent(content: String): Map<String, ArbEntry> {
        val jsonObject = gson.fromJson(content, JsonObject::class.java)

        val entries = mutableMapOf<String, ArbEntry>()

        for ((key, value) in jsonObject.entrySet()) {
            if (key.startsWith("@")) continue // 跳过元数据键和属性键

            val attributes = jsonObject.get("@$key")?.asJsonObject
            val entry = ArbEntry(
                key = key,
                value = if (value.isJsonPrimitive) value.asString else value.toString(),
                attributes = attributes
            )
            entries[key] = entry
        }

        return entries
    }

    /**
     * 读取 ARB 文件
     */
    fun readArbFile(filePath: String): Map<String, ArbEntry> {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("文件不存在：$filePath")
        }

        val content = file.readText()
        return parseArbContent(content)
    }

    /**
     * 保存 ARB 文件
     */
    fun saveArbFile(filePath: String, entries: Map<String, ArbEntry>) {
        val jsonObject = JsonObject()

        for ((_, entry) in entries) {
            jsonObject.addProperty(entry.key, entry.value)
            entry.attributes?.let { attrs ->
                jsonObject.add("@${entry.key}", attrs)
            }
        }

        File(filePath).writeText(gson.toJson(jsonObject))
    }

    /**
     * 解析 ARB 文件的语言信息
     */
    fun getLanguageCode(jsonObject: JsonObject): String {
        return jsonObject.getAsJsonObject("@@")?.get("locale")?.asString ?: "en"
    }


    /**
     * 从文件名提取语言代码
     */
    fun extractLanguageCode(fileName: String): String {
        // app_en.arb -> en
        // app_zh_Hans.arb -> zh_Hans
        val regex = Regex("""app_(.+)\.arb""")
        return regex.find(fileName)?.groupValues?.get(1) ?: fileName
    }
}
