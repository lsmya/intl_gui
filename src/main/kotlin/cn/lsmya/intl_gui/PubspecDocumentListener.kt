package cn.lsmya.intl_gui

import cn.lsmya.intl_gui.event.IntlRefreshMessage
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project

/** pubspec.yaml 文档保存监听器 只在保存时检测配置变更并触发生成 */
class PubspecDocumentListener(private val project: Project) : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        val file = FileDocumentManager.getInstance().getFile(document)
        if (file?.extension != "arb") {
            return
        }
        project.messageBus.syncPublisher(IntlRefreshMessage.TOPIC).onRefresh()
    }
}
