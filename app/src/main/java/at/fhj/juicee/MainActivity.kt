package at.fhj.juicee

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_start_screen)

        val btnStart = findViewById<Button>(R.id.startButton)
        if (isNetworkAvailbale()) {
            btnStart.setOnClickListener {
                db = Firebase.firestore
                currentUser = Firebase.auth.currentUser
                if(currentUser != null){
                    val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
                    userInformationRef.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
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
                } else {
                    val intent = Intent(this, GoogleSignInActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }
            }
        }
    }

    fun  isNetworkAvailbale():Boolean{
        val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val internetInfo =conManager.activeNetworkInfo
        return internetInfo!=null && internetInfo.isConnected
    }
}