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

        var currentNumber = ""
        var firstNumber = ""
        var operation = ""

        var one: Button = findViewById(R.id.button_one)
        one.setOnClickListener({
            currentNumber += "1"
            Display.setText(currentNumber)
        })

        var two: Button = findViewById(R.id.button_two)
        two.setOnClickListener({
            currentNumber += "2"
            Display.setText(currentNumber)
        })

        var three: Button = findViewById(R.id.button_three)
        three.setOnClickListener({
            currentNumber += "3"
            Display.setText(currentNumber)
        })

        var four: Button = findViewById(R.id.button_four)
        four.setOnClickListener({
            currentNumber += "4"
            Display.setText(currentNumber)
        })

        var five: Button = findViewById(R.id.button_five)
        five.setOnClickListener({
            currentNumber += "5"
            Display.setText(currentNumber)
        })

        var six: Button = findViewById(R.id.button_six)
        six.setOnClickListener({
            currentNumber += "6"
            Display.setText(currentNumber)
        })

        var seven: Button = findViewById(R.id.button_seven)
        seven.setOnClickListener({
            currentNumber += "7"
            Display.setText(currentNumber)
        })

        var eight: Button = findViewById(R.id.button_eight)
        eight.setOnClickListener({
            currentNumber += "8"
            Display.setText(currentNumber)
        })

        var nine: Button = findViewById(R.id.button_nine)
        nine.setOnClickListener({
            currentNumber += "9"
            Display.setText(currentNumber)
        })

        var nulle: Button = findViewById(R.id.button_null)
        nulle.setOnClickListener({
            currentNumber += "0"
            Display.setText(currentNumber)
        })

        var plus: Button = findViewById(R.id.button_plus)
        plus.setOnClickListener({
            if (currentNumber != "") {
                firstNumber = currentNumber
                currentNumber = ""
                operation = "+"
                Display.setText("+")
            }
        })

        var minus: Button = findViewById(R.id.button_minus)
        minus.setOnClickListener({
            if (currentNumber != "") {
                firstNumber = currentNumber
                currentNumber = ""
                operation = "-"
                Display.setText("-")
            }
        })

        var multiply: Button = findViewById(R.id.button_multiply)
        multiply.setOnClickListener({
            if (currentNumber != "") {
                firstNumber = currentNumber
                currentNumber = ""
                operation = "*"
                Display.setText("*")
            }
        })

        var divide: Button = findViewById(R.id.button_divide)
        divide.setOnClickListener({
            if (currentNumber != "") {
                firstNumber = currentNumber
                currentNumber = ""
                operation = "/"
                Display.setText("/")
            }
        })

        var point: Button = findViewById(R.id.button_point)
        point.setOnClickListener({
            if (!currentNumber.contains(".")) {
                if (currentNumber == "") {
                    currentNumber = "0."
                } else {
                    currentNumber += "."
                }
                Display.setText(currentNumber)
            }
        })

        var equal: Button = findViewById(R.id.button_equal)
        equal.setOnClickListener({
            if (firstNumber != "" && currentNumber != "" && operation != "") {
                var result = 0.0
                when (operation) {
                    "+" -> result = firstNumber.toDouble() + currentNumber.toDouble()
                    "-" -> result = firstNumber.toDouble() - currentNumber.toDouble()
                    "*" -> result = firstNumber.toDouble() * currentNumber.toDouble()
                    "/" -> result = firstNumber.toDouble() / currentNumber.toDouble()
                }

                if (result % 1 == 0.0) {
                    Display.setText(result.toInt().toString())
                } else {
                    Display.setText(result.toString())
                }

                firstNumber = ""
                currentNumber = Display.text.toString()
                operation = ""
            }
        })

        var clear: Button = findViewById(R.id.button_clear)
        clear.setOnClickListener({
            currentNumber = ""
            firstNumber = ""
            operation = ""
            Display.setText("0")
        })
    }
}