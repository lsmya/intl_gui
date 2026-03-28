package cn.lsmya.intl_gui.event

import kotlin.jvm.java

interface IntlRefreshMessage {
    fun onRefresh()

    companion object {
        val TOPIC = com.intellij.util.messages.Topic(
            "IntlRefreshMessage",
            IntlRefreshMessage::class.java
        )
    }
}