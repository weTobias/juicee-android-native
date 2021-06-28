package at.fhj.juicee

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var imageURI : Uri
    private lateinit var imageKey : String
    private lateinit var storage : FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var db: FirebaseFirestore
    private val uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val back = findViewById<ImageButton>(R.id.backButton)
        val activityLevelInput: TextInputEditText = findViewById(R.id.activityLevelInput)
        val ageInput : TextInputEditText = findViewById(R.id.ageInput)
        val heightInput : TextInputEditText = findViewById(R.id.heightInput)
        val weightInput : TextInputEditText = findViewById(R.id.weightInput)
        val btnSubmit = findViewById<Button>(R.id.FormConfirmButton)

        loadImageKey()

        image = findViewById(R.id.editProfileImage)
        storage = Firebase.storage
        storageReference = storage.reference
        db = Firebase.firestore

        storageReference.child("images/$imageKey").downloadUrl.addOnSuccessListener { taskSnapShot ->
           Glide.with(this).load(taskSnapShot).into(image)
        }

            image.setOnClickListener{
            choosePicture()
        }

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
                    activityLevelInput.setText(activityLevel.toString())
                    ageInput.setText(age.toString())
                    heightInput.setText(height.toString())
                    weightInput.setText(weight.toString())
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                redirectToStart()
            }

        //Go Back to Profile
        back?.setOnClickListener {
            onBackPressed()
        }

        btnSubmit?.setOnClickListener{

            //Update Data of Database onClick
            val docRefUpdate = db.collection("userInformation").document(uid)
            docRefUpdate.update(mapOf(
                "activityLevel" to activityLevelInput.text.toString().toLong(),
                "age" to ageInput.text.toString().toLong(),
                "height" to heightInput.text.toString().toDouble(),
                "weight" to weightInput.text.toString().toDouble()
            ))
        }
    }

    //lets you choose a picture from the device
    private fun choosePicture(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check if requestCode ist correct and data is not null
        if(requestCode == 1 && data != null && data.data !=null){
            imageURI = data.data!!
            image.setImageURI(imageURI)

            //starts the upload process
            uploadPicture()
        }
    }

    private fun uploadPicture(){
        //Progress Dialog for showing the progress of the download
        val pd = ProgressDialog(this)

        //key for naming the file
        val randomKey : String = UUID.randomUUID().toString()

        //set the key of the image in the shared preferences
        imageKey(uid)

        val riversRef = storageReference.child("images/$uid")
        //Title of Dialog
        pd.setTitle("Uploading Image")
        pd.show()
        val uploadTask = imageURI.let { riversRef.putFile(it) }

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {

            pd.dismiss()
            Toast.makeText(applicationContext, "Failed To Upload", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener {  taskSnapshot ->
            //Show the percentage of the download
            val progressPercent = (100.00 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            pd.setMessage("Progress:" + progressPercent.toInt() + "%")

        }.addOnSuccessListener {
            pd.dismiss()
            Snackbar.make(findViewById(R.id.edit_profile_activity),"Image Uploaded",Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun imageKey(string: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString("key", string)
        editor.apply()
    }

    private fun loadImageKey() {
         val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
         val key = sharedPreferences.getString("key", "")
        if (key != null) {
            imageKey = key
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
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