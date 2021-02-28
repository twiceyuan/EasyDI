package com.twiceyuan.easydi.sample.di.impl

import com.twiceyuan.easydi.Scope
import com.twiceyuan.easydi.inject
import com.twiceyuan.easydi.sample.di.SpeakMessage
import com.twiceyuan.easydi.sample.di.Speaker

class CatSpeaker(private val scope: Scope) : Speaker, Scope by scope {

    private val id: Int by inject()
    private val message: SpeakMessage by inject()

    override fun speak(): String {
        return "Cat($id) speak: $message"
    }
}