package elfak.mosis.svirke.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import elfak.mosis.svirke.R
import elfak.mosis.svirke.databinding.FragmentFilterBinding
import elfak.mosis.svirke.viewmodels.LoggedUserViewModel
import elfak.mosis.svirke.viewmodels.MestaZaSvirkeViewModel
import java.util.Calendar
import java.util.Date

class FilterFragment :  DialogFragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val mestoZaSvirkeViewModel: MestaZaSvirkeViewModel by activityViewModels()
    private val loggedUserViewModel: LoggedUserViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editDatee: DatePicker = view.findViewById(R.id.datePicker)
        val currentDate = Calendar.getInstance()
        var date: Date?=null
        val editDate: DatePicker = view?.findViewById(R.id.datePicker)!!
        val calendar = Calendar.getInstance()
        calendar.set(editDate.year, editDate.month, editDate.dayOfMonth)
        date = calendar.time

        editDatee.updateDate(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH))
        binding.filterMestaZaSvirke.setOnClickListener {
            val editName = requireView().findViewById<EditText>(R.id.editTextNaziv)
            val editTipMuzike = requireView().findViewById<Spinner>(R.id.spinnerVrstaMuzike)
            val editOglasavac = requireView().findViewById<EditText>(R.id.editTextOglasivac)
            val editRadijus = requireView().findViewById<EditText>(R.id.radijus)
            val oglasavac = editOglasavac.text.toString()
            val name = editName.text.toString()
            val tipMuzike = editTipMuzike.selectedItem.toString()
            val rad = editRadijus.text.toString()

            var radijus = 0.0
            if (rad != "") {
                radijus = rad?.toDoubleOrNull()!!
            }

            val calendar = Calendar.getInstance()
            calendar.set(editDate.year, editDate.month, editDate.dayOfMonth)
            date = calendar.time

            Toast.makeText(this.activity, "$date", Toast.LENGTH_SHORT).show()

            mestoZaSvirkeViewModel.filtrirajMestaZaSvirke(
                oglasavac,
                name,
                tipMuzike,
                radijus,
                date,
                loggedUserViewModel.location?.latitude!!,
                loggedUserViewModel.location?.longitude!!
            )
            dismiss()
        }

    }
}