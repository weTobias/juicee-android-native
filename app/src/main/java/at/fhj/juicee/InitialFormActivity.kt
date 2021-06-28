package at.fhj.juicee

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import at.fhj.juicee.models.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InitialFormActivity : AppCompatActivity() {
    private val TAG : String = "InitialFormActivity"
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        db = Firebase.firestore
        currentUser = Firebase.auth.currentUser
        val userId = currentUser?.uid

        if(currentUser != null){
            val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
            userInformationRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(applicationContext,MainScreenActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { _ ->
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    startActivity(intent)
                }
        }

        setContentView(R.layout.activity_initial_form)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnPlus = findViewById<Button>(R.id.plusButton)
        val btnMinus = findViewById<Button>(R.id.minusButton)
        val waterNumberInitialForm = findViewById<TextView>(R.id.waterNumberInitialForm)
        var waterNumber = 0

        btnPlus.setOnClickListener{
            waterNumber += 1
            waterNumberInitialForm.setText(waterNumber.toString())
        }
        btnMinus.setOnClickListener{
            if (waterNumber > 0) {
                waterNumber -= 1
                waterNumberInitialForm.setText(waterNumber.toString())
            }
        }

        val btnSubmit = findViewById<Button>(R.id.initalFormConfirmButton)
        btnSubmit.setOnClickListener {
            val heightInput = findViewById<TextInputEditText>(R.id.heightInput)
            val weightInput = findViewById<TextInputEditText>(R.id.weightInput)
            val ageInput = findViewById<TextInputEditText>(R.id.ageInput)
            val activityLevelInput = findViewById<TextInputEditText>(R.id.activityLevelInput)
            val genderToggle = findViewById<MaterialButtonToggleGroup>(R.id.genderToggle)
            val gender = if (genderToggle.checkedButtonId == R.id.buttonMale) {
                Gender.Male
            } else {
                Gender.Female
            }
            val userInformation = UserInformation(heightInput.text.toString().toFloat(), weightInput.text.toString().toFloat(), ageInput.text.toString().toInt(), activityLevelInput.text.toString().toInt(), gender)
            if (userId != null) {
                db.collection("userInformation").document(userId).set(userInformation).addOnSuccessListener {
                    db.collection("beverages").document("water").get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val dailyBeverageConsumption = DailyBeverageConsumption()
                            val water = document.toObject<Beverage>()!!
                            for (index in 0 until waterNumber) {
                                dailyBeverageConsumption.consumptions.add(BeverageConsumption(water, 250))
                            }
                            db.collection("dailyBeverageConsumptions").document(currentUser?.uid.toString()).collection("dailyConsumptions").document(
                                (LocalDateTime.now()).format(
                                    DateTimeFormatter.BASIC_ISO_DATE)).set(dailyBeverageConsumption).addOnSuccessListener {
                                val intent = Intent(this, MainScreenActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent)
                            }
                        }
                    }.addOnFailureListener { _ ->
                        val intent = Intent(applicationContext,MainActivity::class.java)
                        startActivity(intent)
                    }
                }.addOnFailureListener { _ ->
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

}