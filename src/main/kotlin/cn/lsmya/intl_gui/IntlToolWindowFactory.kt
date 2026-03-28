package cn.lsmya.intl_gui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Intl GUI 工具窗口工厂
 */
class IntlToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val intlPanel = IntlToolWindowPanel(toolWindow.disposable)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(intlPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
