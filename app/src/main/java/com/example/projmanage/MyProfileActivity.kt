package com.example.projmanage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projmanage.databinding.ActivityMyProfileBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.lang.Exception

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri?= null
    private lateinit var mUserDetails:User
    private var mProfileImageUrl: String = ""

    lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        FireStore().loadUserData(this)

        binding.ivUserImage.setOnClickListener {

            if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)

            }
            else
            {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.btnupdate.setOnClickListener {
            if(mSelectedImageFileUri != null)
            {
                uploadUSerImage()
            }
            else
            {
                showProgressDialog("Please Wait")
                updateUserProfileData()
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK
            && requestCode == Constants. PICK_IMAGE_REQUEST_CODE
            && data != null && data.data != null)
        {
            mSelectedImageFileUri = data.data

            try {
                Glide.with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri.toString())
                    .centerCrop()
                    .into(binding.ivUserImage)
            }
            catch (e:IOException)
            {
                e.printStackTrace()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE)
        {
            if(grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this)
            }
        }
        else
        {
            Toast.makeText(
                this, "Opps, you just denied permission for storage",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(binding.myProfileToolbar)

        val actionBar = supportActionBar
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.setTitle("Sign Up")
        }
    }

    fun updateProfileDetails(user: User)
    {

        mUserDetails = user

        Glide.with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .into(binding.ivUserImage)
        binding.etprofilename.setText(user.name)
        binding.etprofileemail.setText(user.email)
        if(user.mobile != 0L)
        {
            binding.etprofilemobile.setText(user.mobile.toString())
        }


    }

    private fun uploadUSerImage(){
        showProgressDialog("Please Wait")

        if(mSelectedImageFileUri != null)
        {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                    + "." + getFileExtension(mSelectedImageFileUri!!)
                )

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                takeSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    takeSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                takeSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageUrl = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this@MyProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun getFileExtension(uri: Uri): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData() {
        // Initialize userHashMap
       try
       {
           val userHashMap = HashMap<String, Any>()


           // Ensure mProfileImageUrl is not empty and different from the current user image
           if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image) {
               userHashMap["image"] = mProfileImageUrl
           }

           // Ensure the name is not empty and different from the current user name
           val name = binding.etprofilename.text.toString()
           if (name != mUserDetails.name) {
               userHashMap["name"] = name
           }

           // Ensure the mobile is not empty and different from the current user mobile
           val mobile = binding.etprofilemobile.text.toString()
           if (mobile.isNotEmpty() && mobile != mUserDetails.mobile.toString()) {
               try {
                   userHashMap["mobile"] = mobile.toLong()
               } catch (e: NumberFormatException) {
                   Log.e("Firebase Error", "Invalid mobile number format")
               }
           }

           // Call FireStore to update user profile data
           Log.i("Firestore" ,"Going in the firestore function")
           FireStore().updateUserProfileData(this, userHashMap)
       }
       catch (e: Exception)
       {
           Log.e("Firebase Error", e.message!!)
       }
    }

}