package com.twiceyuan.easydi.sample

import com.twiceyuan.easydi.*
import com.twiceyuan.easydi.sample.di.SpeakMessage
import com.twiceyuan.easydi.sample.di.Speaker
import com.twiceyuan.easydi.sample.di.impl.CatSpeaker
import com.twiceyuan.easydi.sample.di.impl.DefaultSpeaker

fun initDI() {

    // 全局的注入定义
    EasyDI.define {
        // 注入默认字符串
        single {
            SpeakMessage("这是全局的默认消息")
        }

        // Default Speaker
        single<Speaker> {
            DefaultSpeaker(get())
        }
    }

    // 定义一个猫的 scope
    EasyDI.defineScope(catQualifier) {

        val id = properties[PROP_KEY_ID] as Int

        single { id }

        factory {
            // 随机喵 1-5 次
            val message = (0..(Math.random() * 5).toInt()).joinToString("") { "喵" }
            SpeakMessage(message)
        }

        single<Speaker> {
            CatSpeaker(this)
        }
    }
}

// 猫的限定符
val catQualifier = named("Cat")

// Cat ID，猫有很多只，每只要给个 id，比如 activity 的 hashCode
const val PROP_KEY_ID = "id"

// 创建猫的 scope
fun createCatScope(sessionId: Int) =
    EasyDI.getOrCreateScope(sessionId.toString(), catQualifier, mapOf(PROP_KEY_ID to sessionId))
