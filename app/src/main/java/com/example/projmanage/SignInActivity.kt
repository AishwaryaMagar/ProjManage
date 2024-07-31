package com.example.projmanage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.projmanage.databinding.ActivitySignInBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : BaseActivity() {
    lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        binding.signInToolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        auth = Firebase.auth
        binding.btnSigninSubmit.setOnClickListener {
            signinRegisteredUser()
        }
    }

    private fun setupActionBar()
    {
        setSupportActionBar(binding.signInToolbar)

        val actionBar = supportActionBar
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.setTitle("Sign In")
        }
    }

    private fun validateForm(email: String, password: String): Boolean
    {
        return when{
            TextUtils.isEmpty(email)->{
                showSanckBar("Please enter a email")
                false
            }
            TextUtils.isEmpty(password)->{
                showSanckBar("Please enter a password")
                false
            }
            else-> true
        }
    }

    fun signInSuccess(user: User)
    {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun signinRegisteredUser()
    {

        val email: String = binding.etSigninEmail.text.toString().trim{it <= ' ' }
        val password: String = binding.etSigninPassword.text.toString().trim{it <= ' ' }

        if(validateForm(email, password))
        {
            showProgressDialog("Please Wait")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign in", "signInWithEmail:success")
                        val user = auth.currentUser
                        FireStore().loadUserData(this)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }



    }
}