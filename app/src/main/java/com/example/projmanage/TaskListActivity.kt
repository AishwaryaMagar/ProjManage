package com.example.projmanage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanage.databinding.ActivityTaskListBinding
import com.example.projmanage.firebase.FireStore
import com.example.projmanage.models.Board
import com.example.projmanage.models.Card
import com.example.projmanage.models.Task
import com.google.firebase.firestore.persistentCacheSettings
import com.projemanag.adapters.TaskListItemsAdapter

class TaskListActivity : BaseActivity() {

    lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDetails: Board
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var boardDocumentId = ""
        if(intent.hasExtra("documentId"))
        {
            boardDocumentId = intent.getStringExtra("documentId").toString()
        }
        showProgressDialog("Please Wait")
        FireStore().getBoardDetails(this, boardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_members->{
                val intent = Intent(this, MembersListActivity::class.java)
                intent.putExtra("board_details", mBoardDetails)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun boardDetails(board: Board)
    {
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()

        val addTAskList = Task("Add List")
        board.tasklist.add(addTAskList)
        binding.rvTaskList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = TaskListItemsAdapter(this, board.tasklist)
        binding.rvTaskList.adapter = adapter

    }
    private fun setupActionBar()
    {
        setSupportActionBar(binding.myTaskListToolbar)

        val actionBar = supportActionBar
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.setTitle(mBoardDetails.name)
        }
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        showProgressDialog("Please Wait")
        FireStore().getBoardDetails(this, mBoardDetails.documentId!!)

    }
    fun createTaskList(taskListName: String)
    {
        val task = Task(taskListName, FireStore().getCurrentUserId())
        mBoardDetails.tasklist.add(0, task)
        mBoardDetails.tasklist.removeAt(mBoardDetails.tasklist.size-1)

        showProgressDialog("Please Wait")
        FireStore().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position:Int, listName: String, model: Task)
    {
        val task = Task(listName, model.createdBy)
        mBoardDetails.tasklist[position] = task
        mBoardDetails.tasklist.removeAt(mBoardDetails.tasklist.size-1)
        showProgressDialog("Please Wait")
        FireStore().addUpdateTaskList(this, mBoardDetails)
    }
    fun deleteTaskList(position: Int)
    {
        mBoardDetails.tasklist.removeAt(position)
        mBoardDetails.tasklist.removeAt(mBoardDetails.tasklist.size-1)
        showProgressDialog("Please Wait")
        FireStore().addUpdateTaskList(this, mBoardDetails)
    }
    fun addCardToTaskList(position: Int, cardName: String)
    {
        mBoardDetails.tasklist.removeAt(mBoardDetails.tasklist.size-1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStore().getCurrentUserId())

        val card = Card(cardName, FireStore().getCurrentUserId())

        val cardsList = mBoardDetails.tasklist[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.tasklist[position].title,
            mBoardDetails.tasklist[position].createdBy,
            cardsList
        )

        mBoardDetails.tasklist[position] = task
        showProgressDialog("Please Wait")
        FireStore().addUpdateTaskList(this, mBoardDetails)
    }
}