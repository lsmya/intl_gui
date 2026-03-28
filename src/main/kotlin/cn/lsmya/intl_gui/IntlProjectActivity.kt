package cn.lsmya.intl_gui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.messages.MessageBusConnection

class IntlProjectActivity : ProjectActivity {
    private val docListenersMap = mutableMapOf<Project, PubspecDocumentListener>()

    companion object {
        private val LOG = Logger.getInstance(PubspecDocumentListener::class.java)
    }

    override suspend fun execute(project: Project) {
        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(FileDocumentManagerListener.TOPIC, PubspecDocumentListener(project))
    }
}