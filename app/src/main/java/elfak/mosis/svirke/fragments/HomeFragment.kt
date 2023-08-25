package elfak.mosis.svirke.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.svirke.databinding.FragmentHomeBinding
import elfak.mosis.svirke.R
import elfak.mosis.svirke.classes.MestaZaSvirke
import elfak.mosis.svirke.viewmodels.LoggedUserViewModel
import elfak.mosis.svirke.classes.User
import elfak.mosis.svirke.viewmodels.MestaZaSvirkeViewModel
import elfak.mosis.svirke.viewmodels.UsersViewModel

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var location: MutableLiveData<Location>
    private val MestaZaSvirkeViewModel: MestaZaSvirkeViewModel by activityViewModels()
    private var mestaZaSvirkuMap: MutableMap<Marker?, MestaZaSvirke> = mutableMapOf()
    private val UsersViewModel: UsersViewModel by activityViewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        MestaZaSvirkeViewModel.svirke.observe(viewLifecycleOwner, Observer { svirke->

            for(svirka in svirke)
            {
                addMarkerToMap(svirka);
            }
        })
        return binding.root
    }

    private fun addMarkerToMap(svirka: MestaZaSvirke) {
        val latLng = LatLng(svirka.latitude, svirka.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(svirka.title)
            .snippet(svirka.description)
        map?.addMarker(markerOptions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPreferences = requireContext().getSharedPreferences("Svirke", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", "")

        val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        databaseUser.child(savedUsername!!).get().addOnCompleteListener { task ->
            val dataSnapshot = task.result
            val ime=dataSnapshot.child("firstName").getValue(String::class.java)?: ""
            val prezime=dataSnapshot.child("lastName").getValue(String::class.java)?: ""
            val slika=dataSnapshot.child("imageURl").getValue(String::class.java)?: ""
            val korisnicko=dataSnapshot.child("username").getValue(String::class.java)?: ""
            val telefon=dataSnapshot.child("phoneNumber").getValue(String::class.java)?: ""
            val poeni=dataSnapshot.child("points").getValue(Int::class.java)?: 0
            val rang=dataSnapshot.child("rang").getValue(Int::class.java)?: 0
            val sifra=dataSnapshot.child("password").getValue(String::class.java)?: ""
            if (ime != null && prezime != null && slika != null && korisnicko != null
                && telefon != null && poeni != null && rang != null && sifra != null) {
                val userr = User(ime, prezime, korisnicko, sifra, telefon, slika, rang, poeni)
                loggedUserViewModel.user = userr
            } else {
//                Log.e("Firebase", "Some values are null.")
            }

        }

        MestaZaSvirkeViewModel.filtriranaMestaZaSvirke.observe(viewLifecycleOwner) { filtriranaMesta ->
            updateMapWithFilteredMarkers(filtriranaMesta)
        }

        UsersViewModel.getUsers()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())



        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        location= MutableLiveData()
        mapFragment!!.getMapAsync{ mMap ->
            map =mMap
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.clear()
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isCompassEnabled = true
            mMap.setOnMarkerClickListener { marker ->
                val svirka = marker.tag as? MestaZaSvirke
                if (svirka != null) {
                    MestaZaSvirkeViewModel.svirka=svirka
                    this.findNavController().navigate(R.id.action_homeFragment_to_infoFragment)
                }
                false
            }
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                return@getMapAsync
            }
            mMap.isMyLocationEnabled=true
            fusedLocationClient.lastLocation.addOnCompleteListener {location->
                if(location.result  !=null)
                {
                    lastLocation=location.result
                    val currentLatLong= LatLng(location.result.latitude, location.result.longitude)
                    loggedUserViewModel.location=currentLatLong
                    val googlePlex = CameraPosition.builder()
                        .target(currentLatLong)
                        .zoom(15f)
                        .bearing(0f)
                        .tilt(0f)
                        .build()

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null)
                    if(MestaZaSvirkeViewModel.svirke!=null)
                    {
                        MestaZaSvirkeViewModel.getMestaZaSvirke(location= LatLng(lastLocation.latitude,lastLocation.longitude), onDataLoaded = {setUpMarkers()})
                    }
                }
            }.addOnFailureListener{
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        binding.fabAdd.setOnClickListener{
            val filterFragment = FilterFragment()
            filterFragment.show(requireActivity().supportFragmentManager, "filter_dialog")
        }
    }

    private fun updateMapWithFilteredMarkers(filtriranaMesta: List<MestaZaSvirke>) {
        Log.d("UpdateMap", "Ažuriranje mape sa filtriranim mestima")
        map?.clear()
        mestaZaSvirkuMap.clear()

        filtriranaMesta.forEach { mestoZaSvirku ->
            val marker = map?.addMarker(
                MarkerOptions()
                    .position(LatLng(mestoZaSvirku.latitude, mestoZaSvirku.longitude))
                    .title(mestoZaSvirku.title))

            mestaZaSvirkuMap[marker] = mestoZaSvirku
            marker?.tag = mestoZaSvirku
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item=menu.findItem(R.id.action_show_map)
        item.isVisible=false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("Svirke", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_homeFragment_to_login)
                true
            }
            R.id.action_add->
            {
                this.findNavController().navigate(R.id.action_homeFragment_to_addMestaZaSvirkeFragment)
                true
            }
            R.id.action_show_scoreboard->
            {
                this.findNavController().navigate(R.id.action_homeFragment_to_leaderBoardFragment)
                true
            }
            R.id.action_show_profile->
            {
                this.findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }
    private fun setUpMarkers()
    {
        map?.clear()
        mestaZaSvirkuMap= mutableMapOf()
        val mestazaSvirku=MestaZaSvirkeViewModel.svirke.value
        if(mestazaSvirku!=null)
        {
            for(mestozaSvirku in mestazaSvirku)
            {
                val marker=map?.addMarker(MarkerOptions().position(LatLng(mestozaSvirku.latitude,mestozaSvirku.longitude)).title(mestozaSvirku.title))
                mestaZaSvirkuMap[marker]=mestozaSvirku
                marker?.tag=mestozaSvirku
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

}