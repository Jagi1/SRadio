package pl.qwisdom.simpleradio.view.view.fragment

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_feedback.view.*
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.util.hideSoftKeyboard
import pl.qwisdom.simpleradio.view.view.activity.MainActivity
import java.util.*

class SettingsFragment: PreferenceFragmentCompat() {

    private lateinit var act: MainActivity
    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var orientation: SwitchPreference
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
        createFeedbackDialog()
        initPreferences()
    }

    private fun initPreferences() {
        feedback = findPreference("feedback")
        orientation = findPreference("orientation") as SwitchPreference
        time = findPreference("time") as SwitchPreference
        prepareTime()
        prepareOrientation()
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

    private fun prepareOrientation() {
        if (orientation.isChecked) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        orientation.setOnPreferenceClickListener { preference ->
            if (orientation.isChecked) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
            true
        }
    }

    private fun prepareFeedback() {
        feedback.setOnPreferenceClickListener {
            dialog.show()
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            true
        }
    }


    private fun createFeedbackDialog() {
        builder = AlertDialog.Builder(context)
        dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null, false)
        dialogView.send_feedback.setOnClickListener {
            hideSoftKeyboard(act)
            val feedback = HashMap<String, Any>()
            val calendar = Calendar.getInstance(Locale.getDefault())
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            feedback["text"] = dialogView.text_feedback.text.toString()
            database.collection("feedback").document("$year.$month.$day $hour:$minute").set(feedback)
                .addOnSuccessListener {
                    Snackbar.make(act.navigation_view, "Feedback sent", Snackbar.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Snackbar.make(act.navigation_view, "Feedback have not been sent", Snackbar.LENGTH_SHORT).show()
                }.addOnCompleteListener {
                    dialog.dismiss()
                }
        }
        builder.setView(dialogView)
        dialog = builder.create()
    }
}