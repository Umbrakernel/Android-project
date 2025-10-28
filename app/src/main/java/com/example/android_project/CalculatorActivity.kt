package com.example.android_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val display: TextView = findViewById(R.id.display)

        findViewById<Button>(R.id.button_one).setOnClickListener { addToDisplay("1", display) }
        findViewById<Button>(R.id.button_two).setOnClickListener { addToDisplay("2", display) }
        findViewById<Button>(R.id.button_three).setOnClickListener { addToDisplay("3", display) }
        findViewById<Button>(R.id.button_four).setOnClickListener { addToDisplay("4", display) }
        findViewById<Button>(R.id.button_five).setOnClickListener { addToDisplay("5", display) }
        findViewById<Button>(R.id.button_six).setOnClickListener { addToDisplay("6", display) }
        findViewById<Button>(R.id.button_seven).setOnClickListener { addToDisplay("7", display) }
        findViewById<Button>(R.id.button_eight).setOnClickListener { addToDisplay("8", display) }
        findViewById<Button>(R.id.button_nine).setOnClickListener { addToDisplay("9", display) }
        findViewById<Button>(R.id.button_null).setOnClickListener { addToDisplay("0", display) }

        findViewById<Button>(R.id.button_plus).setOnClickListener { addToDisplay("+", display) }
        findViewById<Button>(R.id.button_minus).setOnClickListener { addToDisplay("-", display) }
        findViewById<Button>(R.id.button_multiply).setOnClickListener { addToDisplay("*", display) }
        findViewById<Button>(R.id.button_divide).setOnClickListener { addToDisplay("/", display) }
        findViewById<Button>(R.id.button_point).setOnClickListener { addToDisplay(".", display) }

        findViewById<Button>(R.id.button_clear).setOnClickListener {
            display.text = "0"
        }

        findViewById<Button>(R.id.button_equal).setOnClickListener {
            val result = eval(display.text.toString())
            display.text = result.toString()
        }
    }

    private fun addToDisplay(text: String, display: TextView) {
        if (display.text == "0") {
            display.text = text
        } else {
            display.text = display.text.toString() + text
        }
    }

    private fun eval(str: String): Double {
        return when {
            "+" in str -> str.split("+")[0].toDouble() + str.split("+")[1].toDouble()
            "-" in str -> str.split("-")[0].toDouble() - str.split("-")[1].toDouble()
            "*" in str -> str.split("*")[0].toDouble() * str.split("*")[1].toDouble()
            "/" in str -> str.split("/")[0].toDouble() / str.split("/")[1].toDouble()
            else -> str.toDouble()
        }
    }
}