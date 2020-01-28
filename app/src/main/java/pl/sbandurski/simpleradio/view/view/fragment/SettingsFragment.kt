package pl.sbandurski.simpleradio.view.view.fragment

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_feedback.view.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.util.hideSoftKeyboard
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import java.util.*

class SettingsFragment: PreferenceFragmentCompat() {

    private lateinit var act: MainActivity
    private lateinit var builder: AlertDialog.Builder
    private lateinit var time: SwitchPreference
    private lateinit var feedback: Preference
    private lateinit var dialogView: View
    private lateinit var database: FirebaseFirestore

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        setPreferencesFromResource(R.xml.app_preferences, p1)
        act = activity as MainActivity
        database = FirebaseFirestore.getInstance()
        initPreferences()
        act.viewModel.mGradientPalette.observe(this, androidx.lifecycle.Observer {
            it.lightVibrantSwatch?.let { nonNullColor ->
                time.icon.setTint(nonNullColor)
                feedback.icon.setTint(nonNullColor)
            }
        })
    }

    private fun initPreferences() {
        feedback = findPreference("feedback")
        time = findPreference("time") as SwitchPreference
        prepareTime()
        prepareFeedback()
    }

    private fun prepareTime() {
        try {
            act.viewModel.m12Hour = time.isChecked
            time.setOnPreferenceClickListener(fun(_: Preference): Boolean {
                act.viewModel.m12Hour = time.isChecked
                return true
            })
        } catch (e: UninitializedPropertyAccessException) {
            Log.d("TIME_EXCEPTION", e.message)
        }
    }

    private fun prepareFeedback() {
        feedback.setOnPreferenceClickListener {
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("bandurski.sebastian@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Simple Radio feedback")
            }
            try {
                startActivity(Intent.createChooser(sendEmail, "Send feedback..."))
            } catch (ex : ActivityNotFoundException) {
                Snackbar.make(act.navigation_view, "Couldn't send feedback...", Snackbar.LENGTH_SHORT).show()
            }
            true
        }
    }
}