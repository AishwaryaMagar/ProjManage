package com.example.projmanage

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanage.databinding.ActivityMembersListBinding
import com.example.projmanage.databinding.DialogSearchMemberBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.Board
import com.example.projmanage.models.User
import com.projemanag.adapters.MemberListItemsAdapter

class MembersListActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersListBinding
    private lateinit var mboardDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra("board_details")) {
            mboardDetails = intent.getParcelableExtra<Board>("board_details")!!
        }
        setupActionBar()
        showProgressDialog("Please Wait")
        FireStore().getAssingedMembersList(this, mboardDetails.assignedTo)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.membersListToolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.setTitle("Members")
        }

    }

    fun setupMembersList(list: ArrayList<User>)
    {
        mAssignedMembersList = list
        hideProgressDialog()
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        binding.rvMembersList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun dialogSearchMember()
    {
        val dialog = Dialog(this)
        val dialogbindig: DialogSearchMemberBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogbindig.root)
        dialogbindig.tvAdd.setOnClickListener {
            val email = dialogbindig.etEmailSearchMember.text.toString()
            if(email.isNotEmpty())
            {
                dialog.dismiss()
                //TODO add member
                showProgressDialog("Please Wait")
                FireStore().getMemberDetails(this, email)
            }
            else{
                Toast.makeText(
                    this@MembersListActivity,
                    "Please enter members email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialogbindig.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    fun memberDetails(user: User)
    {
        mboardDetails.assignedTo.add(user.id)
        FireStore().assignMemberToBoard(this, mboardDetails, user)
    }
    fun memberAssignSuccess(user: User)
    {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        setupMembersList(mAssignedMembersList)
    }
}