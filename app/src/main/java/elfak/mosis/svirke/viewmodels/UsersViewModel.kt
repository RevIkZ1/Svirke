package elfak.mosis.svirke.viewmodels

import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import elfak.mosis.svirke.classes.User

class UsersViewModel: ViewModel() {
    private val _users = MutableLiveData<List<User>>(emptyList())

    // Kreiramo referencu na Firebase bazu podataka za kolekciju "Users"
    private val database= FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")


    var users
        get() = _users.value
        set(value) { _users.value = value }

    // Funkcija koja vraća referencu na kolekciju "Users" u bazi podataka


    fun getUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userList = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        val u = userSnapshot.getValue(User::class.java)
                        if (u != null) {
                            Log.d("UsersViewModel", "Read user: ${u.username}, Points: ${u.points}")
                            userList.add(u)
                        }
                    }
                    Log.d("UsersViewModel","${userList}")
                    users = userList.sortedByDescending { it.points }.take(20)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
}