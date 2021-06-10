package at.fhj.juicee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        var name : TextView? = findViewById(R.id.user_name)
        var mail : TextView?  = findViewById(R.id.mail)
        val logout : Button? =findViewById(R.id.google_SignIn)

        val acct : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)

            name?.text = acct?.displayName
            mail?.text = acct?.email


        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext,GoogleSignInActivity::class.java)
            startActivity(intent)
            Firebase.auth.signOut()
        }
    }
}