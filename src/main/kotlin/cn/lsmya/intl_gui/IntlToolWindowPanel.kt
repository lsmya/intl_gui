package cn.lsmya.intl_gui

import cn.lsmya.intl_gui.ui.ArbEditorPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Intl GUI 工具窗口主面板
 */
class IntlToolWindowPanel(
    private val project: Project,
    private val parentDisposable: Disposable
) : JPanel(BorderLayout()) {

    init {
        val arbEditorPanel = ArbEditorPanel(project, parentDisposable)
        add(arbEditorPanel, BorderLayout.CENTER)
    }
}
