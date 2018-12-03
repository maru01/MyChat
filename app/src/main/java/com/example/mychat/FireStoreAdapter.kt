package com.example.mychat

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*

abstract class FireStoreAdapter<VH : RecyclerView.ViewHolder>(var query: Query)
    : RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    private val snapshots: ArrayList<DocumentSnapshot> = ArrayList()

    fun startListening() {
        if (registration == null) {
            registration = query.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        registration?.let {
            it.remove()
            registration = null
        }

        snapshots.clear()
        notifyDataSetChanged()
    }

    fun changeQuery(query: Query) {
        stopListening()

        snapshots.clear()
        notifyDataSetChanged()

        this.query = query
        startListening()
    }

    override fun getItemCount() = snapshots.size

    protected fun getSnapshot(index: Int): DocumentSnapshot {
        return snapshots[index]
    }

    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {

        if (e != null || documentSnapshots == null) {
            Log.w("error", "onEvent:error", e)
            return
        }

        for (change in documentSnapshots.documentChanges) {
            val snapShot = change.document

            when (change.type) {
                DocumentChange.Type.ADDED -> onDocumentAdded(change)
                DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
            }
        }
    }

    protected fun onDocumentAdded(change: DocumentChange) {
        snapshots.add(snapshots.size, change.document)
        notifyItemInserted(snapshots.size)
    }

    protected fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            snapshots.set(change.oldIndex, change.document)
            notifyItemInserted(change.oldIndex)
        } else {
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    protected fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }
}