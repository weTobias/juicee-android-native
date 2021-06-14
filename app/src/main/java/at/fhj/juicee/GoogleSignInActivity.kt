package at.fhj.juicee

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private val TAG : String = "SignInActivity"
    private val RC_SIGN_IN: Int = 0
    private var googleSignInClient: GoogleSignInClient? = null
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val db: FirebaseFirestore = Firebase.firestore


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser= auth.currentUser

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
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_sign_in)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val verify = findViewById<SignInButton>(R.id.google_SignIn)
        auth = Firebase.auth

        createRequest()

        verify.setOnClickListener {
            signIn()
        }
    }

    private fun createRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient?.signInIntent
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
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

                val intent = Intent(applicationContext,InitialFormActivity::class.java)
                startActivity(intent)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                //Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    currentUser= auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}