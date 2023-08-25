package elfak.mosis.svirke.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import elfak.mosis.svirke.R
import elfak.mosis.svirke.databinding.FragmentInfoBinding
import elfak.mosis.svirke.databinding.FragmentProfileBinding
import elfak.mosis.svirke.viewmodels.LoggedUserViewModel
import elfak.mosis.svirke.viewmodels.UsersViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item=menu.findItem(R.id.action_show_profile)
        item.isVisible=false
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val sharedPreferences =
                    requireContext().getSharedPreferences("Svirke", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(elfak.mosis.svirke.R.id.action_profileFragment_to_login)
                true
            }

            R.id.action_show_map -> {
                this.findNavController()
                    .navigate(R.id.action_profileFragment_to_homeFragment)
                true
            }
            R.id.action_add->
            {
                this.findNavController()
                    .navigate(R.id.action_profileFragment_to_addMestaZaSvirkeFragment)
                true
            }
            R.id.action_show_scoreboard->
            {
                this.findNavController()
                    .navigate(R.id.action_profileFragment_to_leaderBoardFragment)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textUsername.setText(loggedUserViewModel.user?.username)
        binding.textFirstName.setText(loggedUserViewModel.user?.firstName)
        binding.textLastName.setText(loggedUserViewModel.user?.lastName)
        binding.textPhoneNumber.setText(loggedUserViewModel.user?.phoneNumber)
        binding.textPoints.setText((loggedUserViewModel.user?.points).toString())
        val imageUrl = loggedUserViewModel.user?.imageURl
        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions().placeholder(R.drawable.placeholder_image))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imageView)
    }
        companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}