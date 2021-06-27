package at.fhj.juicee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

        val back : ImageButton? = findViewById(R.id.backButton)
        val name : TextView? = findViewById(R.id.user_name)
        val logout : Button? =findViewById(R.id.google_SignIn)
        val edit : Button? =findViewById(R.id.initalFormConfirmButton)
        //val image: ImageView? = findViewById(R.id.profileImage)

        val acct : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)

            name?.text = acct?.displayName
            //image?.setImageURI(acct?.photoUrl)

        edit?.setOnClickListener{
            val intent = Intent(applicationContext,EditProfileActivity::class.java)
            startActivity(intent)
        }

        back?.setOnClickListener {
            val intent = Intent(applicationContext,MainScreenActivity::class.java)
            startActivity(intent)
        }


        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext,GoogleSignInActivity::class.java)
            startActivity(intent)
            Firebase.auth.signOut()
        }
    }
}