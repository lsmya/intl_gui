package cn.lsmya.intl_gui

import cn.lsmya.intl_gui.ui.ArbEditorPanel
import com.intellij.openapi.Disposable
import javax.swing.JPanel
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.SwingConstants
import com.intellij.openapi.project.ProjectManager

/**
 * Intl GUI 工具窗口主面板
 */
class IntlToolWindowPanel(private val parentDisposable: Disposable) : JPanel(BorderLayout()) {

    init {
        // 获取当前项目
        val project = ProjectManager.getInstance().openProjects.firstOrNull()

        if (project != null) {
            // 添加 ARB 编辑器
            val arbEditorPanel = ArbEditorPanel(project, parentDisposable)
            add(arbEditorPanel, BorderLayout.CENTER)
        } else {
            // 没有打开的项目时显示提示
            val welcomeLabel = JLabel("Flutter Intl 国际化管理器", SwingConstants.CENTER)
            add(welcomeLabel, BorderLayout.CENTER)
        }
    }
}
