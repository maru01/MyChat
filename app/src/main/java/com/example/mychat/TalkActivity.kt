package com.example.mychat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ActivityTalkBinding
import com.example.mychat.databinding.ItemTalkBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class TalkActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, id: String, name: String?, userName: String?) {
            val intent = Intent(context, TalkActivity::class.java)
            intent.putExtra("room", id)
            intent.putExtra("name", name)
            intent.putExtra("userName", userName)
            context.startActivity(intent)
        }
    }

    lateinit var binding: ActivityTalkBinding

    var myAdapter: Adapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_talk)

        val room = intent.getStringExtra("room")
        val name = intent.getStringExtra("name")
        val userName = intent.getStringExtra("userName")

        binding.toolbar.title = name

        val db = FirebaseFirestore.getInstance()

        val query = db.collection("rooms").document(room).collection("talks")
                .orderBy("sendTime")
        setUpView(userName, query)

        binding.sendButton.setOnClickListener {
            val message = binding.messageEdit.text.toString()
            val data = HashMap<String, Any?>()
            data["message"] = message
            data["sendTime"] = Timestamp(Date())
            data["userName"] = userName
            db.collection("rooms").document(room)
                    .collection("talks")
                    .add(data)
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
                    R.layout.item_talk,
                    parent,
                    false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getSnapshot(position))
        }

        inner class ViewHolder(val binding: ItemTalkBinding)
            : RecyclerView.ViewHolder(binding.root) {

            fun bind(snapshot: DocumentSnapshot) {
                val talk = snapshot.toObject(Talk::class.java)
                talk?: return
                binding.message.text = talk.message
                if (userName == talk.userName) {
                    binding.message.gravity = Gravity.RIGHT
                }
            }
        }
    }
}