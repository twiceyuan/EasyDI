package com.twiceyuan.easydi.sample.di.impl

import com.twiceyuan.easydi.sample.di.SpeakMessage
import com.twiceyuan.easydi.sample.di.Speaker

class DefaultSpeaker(private val defaultMessage: SpeakMessage) : Speaker {

    override fun speak(): String {
        return defaultMessage.message
    }
}