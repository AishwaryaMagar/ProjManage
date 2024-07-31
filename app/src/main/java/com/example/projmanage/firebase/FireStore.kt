package com.example.projmanage.firebase

import android.app.Activity
import android.provider.SyncStateContract.Constants
import android.util.Log
import android.widget.Toast
import com.example.projmanage.CreateBoardActivity
import com.example.projmanage.MainActivity
import com.example.projmanage.MembersListActivity
import com.example.projmanage.MyProfileActivity
import com.example.projmanage.SignInActivity
import com.example.projmanage.SignUpActivity
import com.example.projmanage.TaskListActivity
import com.example.projmanage.models.Board
import com.example.projmanage.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.toObject

open class FireStore {

    private val mFireStore = FirebaseFirestore.getInstance()


    fun registeredUser(activity: SignUpActivity, userInfo: User) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            mFireStore.collection("Users")
                .document(userId)
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteresSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error registering user: ${e.message}")
                }
        } else {
            Log.e("Firestore Error", "User ID is empty, cannot register user.")
        }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            mFireStore.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)
                    if (loggedInUser != null) {
                        when (activity) {
                            is SignInActivity -> {
                                activity.signInSuccess(loggedInUser)
                            }

                            is MainActivity -> {
                                activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                            }

                            is MyProfileActivity -> {
                                activity.updateProfileDetails(loggedInUser)
                            }
                        }
                    } else {
                        Log.e("Firestore Error", "Logged in user is null.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error signing in user: ${e.message}")
                }
        } else {
            Log.e("Firestore Error", "User ID is empty, cannot sign in user.")
        }
    }

    fun updateUserProfileData(
        activity: MyProfileActivity,
        userHashMap: HashMap<String, Any>
    ) {

        Log.i("Firestore", "Went in the fucntion")
        mFireStore.collection("Users")
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data Updated")
                Toast.makeText(activity, "Profile Updated", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board",
                    e
                )
                Toast.makeText(activity, "Error while updating profile", Toast.LENGTH_SHORT).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection("boards")
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created Successfully")
                Toast.makeText(activity, "Board created Successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName, "Error while creating board", exception
                )
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection("boards")
            .whereArrayContains("assignedTo", getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board")
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String)
    {
        mFireStore.collection("boards")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val boardList: ArrayList<Board> = ArrayList()
                //TODO get board details
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board")
            }
    }
    fun addUpdateTaskList(activity: TaskListActivity, board: Board)
    {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap["tasklist"] = board.tasklist
        mFireStore.collection("boards")
            .document(board.documentId!!)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList Updated")
                activity.boardDetails(board)
            }
            .addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating task list" )
            }
    }

    fun getAssingedMembersList(
        activity: MembersListActivity,
        assignedTo: ArrayList<String>
    )
    {
        mFireStore.collection("Users")
            .whereIn("id", assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents)
                {
                    val user = i.toObject(User::class.java)
                    usersList.add(user!!)
                }
                activity.setupMembersList(usersList)
            }
            .addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating a board", e)
            }
    }
    fun getMemberDetails(activity: MembersListActivity, email:String)
    {
        mFireStore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size > 0)
                {
                    val user = document.documents[0].toObject(User::class.java)
                    activity.memberDetails(user!!)
                }
                else
                {
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "No such member exists", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    it
                )
            }
    }

    fun assignMemberToBoard(
        activity: MembersListActivity, board: Board, user: User
    ){
        val assignToHashMap = HashMap<String, Any>()
        assignToHashMap["assignedto"] = board.assignedTo
        mFireStore.collection("boards")
            .document(board.documentId!!)
            .update(assignToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error While assigning member")
            }

    }



}
