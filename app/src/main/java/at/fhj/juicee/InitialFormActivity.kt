package at.fhj.juicee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import at.fhj.juicee.models.Gender
import at.fhj.juicee.models.UserInformation
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InitialFormActivity : AppCompatActivity() {
    private val TAG : String = "InitialFormActivity"
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser != null){
            val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
            userInformationRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val intent = Intent(applicationContext,MainScreenActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore
        currentUser = Firebase.auth.currentUser
        val userId = currentUser?.uid

        setContentView(R.layout.activity_initial_form)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
                    val intent = Intent(this, MainScreenActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }
    }

}