package com.example.projmanage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.projmanage.firebase.FireStore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed(
            {
                var currentUserId = FireStore().getCurrentUserId()
                if(currentUserId != null)
                {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else
                {
                    startActivity(Intent(this, IntroActivity::class.java))
                }
                finish()
            }, 2500
        )
    }
}