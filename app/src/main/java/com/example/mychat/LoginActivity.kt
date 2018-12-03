package com.example.mychat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mychat.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog_password.view.*

class LoginActivity : AppCompatActivity() {

    var users: List<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_login)

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
                .addSnapshotListener { querySnapshot, _ ->
                    users = querySnapshot?.toObjects(User::class.java)
                }

        binding.login.setOnClickListener {
            val userName = binding.userName.text.toString()
            if (!userName.isNullOrBlank()) {
                val result = users?.find {
                    it.name == userName
                }
                if (result == null) {
                    val layout = layoutInflater.inflate(R.layout.dialog_password, null, false)
                    AlertDialog.Builder(this)
                            .setTitle("パスワードを作成")
                            .setView(layout)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val password = layout.password.text.toString()
                                val user = User(userName, password)
                                db.collection("users")
                                        .add(user)
                                RoomActivity.start(this, userName)
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                } else {
                    val layout = layoutInflater.inflate(R.layout.dialog_password, null, false)
                    AlertDialog.Builder(this)
                            .setTitle("パスワードを入力")
                            .setView(layout)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val password = layout.password.text.toString()
                                if (password == result.password) {
                                    RoomActivity.start(this, userName)
                                } else {
                                    Toast.makeText(this, "パスワードが違います", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                }
            }
        }
    }
}