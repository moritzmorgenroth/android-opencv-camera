package de.moritzmorgenroth.testapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import de.moritzmorgenroth.opencvtest.RecognizerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, RecognizerActivity::class.java)
        startActivity(intent)
    }
}
