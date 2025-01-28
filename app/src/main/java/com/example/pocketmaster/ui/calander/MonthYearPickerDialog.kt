package com.example.pocketmaster.ui.calander

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pocketmaster.R

import com.example.pocketmaster.databinding.FragmentMonthYearPickerDialogBinding
import java.util.Calendar

class MonthYearPickerDialog : DialogFragment() {

    private var onDateSelected: ((Int, Int) -> Unit)? = null
    private var _binding: FragmentMonthYearPickerDialogBinding? = null
    private val binding get() = _binding!!

    fun setOnDateSelectedListener(listener: (Int, Int) -> Unit) {
        onDateSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentMonthYearPickerDialogBinding.inflate(LayoutInflater.from(context))

        val calendar = Calendar.getInstance()

        // Set up DatePicker
        binding.datePicker.apply {
            init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ) { _, _, _, _ -> }
        }

        // Hide day spinner
        val daySpinner = binding.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )
        daySpinner?.visibility = View.GONE

        // Set up button click listeners
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            onDateSelected?.invoke(
                binding.datePicker.year,
                binding.datePicker.month
            )
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MonthYearPickerDialog"
    }
}