package com.example.mychat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ActivityMainBinding
import com.example.mychat.databinding.ItemRoomBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.dialog_add_room.view.*

class RoomActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var userName: String = ""

    var myAdapter: Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()

        userName = intent.getStringExtra("user_name")

        val query = db.collection("rooms")
                .orderBy("name")
        setUpView(userName, query)

        binding.adRoomView.setOnClickListener {
            val layout = layoutInflater.inflate(R.layout.dialog_add_room, null, false)
            AlertDialog.Builder(this)
                    .setTitle("部屋を追加")
                    .setView(layout)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val name = layout.name.text.toString()
                        val room = Room(name)
                        db.collection("rooms")
                                .add(room)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
        }
    }

    override fun onStart() {
        super.onStart()
        myAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        myAdapter?.stopListening()
    }

    private fun setUpView(userName: String, query: Query) {
        binding.recyclerView.apply {
            myAdapter = Adapter(userName, query)
            this.adapter = myAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    class Adapter(val userName: String, query: Query) : FireStoreAdapter<Adapter.ViewHolder>(query) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_room,
                    parent,
                    false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getSnapshot(position))
        }

        inner class ViewHolder(val binding: ItemRoomBinding)
            : RecyclerView.ViewHolder(binding.root) {

            fun bind(snapShot: DocumentSnapshot) {
                val room = snapShot.toObject(Room::class.java)
                binding.room.text = room?.name
                binding.room.setOnClickListener {
                    TalkActivity.start(binding.room.context, snapShot.id, room?.name, userName)
                }
            }
        }
    }

    companion object {
        fun start(context: Context, userName: String) {
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra("user_name", userName)
            context.startActivity(intent)
        }
    }
}
