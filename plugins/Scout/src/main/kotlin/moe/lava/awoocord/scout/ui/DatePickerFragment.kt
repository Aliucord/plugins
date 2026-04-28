package moe.lava.awoocord.scout.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.Calendar

class DatePickerFragment(
    private val callback: (String) -> Unit
) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    companion object {
        fun open(fragmentManager: FragmentManager, callback: (date: String) -> Unit) {
            DatePickerFragment(callback).show(fragmentManager, "datePicker")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), android.R.style.Theme_DeviceDefault_Dialog, this, year, month, day).apply {
            datePicker.maxDate = calendar.timeInMillis
        }
    }

    override fun onDateSet(picker: DatePicker, year: Int, month: Int, day: Int) {
        callback("%04d-%02d-%02d".format(year, month + 1, day))
    }
}
