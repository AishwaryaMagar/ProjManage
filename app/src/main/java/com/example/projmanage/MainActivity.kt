package com.example.projmanage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projmanage.databinding.ActivityMainBinding
import com.example.projmanage.databinding.AppbarMainBinding
import com.example.projmanage.databinding.MainContentBinding
import com.example.projmanage.databinding.NavHeaderMainBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.Board
import com.example.projmanage.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private  lateinit var navbinding: NavHeaderMainBinding
    private lateinit var appbarMainBinding: AppbarMainBinding
    private lateinit var mainContentBinding: MainContentBinding

    private lateinit var mUsername: String

    companion object{
        const val MYPROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        navbinding = NavHeaderMainBinding.inflate(layoutInflater)
        appbarMainBinding = AppbarMainBinding.inflate(layoutInflater)
        mainContentBinding = MainContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHeaderView = binding.navigationview.getHeaderView(0)
        navbinding = NavHeaderMainBinding.bind(navHeaderView)

        val appbarMainView = findViewById<View>(R.id.appbar_main)
        appbarMainBinding = AppbarMainBinding.bind(appbarMainView)

        val mainContentView = findViewById<View>(R.id.maincontent)
        mainContentBinding = MainContentBinding.bind(mainContentView)

        setSupportActionBar(binding.root.findViewById(R.id.tbmainactivity))
        binding.root.findViewById<androidx.appcompat.widget.Toolbar>(R.id.tbmainactivity).setNavigationIcon(R.drawable.ic_navigation)
        binding.root.findViewById<androidx.appcompat.widget.Toolbar>(R.id.tbmainactivity).setNavigationOnClickListener {
            toggleDrawer()
        }

        binding.navigationview.setNavigationItemSelectedListener(this)
        FireStore().loadUserData(this, true)

        appbarMainBinding.fb.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra("name", mUsername)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardData: Boolean){
        mUsername = user.name
        Glide.with(this).load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(navbinding.profileImg)

        navbinding.phUsername.text = user.name
        if(readBoardData == true)
        {
            showProgressDialog("Please Wait")
            FireStore().getBoardList(this)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MYPROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            FireStore().loadUserData(this)
        }
        else if (resultCode == Activity.RESULT_OK &&
            requestCode == CREATE_BOARD_REQUEST_CODE)
        {
            FireStore().getBoardList(this)
        }
        else
        {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {

                startActivityForResult(Intent(this, MyProfileActivity::class.java), MYPROFILE_REQUEST_CODE)
                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>)
    {
        hideProgressDialog()
        if(boardsList.size >0)
        {

            mainContentBinding.rvBoardsList.visibility = View.VISIBLE
            mainContentBinding.tvNoBoardsAvailable.visibility = View.GONE

            mainContentBinding.rvBoardsList.layoutManager = LinearLayoutManager(this)
            mainContentBinding.rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            mainContentBinding.rvBoardsList.adapter = adapter
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra("documentId", model.documentId)

                    startActivity(intent)
                }
            })
        }
        else
        {
            mainContentBinding.rvBoardsList.visibility = View.GONE
            mainContentBinding.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }
}
