package at.fhj.juicee

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

/**
 * Initial user input if no userdata exists.
 */
class InitialFormActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private var userId: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        db = Firebase.firestore
        currentUser = Firebase.auth.currentUser
        userId = currentUser?.uid

        // check if user is logged in. If not, redirect to sign in activity
        if(currentUser != null){
            // check if user information exists. If not, setup screen
            val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
            userInformationRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(applicationContext,MainScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        setupScreen()
                    }
                }
                .addOnFailureListener {
                    redirectToStart()
                }
        } else {
            redirectToLogin()
        }
    }

    /**
     * Screen setup for data input and submission.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupScreen() {
        setContentView(R.layout.activity_initial_form)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnPlus = findViewById<Button>(R.id.plusButton)
        val btnMinus = findViewById<Button>(R.id.minusButton)
        val waterNumberInitialForm = findViewById<TextView>(R.id.waterNumberInitialForm)
        var waterNumber = 0

        btnPlus.setOnClickListener{
            waterNumber += 1
            waterNumberInitialForm.text = waterNumber.toString()
        }
        btnMinus.setOnClickListener{
            if (waterNumber > 0) {
                waterNumber -= 1
                waterNumberInitialForm.text = waterNumber.toString()
            }
        }

        val btnSubmit = findViewById<Button>(R.id.FormConfirmButton)

        // transform data and submit
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
                db.collection("userInformation").document(userId!!).set(userInformation).addOnSuccessListener {
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
                                finish()
                            }
                        }
                    }.addOnFailureListener {
                        redirectToStart()
                    }
                }.addOnFailureListener {
                    redirectToStart()
                }
            }
        }
    }

    /**
     * Start starting activity and close all other activities
     */
    private fun redirectToStart() {
        val intent = Intent(applicationContext,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Start sign in activity and close all other activities
     */
    private fun redirectToLogin() {
        val intent = Intent(applicationContext,GoogleSignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}