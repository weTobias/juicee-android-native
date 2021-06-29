package at.fhj.juicee

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class GoogleSignInActivity : AppCompatActivity() {
    private val RC_SIGN_IN: Int = 0
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        currentUser = Firebase.auth.currentUser

        //if there is a user
        if(currentUser != null){
            val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
            userInformationRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        //start MainScreenActivity
                        val intent = Intent(applicationContext,MainScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        //start InitialFormActivity
                        val intent = Intent(applicationContext,InitialFormActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener {
                    //Go Back to the first screen
                    redirectToStart()
                }
        } else {
            setContentView(R.layout.activity_google_sign_in)
            supportActionBar?.setDisplayShowTitleEnabled(false)


            //Initialize GoogleSignIn Button and Firebase Authentication
            val verify = findViewById<SignInButton>(R.id.google_SignIn)
            (verify.getChildAt(0) as TextView).text = getString(R.string.log_in_with_google)
            auth = Firebase.auth

            //create a request for the user
            createRequest()

            //sign the user in
            verify.setOnClickListener {
                signIn()
            }
        }
    }

    // Configure Google Sign In
    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                 //show error Message on failed login
                Toast.makeText(applicationContext, "Failed To Login", Toast.LENGTH_SHORT).show()
                redirectToStart()
               
            }
        }
    }


    //authenticate with Google and change screen when login successful
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    currentUser= auth.currentUser
                    val intent = Intent(applicationContext,InitialFormActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, redirect to first screen
                    redirectToStart()
                }
            }
    }

    private fun redirectToStart() {
        val intent = Intent(applicationContext,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}