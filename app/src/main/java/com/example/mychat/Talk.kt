package com.example.mychat

import com.google.firebase.Timestamp

class Talk() {
    var sendTime: Timestamp? = null
    var message: String = ""
    var userName: String? = null

    constructor(sendTime: Timestamp, message: String, userName: String) : this() {
        this.sendTime = sendTime
        this.message = message
        this.userName = userName
    }
}