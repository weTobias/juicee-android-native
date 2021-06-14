package at.fhj.juicee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val TAG : String = "StartActivity"
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnStart = findViewById<Button>(R.id.startButton)
        btnStart.setOnClickListener {
            db = Firebase.firestore
            currentUser = Firebase.auth.currentUser

            if(currentUser != null){
                val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
                userInformationRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val intent = Intent(applicationContext,MainScreenActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(applicationContext,InitialFormActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
            }
            val intent = Intent(this, GoogleSignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }


    }
}