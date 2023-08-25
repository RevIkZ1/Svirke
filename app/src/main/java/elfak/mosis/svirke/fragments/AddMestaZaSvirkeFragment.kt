package elfak.mosis.svirke.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.svirke.R
import elfak.mosis.svirke.classes.User
import elfak.mosis.svirke.viewmodels.LoggedUserViewModel
import elfak.mosis.svirke.databinding.FragmentAddMestaZaSvirkeBinding
import elfak.mosis.svirke.classes.MestaZaSvirke
import elfak.mosis.svirke.databinding.FragmentHomeBinding
import elfak.mosis.svirke.viewmodels.MestaZaSvirkeViewModel
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class AddMestaZaSvirkeFragment : Fragment() {
    private val MestaZaSvirkeViewModel: MestaZaSvirkeViewModel by activityViewModels()
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private var _binding: FragmentAddMestaZaSvirkeBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var databaseUser: DatabaseReference? = null
    private lateinit var progressDialog: ProgressDialog

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Dodavanje: 0 %")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100
        val user = loggedUserViewModel.user
        Toast.makeText(this.activity, user?.username, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddMestaZaSvirkeBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddImage.setOnClickListener()
        {
            otvoriGaleriju()
        }
        binding.buttonSave.setOnClickListener()
        {
            addMestoZaSvirku()
        }
    }

    private fun otvoriGaleriju() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Image"),
            RegisterFragment.PICK_IMAGE_REQUEST
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            try {
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(selectedImageUri!!)
                val selectedImageBitmap = BitmapFactory.decodeStream(imageStream)

                // Postavljanje odabrane slike u ImageView
                val imageViewSelectedImage = requireView().findViewById<ImageView>(R.id.imageView)
                imageViewSelectedImage.setImageBitmap(selectedImageBitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.action_add)
        item.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val sharedPreferences =
                    requireContext().getSharedPreferences("Svirke", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_addMestaZaSvirkeFragment_to_login)
                true
            }

            R.id.action_show_map -> {
                this.findNavController()
                    .navigate(R.id.action_addMestaZaSvirkeFragment_to_homeFragment)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun addMestoZaSvirku() {
        val editTitle = requireView().findViewById<EditText>(R.id.editTextTitle)
        val editDescription = requireView().findViewById<EditText>(R.id.editTextDescription)
        val editDate = requireView().findViewById<EditText>(R.id.editTextDate)
        val vrstaMuzike = requireView().findViewById<Spinner>(R.id.spinnerVrstaMuzike)
        val title = editTitle.text.toString()
        val description = editDescription.text.toString()
        val dateStr = editDate.text.toString()
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date: Date? = try {
            dateFormatter.parse(dateStr)
        } catch (e: ParseException) {
            null
        }
        val tipMuzike = vrstaMuzike.selectedItem.toString()
        if (selectedImageUri != null && title != null && description != null && date != null) {
            databaseUser =
                FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("MestaZaSvirke")
            val storageRef = FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            if (selectedImageUri != null) {
                progressDialog.show()
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!).addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        val activityObj: Activity? = this.activity
                        val ownerId = loggedUserViewModel.user?.username
                        val lat = loggedUserViewModel.location?.latitude
                        val lon = loggedUserViewModel.location?.longitude
                        val userKey = databaseUser?.push()?.key
                        Toast.makeText(this.activity, lat.toString(), Toast.LENGTH_SHORT).show()
                        Toast.makeText(this.activity, lon.toString(), Toast.LENGTH_SHORT).show()
                        val mestoZaSvirke = MestaZaSvirke(
                            id = userKey!!,
                            title=title,
                            vrstaMuzike=tipMuzike,
                            description=description,
                            ownerId=ownerId!!,
                            date=date,
                            latitude = lat!!,
                            longitude = lon!!,
                            imageURL = uri.toString()
                        )
                        val databaseUser =
                            FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("MestaZaSvirke")
                        databaseUser.child(mestoZaSvirke.id).get().addOnCompleteListener { task ->
                            val dataSnapshot = task.result
                            databaseUser?.child(mestoZaSvirke.id)?.setValue(mestoZaSvirke)
                                ?.addOnSuccessListener {
                                    MestaZaSvirkeViewModel.addSvirka(
                                        mestoZaSvirke,
                                        loggedUserViewModel.user!!
                                    )
                                    Navigation.findNavController(binding.root)
                                        .navigate(R.id.action_addMestaZaSvirkeFragment_to_homeFragment)
                                    Toast.makeText(
                                        activityObj,
                                        "Uspesno dodata svirka",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                ?.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        activityObj,
                                        "Doslo je do greske prilikom cuvanja podataka: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
                    .addOnProgressListener { taskSnapshot ->
                        val percent =
                            ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                        progressDialog.progress = percent
                        progressDialog.setMessage("Dodavanje: $percent %")
                    }
                    .addOnFailureListener {
                        val activityObj: Activity? = this.activity
                        Toast.makeText(
                            activityObj,
                            "Doslo je do greske prilikom uploadovanja slike",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                    }
            }

        } else {
            Toast.makeText(this.activity, "Morate uneti sva polja", Toast.LENGTH_SHORT).show()
        }
    }
}