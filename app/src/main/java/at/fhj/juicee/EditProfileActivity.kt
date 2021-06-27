package at.fhj.juicee

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private var image: ImageView?= null
    var imageURI : Uri? = null
    var storage : FirebaseStorage? = null
    var storageReference: StorageReference? =null
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val back : ImageButton? = findViewById(R.id.backButton)
        image = findViewById(R.id.editProfileImage);

        storage = Firebase.storage
        storageReference = storage?.reference

            image?.setOnClickListener{
            choosePicture()
        }

        back?.setOnClickListener {
            val intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
        }

    }
    private fun choosePicture(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //check if requestCode ist correct and data is not null
        if(requestCode == 1 && data != null && data.data !=null){
            imageURI = data.data
            image?.setImageURI(imageURI)
            uploadPicture()
        }

    }

    private fun uploadPicture(){
        //Progess Dialog for showing the progress of the download
        val pd = ProgressDialog(this)

        //Title of Dialog
        pd.setTitle("Uploading Image")
        pd.show()

        //Random key for naimng the file
        val randomKey : String = UUID.randomUUID().toString()

        val riversRef = storageReference?.child("images/$randomKey")
        val uploadTask = imageURI?.let { riversRef?.putFile(it) }

        // Register observers to listen for when the download is done or if it fails
        uploadTask?.addOnFailureListener {
            pd.dismiss()
            Toast.makeText(applicationContext, "Failed To Upload", Toast.LENGTH_SHORT).show()
        }?.addOnSuccessListener {
            pd.dismiss()
            Snackbar.make(findViewById(R.id.edit_profile_activity),"Image Uploaded",Snackbar.LENGTH_SHORT).show()
        }?.addOnProgressListener {  taskSnapshot ->
            //Show the percentage of the download
            val progressPercent = (100.00 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            pd.setMessage("Progress:" + progressPercent.toInt() + "%")
        }
    }
}