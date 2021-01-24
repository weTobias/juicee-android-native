package com.example.juicee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class InitialFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_form)

        val btnPlus = findViewById<Button>(R.id.plusButton)
        val btnMinus = findViewById<Button>(R.id.minusButton)
        val waterNumberInitialForm = findViewById<TextView>(R.id.waterNumberInitialForm)

        btnPlus.setOnClickListener{
            var number = waterNumberInitialForm.getText().toString().toInt()
            number += 1
            waterNumberInitialForm.setText(number.toString())
        }
        btnMinus.setOnClickListener{
            var number = waterNumberInitialForm.getText().toString().toInt()
            if (number > 0) {
                number -= 1
                waterNumberInitialForm.setText(number.toString())
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}