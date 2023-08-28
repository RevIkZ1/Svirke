package elfak.mosis.svirke.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import elfak.mosis.svirke.R
import elfak.mosis.svirke.databinding.FragmentInfoBinding
import elfak.mosis.svirke.viewmodels.LoggedUserViewModel
import elfak.mosis.svirke.viewmodels.MestaZaSvirkeViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val MestaZaSvirkeViewModel: MestaZaSvirkeViewModel by activityViewModels()
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        return binding.root

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
                this.findNavController().navigate(elfak.mosis.svirke.R.id.action_infoFragment_to_login)
                true
            }

            R.id.action_show_map -> {
                this.findNavController()
                    .navigate(R.id.action_infoFragment_to_homeFragment)
                true
            }
            R.id.action_add->
            {
                this.findNavController()
                    .navigate(R.id.action_infoFragment_to_addMestaZaSvirkeFragment)
                true
            }
            R.id.action_show_profile->
            {
                this.findNavController()
                    .navigate(R.id.action_infoFragment_to_profileFragment)
                true
            }
            R.id.action_show_scoreboard->
            {
                this.findNavController()
                    .navigate(R.id.action_infoFragment_to_leaderBoardFragment)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.info.setText(MestaZaSvirkeViewModel.svirka?.title)
        val datum=MestaZaSvirkeViewModel.svirka?.date.toString()
        binding.datum.setText(datum)
        binding.opis.setText(MestaZaSvirkeViewModel.svirka?.description)
        binding.vrsta.setText(MestaZaSvirkeViewModel.svirka?.vrstaMuzike)
        val imageUrl = MestaZaSvirkeViewModel.svirka?.imageURL
        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions().placeholder(R.drawable.placeholder_image))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.image)
        val likeButton = view.findViewById<Button>(R.id.likeButton)
        val dislikeButton = view.findViewById<Button>(R.id.dislikeButton)
        likeButton.setOnClickListener {
            Toast.makeText(this.activity, "Like", Toast.LENGTH_SHORT).show()
            MestaZaSvirkeViewModel.like(loggedUserViewModel.user!!,MestaZaSvirkeViewModel.svirka!!)
        }
        dislikeButton.setOnClickListener {
            Toast.makeText(this.activity, "Dislike", Toast.LENGTH_SHORT).show()
            MestaZaSvirkeViewModel.dislike(loggedUserViewModel.user!!,MestaZaSvirkeViewModel.svirka!!)
        }
        binding.likeCount.setText(MestaZaSvirkeViewModel.svirka!!.like.toString())
        binding.dislikeCount.setText(MestaZaSvirkeViewModel.svirka!!.dislike.toString())

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}