package cn.lsmya.intl_gui.utils

import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project

// 显示成功提示（Toast 效果）
fun showSuccessToast(project: Project, content: String) {
    val notification = Notification(
        "cn.lsmya.intl_gui",  // 分组ID，随便写固定值
        "Intl GUI",     // 标题
        content,        // 内容
        NotificationType.INFORMATION
    )
    Notifications.Bus.notify(notification, project)
}