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
    private var _binding: FragmentMonthYearPickerDialogBinding? = null
    private val binding get() = _binding!!

    private var onDateSelected: ((Int, Int) -> Unit)? = null
    private var onReset: (() -> Unit)? = null

    fun setOnDateSelectedListener(listener: (Int, Int) -> Unit) {
        onDateSelected = listener
    }

    fun setOnResetListener(listener: () -> Unit) {
        onReset = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentMonthYearPickerDialogBinding.inflate(LayoutInflater.from(context))

        val calendar = Calendar.getInstance()

        // Set up DatePicker
        binding.datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, _, _, _ -> }

        // Hide day spinner
        binding.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE

        binding.btnReset.setOnClickListener {
            onReset?.invoke()
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            onDateSelected?.invoke(
                binding.datePicker.year,
                binding.datePicker.month
            )
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
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