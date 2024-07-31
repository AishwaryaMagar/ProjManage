package com.example.projmanage

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projmanage.Constants.getFileExtension
import com.example.projmanage.databinding.ActivityCreateBoardBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.Board
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri?= null
    private lateinit var mUsername: String
    private var mBoardImageUrl: String = ""


    lateinit var binding: ActivityCreateBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()

        if(intent.hasExtra("name"))
        {
            mUsername = intent.getStringExtra("name").toString()
        }

        binding.ivBoardImage.setOnClickListener {
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
        binding.btncreate.setOnClickListener {
            if(mSelectedImageFileUri != null)
            {
                uploadBoardImage()
            }
            else
            {
                showProgressDialog("Please Wait")
                createBoard()
            }
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
            actionBar.setTitle("Create Board")
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
                Glide.with(this@CreateBoardActivity)
                    .load(mSelectedImageFileUri.toString())
                    .centerCrop()
                    .into(binding.ivBoardImage)
            }
            catch (e: IOException)
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

    fun boardCreatedSuccessfully()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun createBoard(){
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())

        var board = Board(
            binding.etboardname.text.toString(),
            mBoardImageUrl,
            mUsername,
            assignedUserArrayList
        )
        FireStore().createBoard(this, board)
    }

    private fun uploadBoardImage()
    {
        showProgressDialog("Please Wait")

        if(mSelectedImageFileUri != null)
        {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "BOARD_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(this, mSelectedImageFileUri!!)
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
                    mBoardImageUrl = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener {
                    exception ->
                Toast.makeText(this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

}