package elfak.mosis.svirke.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.svirke.R
import elfak.mosis.svirke.classes.User
import java.io.FileNotFoundException
import java.io.InputStream
import elfak.mosis.svirke.databinding.FragmentRegisterBinding
import kotlin.random.Random
import java.security.MessageDigest


class RegisterFragment : Fragment() {

    private val random = Random(System.currentTimeMillis())
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var databaseUser:DatabaseReference?=null
    private var downloadUrl:String=""
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Registrovanje: 0 %")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.btnAddImage.setOnClickListener{
            otvoriGaleriju()
        }
        binding.btnRegister.setOnClickListener{
            register()
        }
        binding.loginNow.setOnClickListener{
            Navigation.findNavController(binding.root).navigate(R.id.action_registerFragment_to_login)
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Restore the visibility of the toolbar when the fragment is destroyed
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        _binding = null
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
    private fun register() {
        val editIme = requireView().findViewById<EditText>(R.id.ime)
        val editPrezime = requireView().findViewById<EditText>(R.id.prezime)
        val editUsername = requireView().findViewById<EditText>(R.id.username)
        val editPassword = requireView().findViewById<EditText>(R.id.password)
        val editBroj = requireView().findViewById<EditText>(R.id.broj_telefona)


        val ime = editIme.text.toString()
        val prezime = editPrezime.text.toString()
        val username = editUsername.text.toString()
        val password = hashPassword(editPassword.text.toString())
        val brojTelefona = editBroj.text.toString()

        if (ime != "" && prezime != "" && username != "" && password != "" && brojTelefona != "") {
            databaseUser =
                FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("Users")

            val storageRef= FirebaseStorage.getInstance().getReference();
            val stringBuilder = StringBuilder()
            for (i in 1..50) {
                val randomDigit = random.nextInt(10)
                stringBuilder.append(randomDigit)
            }
            if (selectedImageUri!=null) {
                progressDialog.show()
                val fileRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {

                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val activityObj: Activity? = this.activity
                            val user = User(ime, prezime, username, password, brojTelefona,uri.toString())
                            if (user.username != null) {
                                val databaseUser = FirebaseDatabase.getInstance("https://svirke-4ddc6-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
                                databaseUser.child(username).get().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val dataSnapshot = task.result
                                        if (dataSnapshot.exists()) {
                                            Toast.makeText(activityObj, "Vec postoji nalog sa tim usernameom", Toast.LENGTH_LONG).show()
                                        }
                                        else
                                        {
                                            databaseUser?.child(user.username)?.setValue(user)
                                                ?.addOnSuccessListener {
                                                    Navigation.findNavController(binding.root).navigate(R.id.action_registerFragment_to_login)
                                                    Toast.makeText(activityObj, "Uspesno registrovan korisnik", Toast.LENGTH_LONG).show()
                                                }
                                                ?.addOnFailureListener {
                                                    Toast.makeText(activityObj, "Bezuspesno registrovanje", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                }
                            } else {
                                val activityObj: Activity? = this.activity
                                Toast.makeText(activityObj, "Unesite sve podatke", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val percent = ((100 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                        progressDialog.progress = percent
                        progressDialog.setMessage("Registrovanje: $percent %")
                    }
                    .addOnFailureListener {
                        val activityObj: Activity? = this.activity
                        Toast.makeText(activityObj, "Doslo je do greske prilikom uploadovanja slike", Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                    }
            }

        }
    }

    private fun otvoriGaleriju()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v.ushr(4)]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }


    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}