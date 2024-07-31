package com.example.projmanage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.projmanage.databinding.ActivitySignUpBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        binding.signUpToolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        binding.btnSignupSubmit.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteresSuccess()
    {
        Toast.makeText(this, "you have" +
                "succesfully registered" ,
            Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar()
    {
        setSupportActionBar(binding.signUpToolbar)

        val actionBar = supportActionBar
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.setTitle("Sign Up")
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean
    {
        return when{
            TextUtils.isEmpty(name)->{
                showSanckBar("Please enter a name")
                false
            }
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

    private fun registerUser()
    {
        val name: String = binding.etName.text.toString().trim{it <= ' ' }
        val email: String = binding.etEmail.text.toString().trim{it <= ' ' }
        val password: String = binding.etPassword.text.toString().trim{it <= ' ' }

        if(validateForm(name, email, password))
        {
            showProgressDialog("Please wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {task->

                if(task.isSuccessful)
                {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredemail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registeredemail)
                    FireStore().registeredUser(this, user)
                }
                else
                {
                    Toast.makeText(this, task.exception!!.message,
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}