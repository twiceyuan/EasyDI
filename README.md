# EasyDI

一个 DI 库的简单实现。模仿 Koin 的 API 实现以最简单的方式实现依赖注入部分功能，学习使用。

## API 参考

定义：

```kotlin
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
```

使用时注入：

```kotlin

class MainActivity : AppCompatActivity() {

    private val speaker: Speaker by inject()

    // 用 activity 的 hashCode 作为 session id
    private val catScope = createCatScope(this.hashCode())
    
    private val catSpeaker: Speaker by catScope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onDefaultSpeak(view: View) {
        Toast.makeText(this, speaker.speak(), Toast.LENGTH_SHORT).show()
    }

    fun onCatSpeak(view: View) {
        Toast.makeText(this, catSpeaker.speak(), Toast.LENGTH_SHORT).show()
    }
}
```
