package com.projemanag.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projmanage.R
import com.example.projmanage.TaskListActivity
import com.example.projmanage.databinding.ActivityTaskListBinding
import com.example.projmanage.databinding.ItemTaskBinding
import com.example.projmanage.models.Task


// TODO (Step 5: Create an adapter class for Task List Items in the TaskListActivity.)
// START
open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: ItemTaskBinding

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // TODO (Step 6: Here we have done some additional changes to display the item of the task list item in 70% of the screen size.)
        // START
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        // Here the layout params are converted dynamically according to the screen size as width is 70% and height is wrap_content.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Here the dynamic margins are applied to the view.
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(ItemTaskBinding.bind(view))
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (position == list.size - 1) {
                holder.tv_add_task_list.visibility = View.VISIBLE
                holder.ll_task_item.visibility = View.GONE
            } else {
                holder.tv_add_task_list.visibility = View.GONE
                holder.ll_task_item.visibility = View.VISIBLE
            }
            holder.tv_task_list_title.text = model.title
            holder.tv_add_task_list.setOnClickListener {
                holder.tv_add_task_list.visibility = View.GONE
                holder.cv_add_task_list.visibility = View.VISIBLE
            }
            holder.ib_close_list_name.setOnClickListener{
                holder.tv_add_task_list.visibility = View.VISIBLE
                holder.cv_add_task_list.visibility = View.GONE
            }
            holder.ib_done_list_name.setOnClickListener{
                //TODO Create entry in DB and display the task list
                val listName = holder.et_task_list_name.text.toString()
                if(listName.isNotEmpty()){
                    if(context is TaskListActivity)
                    {
                        context.createTaskList(listName)
                    }
                }
                else{
                    Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
                }
            }
            holder.ib_edit_list_name.setOnClickListener {
                holder.et_edit_task_list_name.setText(model.title)
                holder.ll_title_view.visibility = View.GONE
                holder.cv_edit_task_list_name.visibility = View.VISIBLE
            }
            holder.ib_close_editable_view.setOnClickListener{
                holder.ll_title_view.visibility = View.VISIBLE
                holder.cv_edit_task_list_name.visibility = View.GONE
            }
            holder.ib_done_list_name.setOnClickListener {
                //TODO done edititng implement
                val listName = holder.et_edit_task_list_name.text.toString()
                if(listName.isNotEmpty()){
                    if(context is TaskListActivity)
                    {
                        context.updateTaskList(position,listName,model)
                    }
                }
                else{
                    Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
                }
            }
            holder.ib_delete_list.setOnClickListener {
                alertDialogForDeleteList(position,model.title)
            }
            holder.tv_add_card.setOnClickListener {
                holder.tv_add_card.visibility =View.GONE
                holder.cv_add_card.visibility = View.VISIBLE
            }
            holder.ib_close_card_name.setOnClickListener {
                holder.tv_add_card.visibility = View.VISIBLE
                holder.cv_add_card.visibility = View.GONE
            }
            holder.ib_done_card_name.setOnClickListener {
                var cardNAme = holder.et_card_name.text.toString()
                if(cardNAme.isNotEmpty())
                {
                    if(context is TaskListActivity)
                    {
                        context.addCardToTaskList(position, cardNAme)
                    }
                }
                else{
                    Toast.makeText(context, "Please Enter Card NAme", Toast.LENGTH_SHORT).show()
                }
            }
            holder.rv_cards_list.layoutManager = LinearLayoutManager(context)
            holder.rv_cards_list.setHasFixedSize(true)
            val adapter = CardListItemsAdapter(context, model.cards)
            holder.rv_cards_list.adapter = adapter

        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function to get density pixel from pixel
     */
    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    /**
     * A function to get pixel from density pixel
     */
    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
    {
        val tv_add_task_list = binding.tvAddTaskList
        val ll_task_item = binding.llTaskItem
        val tv_task_list_title = binding.tvTaskListTitle
        val cv_add_task_list = binding.cvAddTaskListName
        val ib_close_list_name = binding.ibCloseListName
        val ib_done_list_name = binding.ibDoneEditListName
        val et_task_list_name = binding.etTaskListName
        val ib_edit_list_name = binding.ibEditListName
        val ib_close_editable_view = binding.ibCloseEditableView
        val et_edit_task_list_name = binding.etEditTaskListName
        val ll_title_view = binding.llTitleView
        val cv_edit_task_list_name = binding.cvEditTaskListName
        val ib_delete_list = binding.ibDeleteList
        val tv_add_card = binding.tvAddCard
        val cv_add_card = binding.cvAddCard
        val ib_close_card_name = binding.ibCloseCardName
        val ib_done_card_name = binding.ibDoneCardName
        val et_card_name = binding.etCardName
        val rv_cards_list = binding.rvCardList
    }
}
// END