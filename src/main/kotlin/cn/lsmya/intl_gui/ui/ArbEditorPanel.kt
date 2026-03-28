package cn.lsmya.intl_gui.ui

import cn.lsmya.intl_gui.dialog.AddFieldDialog
import cn.lsmya.intl_gui.event.IntlRefreshMessage
import cn.lsmya.intl_gui.model.ArbEntry
import cn.lsmya.intl_gui.model.ArbFileManager
import cn.lsmya.intl_gui.model.ArbFileManager.extractLanguageCode
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.TableView
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

/**
 * ARB 文件编辑器面板
 */
class ArbEditorPanel(
    private val project: Project,
    private val parentDisposable: Disposable
) : JPanel(BorderLayout()) {

    private val connection: MessageBusConnection by lazy { project.messageBus.connect(parentDisposable) }


    // 初始化表格模型
    private val tableView: TableView<TranslationRow> = TableView(ListTableModel())

    // 数据存储
    private val l10nFiles = mutableListOf<VirtualFile>()
    private val allTranslations = mutableMapOf<String, MutableMap<String, ArbEntry>>() // fileName -> entries

    init {
        connection.subscribe(IntlRefreshMessage.TOPIC, object : IntlRefreshMessage {
            override fun onRefresh() {
                // 延时加载
                SwingUtilities.invokeLater { loadL10nFiles() }
            }
        })
        // 顶部工具栏
        val toolbar = createToolbar()
        add(toolbar, BorderLayout.NORTH)

        // 网格视图 - 使用 TableView
        val tableScroll = JBScrollPane(tableView)
        tableView.autoResizeMode = JTable.AUTO_RESIZE_NEXT_COLUMN
        tableView.rowHeight = 30
        add(tableScroll, BorderLayout.CENTER)

        loadL10nFiles()
    }

    private fun createToolbar(): JPanel {
        val panel = JPanel()

        val addFieldButton = JButton("新增字段", AllIcons.General.Add)
        addFieldButton.addActionListener {
            AddFieldDialog.show(project, l10nFiles, allTranslations)
        }
        panel.add(addFieldButton)

        val refreshButton = JButton("刷新", AllIcons.Actions.Refresh)
        refreshButton.addActionListener { loadL10nFiles() }
        panel.add(refreshButton)

        return panel
    }

    /**
     * 加载 lib/l10n 目录下的所有 ARB 文件
     */
    private fun loadL10nFiles() {
        l10nFiles.clear()
        allTranslations.clear()

        // 查找 lib/l10n 目录
        val baseDir = project.baseDir
        val libDir = baseDir.findChild("lib") ?: return
        val l10nDir = libDir.findChild("l10n") ?: return

        // 获取所有 ARB 文件
        val arbFiles = l10nDir.children.filter { it.extension == "arb" }.sortedBy { it.name }

        // 加载每个 ARB 文件
        arbFiles.forEach { file ->
            try {
                val content = VfsUtil.loadText(file)
                val entries = ArbFileManager.parseArbContent(content)
                allTranslations[file.name] = entries.toMutableMap()
                println("加载文件 ${file.name} 成功")
                l10nFiles.add(file)
            } catch (ex: Exception) {
                println("加载文件 ${file.name} 失败：${ex.message}")
            }
        }

        // 重建表格模型
        refreshTableModel()
    }

    /**
     * 刷新表格模型
     */
    private fun refreshTableModel() {
        // 收集所有的 key
        val allKeys = mutableSetOf<String>()
        allTranslations.values.forEach { entries ->
            allKeys.addAll(entries.keys)
        }

        // 构建所有列
        val columns = buildList {
            // 添加 Key 列
            add(object : ColumnInfo<TranslationRow, String>("Key") {
                override fun valueOf(item: TranslationRow): String = item.key
            })

            // 添加语言列
            l10nFiles.forEach { file ->
                val langCode = extractLanguageCode(file.name)
                add(object : ColumnInfo<TranslationRow, String>(langCode) {
                    override fun valueOf(item: TranslationRow): String {
                        val entries = allTranslations[file.name] ?: return ""
                        return entries[item.key]?.value ?: ""
                    }
                })
            }
        }

        // 设置行数据
        val rows = allKeys.sorted().map { key -> TranslationRow(key) }

        // 重新创建表格模型
        val newTableModel = object : ListTableModel<TranslationRow>(columns.toTypedArray(), rows) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return true
            }

            override fun setValueAt(value: Any?, row: Int, col: Int) {
                // 旧值
                val oldValue = getValueAt(row, col)?.toString() ?: ""
                // 新值
                val newValue = value?.toString() ?: ""
                //在这里更新你的数据！
                updateEditedValue(row, col, oldValue, newValue)
            }
        }

        // 更新表格模型
        tableView.model = newTableModel

        // 设置列宽均分窗口
        SwingUtilities.invokeLater {
            val tableWidth = tableView.width
            if (tableWidth > 0) {
                val columnCount = tableView.columnModel.columns.toList().size
                if (columnCount > 0) {
                    val preferredWidth = tableWidth / columnCount
                    for (i in 0 until columnCount) {
                        val column = tableView.columnModel.getColumn(i)
                        column.preferredWidth = preferredWidth
                        column.minWidth = preferredWidth / 2
                        column.maxWidth = preferredWidth * 2
                    }
                }
            }
        }
    }

    private fun updateEditedValue(row: Int, col: Int, oldValue: String, newValue: String) {
        if (oldValue == newValue) {
            return
        }
        if (col == 0) return // Key 列不允许修改
        val rowData = tableView.listTableModel.getItem(row)
        val key = rowData.key

        val fileIndex = col - 1
        if (fileIndex in l10nFiles.indices) {
            val fileName = l10nFiles[fileIndex].name
            allTranslations[fileName]?.get(key)?.value = newValue
            // 有针对性的只保存当前所对应的文件
            saveFile(l10nFiles[fileIndex])
        }
    }

    /**
     * 保存单个文件
     */
    private fun saveFile(file: VirtualFile): Int {
        val entries = allTranslations[file.name] ?: return 0
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
        return 1
    }


    /**
     * 保存所有文件
     */
    private fun saveAllFiles() {
        var savedCount = 0
        l10nFiles.forEach { file ->
            try {
                savedCount += saveFile(file)
            } catch (ex: Exception) {
                println("保存文件 ${file.name} 失败：${ex.message}")
            }
        }
        JOptionPane.showMessageDialog(
            this,
            "已保存 $savedCount 个文件",
            "成功",
            JOptionPane.INFORMATION_MESSAGE
        )
    }


    /**
     * 翻译行数据类
     */
    data class TranslationRow(val key: String)
}
