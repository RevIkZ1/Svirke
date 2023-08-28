package elfak.mosis.svirke.viewmodels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
//import elfak.mosis.svirke.data.Comment
import elfak.mosis.svirke.classes.MestaZaSvirke
import elfak.mosis.svirke.classes.User
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MestaZaSvirkeViewModel : ViewModel() {

    private val database= Firebase.database.reference
    private val storageRef = FirebaseStorage.getInstance().reference

    private val _svirka=MutableLiveData<MestaZaSvirke?>(null)
    private val _svirke=MutableLiveData<List<MestaZaSvirke>>(emptyList())
    private val _filtriranaMestaZaSvirke = MutableLiveData<List<MestaZaSvirke>>()
    private val _ResetMestaZaSvirke = MutableLiveData<List<MestaZaSvirke>>()
    val filtriranaMestaZaSvirke: LiveData<List<MestaZaSvirke>> get() = _filtriranaMestaZaSvirke
    var all: Boolean=true
    var camera: Boolean=false
    var radar:Boolean=false
    var svirka
        get() = _svirka.value
        set(value) { _svirka.value=value}

    val svirke: LiveData<List<MestaZaSvirke>> get() = _svirke

    fun addSvirka(svirka: MestaZaSvirke, user: User)
    {
        val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        databaseUser.child(svirka.ownerId).child("points").setValue(user.points+10)
    }
    private fun getDistance(currentLat: Double, currentLon: Double, mestaZaSvirkeLat: Double, mestaZaSvirkeLon: Double): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters

        val currentLatRad = Math.toRadians(currentLat)
        val mestaZaSvirkeLatRad = Math.toRadians(mestaZaSvirkeLat)
        val deltaLat = Math.toRadians(mestaZaSvirkeLat - currentLat)
        val deltaLon = Math.toRadians(mestaZaSvirkeLon - currentLon)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(currentLatRad) * cos(mestaZaSvirkeLatRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
    fun filterLocations(all:Boolean?, camera:Boolean?, radar:Boolean?, loc: LatLng, rad: Int=10000)
    {
        this.all= all!!
        this.camera=camera!!
        this.radar=radar!!
        getMestaZaSvirke(location=loc, radius=rad, onDataLoaded = {})
    }
    fun getMestaZaSvirke(location: LatLng, radius: Int=10000,   onDataLoaded: () -> Unit)
    {
        database.child("MestaZaSvirke").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val mestaZaSvirkeList= mutableListOf<MestaZaSvirke>()
                    for(dev in snapshot.children){
                        val d=dev.getValue(MestaZaSvirke::class.java)
                        d?.let{
                            val distance=getDistance(location.latitude, location.longitude, d.latitude, d.longitude)
                            if(distance<=radius)
                            {
                                if(all)
                                {
                                    mestaZaSvirkeList.add(d)
                                }
                            }
                        }
                    }
                    _svirke.value=mestaZaSvirkeList
                    onDataLoaded()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException());
            }
        })
    }


    fun filtrirajMestaZaSvirke(
        oglasavac: String,
        naziv: String,
        tipMuzike: String,
        radijus: Double,
        datum: Date?,
        userLat: Double,
        userLong: Double
    ) {
        val databaseMestaZaSvirke = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("MestaZaSvirke")
        databaseMestaZaSvirke.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filtriranaMestaZaSvirke = mutableListOf<MestaZaSvirke>()

                for (mestoZaSvirkuSnapshot in snapshot.children) {
                    val mestoZaSvirku = mestoZaSvirkuSnapshot.getValue(MestaZaSvirke::class.java)

                    if (mestoZaSvirku != null) {
                        val matchOglasavac = oglasavac.isEmpty() || mestoZaSvirku.ownerId.contains(oglasavac, ignoreCase = true)
                        val matchNaziv = naziv.isEmpty() || mestoZaSvirku.title.contains(naziv, ignoreCase = true)
                        val matchTipMuzike = tipMuzike == "Choose" || mestoZaSvirku.vrstaMuzike.contains(tipMuzike, ignoreCase = true)

                        val matchRadijus = radijus == 0.0 || getDistance(userLat, userLong, mestoZaSvirku.latitude, mestoZaSvirku.longitude) < radijus

                        val mestoDate = mestoZaSvirku.date
                        val calendar = Calendar.getInstance()
                        calendar.time = mestoDate

                        val mestoYear = calendar.get(Calendar.YEAR)
                        val mestoMonth = calendar.get(Calendar.MONTH)
                        val mestoDay = calendar.get(Calendar.DAY_OF_MONTH)

                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.time = datum
                        val selectedYear = selectedCalendar.get(Calendar.YEAR)
                        val selectedMonth = selectedCalendar.get(Calendar.MONTH)
                        val selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)

                        val matchDatum = datum == null || (selectedYear == mestoYear && selectedMonth == mestoMonth && selectedDay == mestoDay)

                        if (matchOglasavac && matchNaziv && matchTipMuzike && matchRadijus && matchDatum) {
                            filtriranaMestaZaSvirke.add(mestoZaSvirku)
                        }
                    }
                }
                _svirke.value = filtriranaMestaZaSvirke
                _filtriranaMestaZaSvirke.value = filtriranaMestaZaSvirke
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
    fun resetFilter()
    {
        _filtriranaMestaZaSvirke.value=_ResetMestaZaSvirke.value
    }
    fun like(user: User, svirka: MestaZaSvirke) {
        if (!svirka.likedByUsers.any { it == user.username }) {
            svirka.likedByUsers.add(user.username)
            svirka.like = svirka.like + 1
            val databaseSvirke = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("MestaZaSvirke")
            if (svirka.dislikedByUsers.contains(user.username)) {
                val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
                databaseUser.child(user.username).child("points").setValue(user.points + 10)
                svirka.dislikedByUsers.remove(user.username)
                svirka.dislike = svirka.dislike - 1
                databaseSvirke.child(svirka.id).child("dislikedByUsers").setValue(svirka.dislikedByUsers)
                databaseSvirke.child(svirka.id).child("dislike").setValue(svirka.dislike)
                databaseUser.child(user.username).child("points").setValue(user.points + 50)
            }
            databaseSvirke.child(svirka.id).child("likedByUsers").setValue(svirka.likedByUsers)
            databaseSvirke.child(svirka.id).child("like").setValue(svirka.like)
            val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
            databaseUser.child(user.username).child("points").setValue(user.points + 5)

            databaseUser.child(svirka.ownerId).get().addOnSuccessListener { dataSnapshot ->
                val owner = dataSnapshot.getValue(User::class.java)
                if (owner != null) {
                    val updatedPoints = owner.points + 10
                    if (updatedPoints >= 0) {
                        databaseUser.child(svirka.ownerId).child("points").setValue(updatedPoints)
                    }
                }
            }
        }
    }

    fun dislike(user: User, svirka: MestaZaSvirke) {
        if (!svirka.dislikedByUsers.any { it == user.username }) {
            svirka.dislikedByUsers.add(user.username)
            svirka.dislike = svirka.dislike + 1
            val databaseSvirke = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("MestaZaSvirke")
            if (svirka.likedByUsers.contains(user.username)) {
                val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
                databaseUser.child(user.username).child("points").setValue(user.points - 10)
                svirka.likedByUsers.remove(user.username)
                svirka.like = svirka.like - 1
                databaseSvirke.child(svirka.id).child("likedByUsers").setValue(svirka.likedByUsers)
                databaseSvirke.child(svirka.id).child("like").setValue(svirka.like)
                databaseUser.child(user.username).child("points").setValue(user.points - 5)
            }
            databaseSvirke.child(svirka.id).child("dislikedByUsers").setValue(svirka.dislikedByUsers)
            databaseSvirke.child(svirka.id).child("dislike").setValue(svirka.dislike)
            val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
            databaseUser.child(user.username).child("points").setValue(user.points - 5)
            databaseUser.child(svirka.ownerId).get().addOnSuccessListener { dataSnapshot ->
                val owner = dataSnapshot.getValue(User::class.java)
                if (owner != null) {
                    val updatedPoints = owner.points - 10
                    if (updatedPoints >= 0) {
                        databaseUser.child(svirka.ownerId).child("points").setValue(updatedPoints)
                    }
                }
            }

        }
    }
}