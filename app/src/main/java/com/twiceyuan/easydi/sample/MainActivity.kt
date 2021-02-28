package com.twiceyuan.easydi.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.twiceyuan.easydi.inject
import com.twiceyuan.easydi.sample.di.Speaker

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