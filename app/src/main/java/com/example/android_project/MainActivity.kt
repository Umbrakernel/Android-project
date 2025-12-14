package com.example.android_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import com.example.android_project.LocationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bGoToCalculator: Button = findViewById(R.id.bGoToCalculator)
        bGoToCalculator.setOnClickListener({
            val calculatorIntent = Intent(this, CalculatorActivity::class.java)
            startActivity(calculatorIntent)
        })
        val bGoToPlayer: Button = findViewById(R.id.bGoToPlayer)
        bGoToPlayer.setOnClickListener({
            val playerIntent = Intent(this, MediaPlayerActivity::class.java)
            startActivity(playerIntent)
        })
        /*
        val bGoToViews: Button = findViewById(R.id.bGoToViews)
        bGoToViews.setOnClickListener({
            val viewsIntent = Intent(this, ViewsActivity::class.java)
            startActivity(viewsIntent)
        })
        */
        val bGoToLocation: Button = findViewById(R.id.bGoToLocation)
        bGoToLocation.setOnClickListener({
            val locationIntent = Intent(this, LocationActivity::class.java)
            startActivity(locationIntent)
        })
    }
}