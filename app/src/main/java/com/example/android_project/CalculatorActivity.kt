package com.example.android_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calculator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var Display: TextView = findViewById(R.id.display)
        Display.text = "0"

        var one: Button = findViewById(R.id.button_one)
        one.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "1"
            }
            else {
                Display.setText(Display.text.toString() + "1")
            }
        })

        var two: Button = findViewById(R.id.button_two)
        two.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "2"
            }
            else {
                Display.setText(Display.text.toString() + "2")
            }
        })

        var three: Button = findViewById(R.id.button_three)
        three.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "3"
            }
            else {
                Display.setText(Display.text.toString() + "3")
            }
        })

        var four: Button = findViewById(R.id.button_four)
        four.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "4"
            }
            else {
                Display.setText(Display.text.toString() + "4")
            }
        })

        var five: Button = findViewById(R.id.button_five)
        five.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "5"
            }
            else {
                Display.setText(Display.text.toString() + "5")
            }
        })

        var six: Button = findViewById(R.id.button_six)
        six.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "6"
            }
            else {
                Display.setText(Display.text.toString() + "6")
            }
        })

        var seven: Button = findViewById(R.id.button_seven)
        seven.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "7"
            }
            else {
                Display.setText(Display.text.toString() + "7")
            }
        })

        var eight: Button = findViewById(R.id.button_eight)
        eight.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "8"
            }
            else {
                Display.setText(Display.text.toString() + "8")
            }
        })

        var nine: Button = findViewById(R.id.button_nine)
        nine.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "9"
            }
            else {
                Display.setText(Display.text.toString() + "9")
            }
        })

        var nulle: Button = findViewById(R.id.button_null)
        nulle.setOnClickListener({
            if(Display.text == "0") {
                Display.text = "0"
            }
            Display.setText(Display.text.toString() + "0")
        })

        var plus: Button = findViewById(R.id.button_plus)
        plus.setOnClickListener({
            Display.setText(Display.text.toString() + "+")
        })

        var minus: Button = findViewById(R.id.button_minus)
        minus.setOnClickListener({
            Display.setText(Display.text.toString() + "-")
        })

        var multiply: Button = findViewById(R.id.button_multiply)
        multiply.setOnClickListener({
            Display.setText(Display.text.toString() + "*")
        })

        var divide: Button = findViewById(R.id.button_divide)
        divide.setOnClickListener({
            Display.setText(Display.text.toString() + "/")
        })

        var point: Button = findViewById(R.id.button_point)
        point.setOnClickListener({
            Display.setText(Display.text.toString() + ".")
        })

        var equal: Button = findViewById(R.id.button_equal)
        equal.setOnClickListener({
            val text = Display.text.toString()
            var result = 0.0

            if (text.contains("+")) {
                val numbers = text.split("+")
                result = numbers[0].toDouble() + numbers[1].toDouble()
            } else if (text.contains("-")) {
                val numbers = text.split("-")
                result = numbers[0].toDouble() - numbers[1].toDouble()
            } else if (text.contains("*")) {
                val numbers = text.split("*")
                result = numbers[0].toDouble() * numbers[1].toDouble()
            } else if (text.contains("/")) {
                val numbers = text.split("/")
                result = numbers[0].toDouble() / numbers[1].toDouble()
            }

            if (result % 1 == 0.0) {
                Display.setText(result.toInt().toString())
            } else {
                Display.setText(result.toString())
            }
        })

        var clear: Button = findViewById(R.id.button_clear)
        clear.setOnClickListener({
            Display.setText("0")
        })
    }
}