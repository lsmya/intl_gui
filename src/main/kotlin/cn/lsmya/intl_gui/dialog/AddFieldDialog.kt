package cn.lsmya.intl_gui.dialog

import cn.lsmya.intl_gui.event.IntlRefreshMessage
import cn.lsmya.intl_gui.model.ArbEntry
import cn.lsmya.intl_gui.model.ArbFileManager
import cn.lsmya.intl_gui.utils.showSuccessToast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import com.intellij.ui.dsl.builder.columns

/**
 * 新增字段对话框
 */
class AddFieldDialog private constructor(
    private val project: Project,
    private val l10nFiles: MutableList<VirtualFile>,
    // 所有翻译文件的翻译内容
    private val allTranslations: MutableMap<String, MutableMap<String, ArbEntry>>
) : DialogWrapper(project) {

    companion object {
        fun show(
            project: Project,
            l10nFiles: MutableList<VirtualFile>,
            allTranslations: MutableMap<String, MutableMap<String, ArbEntry>>
        ) {
            val dialog = AddFieldDialog(
                project, l10nFiles,
                allTranslations = allTranslations
            )
            dialog.show()
        }
    }

    private lateinit var keyNameField: JBTextField

    private val valueFields = mutableMapOf<String, JBTextField>()

    init {
        title = "新增字段"
        isResizable = false
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("字段名:") {
                keyNameField = textField()
                    .columns(30)
                    .apply {
                        component.toolTipText = "例如：title, button_submit, error_message 等"
                        component.text = ""
                    }
                    .component
            }
            separator()
            valueFields.clear()
            for (file in l10nFiles) {
                row(ArbFileManager.extractLanguageCode(file.name)) {
                    textField()
                        .columns(30)
                        .apply {
                            component.text = ""
                        }
                        .component.apply {
                            valueFields[file.name] = this
                        }
                }
            }
            row {
                label("<html><font color='gray'>提示：为每个语言输入该字段的翻译值</font></html>")
            }
        }
    }

    // 表单验证
    override fun doValidate(): ValidationInfo? {
        val keyName = keyNameField.text.trim()
        if (keyName.isBlank()) {
            return ValidationInfo("请输入字段名称", keyNameField)
        }
        // 验证 Key 格式（不能包含特殊字符）
        if (!Regex("^[a-zA-Z][a-zA-Z0-9_]*\$").matches(keyName)) {
            return ValidationInfo("Key 必须以字母开头，只能包含字母、数字和下划线", keyNameField)
        }
        // 检查是否已存在
        val allKeys = allTranslations.values.flatMap { it.keys }.toSet()
        if (allKeys.contains(keyName)) {
            return ValidationInfo("该 Key 已存在", keyNameField)
        }
        return null
    }

    override fun doOKAction() {
        // 创建新字段
        try {
            val valuesMap = valueFields.mapValues { (_, textArea) ->
                textArea.text.trim()
            }
            val key = keyNameField.text.trim()
            addNewFieldToAllFiles(
                key = key,
                valuesMap = valuesMap,
                allTranslations = allTranslations,
                l10nFiles = l10nFiles
            )
            showSuccessToast(project, "字段创建成功")
            project.messageBus.syncPublisher(IntlRefreshMessage.TOPIC).onRefresh()
        } catch (ex: Exception) {
            showSuccessToast(project, "创建失败：${ex.message}")
        }
        super.doOKAction()
    }

    /**
     * 在所有文件中添加新字段
     */
    private fun addNewFieldToAllFiles(
        key: String,
        valuesMap: Map<String, String>,
        allTranslations: MutableMap<String, MutableMap<String, ArbEntry>>,
        l10nFiles: MutableList<VirtualFile>,
    ) {
        // 为每个语言文件添加新字段
        allTranslations.forEach { (fileName, entries) ->
            // 从 Map 中获取对应语言的值，如果没有则为空字符串
            val value = valuesMap[fileName] ?: ""

            // 添加新字段
            entries[key] = ArbEntry(key, value, null)
        }
        // 保存所有文件
        if (l10nFiles.isNotEmpty()) {
            saveAllFiles()
        }
    }

    private fun saveAllFiles() {
        for (file in l10nFiles) {
            val entries = allTranslations[file.name] ?: return
            val jsonObject = JsonObject()
            entries.forEach { (_, entry) ->
                jsonObject.addProperty(entry.key, entry.value)
                entry.attributes?.let { attrs ->
                    jsonObject.add("@${entry.key}", attrs)
                }
            }
            ApplicationManager.getApplication().runWriteAction {
                VfsUtil.saveText(file, GsonBuilder().setPrettyPrinting().create().toJson(jsonObject))
            }
        }

    }

}