package elfak.mosis.svirke.fragments

import androidx.fragment.app.viewModels


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.svirke.R
import elfak.mosis.svirke.adapter.LeaderBoardAdapter
import elfak.mosis.svirke.classes.User
import elfak.mosis.svirke.databinding.FragmentLeaderBoardBinding
import elfak.mosis.svirke.viewmodels.UsersViewModel
class LeaderBoardFragment : Fragment() {
    private lateinit var _binding: FragmentLeaderBoardBinding
    private val binding get() = _binding!!
    private val usersViewModel: UsersViewModel by activityViewModels()
    private var usersList: List<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(this.activity, "kurac", Toast.LENGTH_SHORT).show()
        val leaderboardAdapter= LeaderBoardAdapter(requireContext(),usersViewModel.users!!)
        binding.listViewLeaderboard.adapter=leaderboardAdapter
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.action_show_scoreboard)
        item.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout ->{
                val sharedPreferences =
                    requireContext().getSharedPreferences("Svirke", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                this.findNavController().navigate(R.id.action_leaderBoardFragment_to_login)
                true
            }
            R.id.action_add->
            {
                this.findNavController().navigate(R.id.action_leaderBoardFragment_to_addMestaZaSvirkeFragment)
                true
            }
            R.id.action_show_map->
            {
                this.findNavController().navigate(R.id.action_leaderBoardFragment_to_homeFragment)
                true
            }
            else->super.onContextItemSelected(item)
        }
    }
}