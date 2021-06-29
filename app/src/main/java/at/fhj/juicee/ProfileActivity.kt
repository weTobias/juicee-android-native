package at.fhj.juicee

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ProfileActivity : AppCompatActivity() {
    private lateinit var storageReference: StorageReference
    private lateinit var storage : FirebaseStorage
    private lateinit var image: ImageView
    private lateinit var db: FirebaseFirestore
    private val uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val back : ImageButton? = findViewById(R.id.backButton)
        val name : TextView? = findViewById(R.id.user_name)
        val logout : Button? =findViewById(R.id.google_SignIn)
        val edit : Button? =findViewById(R.id.FormConfirmButton)

        val activityLevelValue : TextView = findViewById(R.id.activityLevelValue)
        val ageValue : TextView = findViewById(R.id.ageValue)
        val heightValue : TextView = findViewById(R.id.heightValue)
        val weightValue : TextView = findViewById(R.id.weightValue)

        image = findViewById(R.id.profileImage)
        storage = Firebase.storage
        storageReference = storage.reference
        db = Firebase.firestore



        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val key = sharedPreferences.getString("key", "")

        storageReference.child("images/$key").downloadUrl.addOnSuccessListener { taskSnapShot ->
            Glide.with(this).load(taskSnapShot).into(image)
        }

        val acct : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)

        name?.text = acct?.displayName


        //get document from the collection with id of the current user
        val docRef = db.collection("userInformation").document(uid)
        docRef.get()
            //if success
            .addOnSuccessListener { document ->
                if (document != null) {
                    //get data from Firestore document
                    val activityLevel: Long? = document.getLong("activityLevel")
                    val age: Long? = document.getLong("age")
                    val height: Double? = document.getDouble("height")
                    val weight: Double? = document.getDouble("weight")

                    //set data to fields
                    activityLevelValue.text = activityLevel.toString()
                    ageValue.text = age.toString()
                    heightValue.text = height.toString()
                    weightValue.text = weight.toString()
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }



        edit?.setOnClickListener{
            val intent = Intent(applicationContext,EditProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        back?.setOnClickListener {
            onBackPressed()
        }




        logout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, GoogleSignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Firebase.auth.signOut()
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}