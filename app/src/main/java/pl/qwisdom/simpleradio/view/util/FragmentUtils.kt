package pl.qwisdom.simpleradio.view.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import pl.qwisdom.simpleradio.R

fun openFragment(fragment: Fragment, @Nullable sfm: FragmentManager) {
    val transaction = sfm.beginTransaction()
    transaction.replace(R.id.container, fragment)
    transaction.addToBackStack(null)
    transaction.commit()
}

fun hideSoftKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(
        activity.currentFocus?.windowToken, 0
    )
}

fun showSoftKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun vibrate(context: Context, time: Long) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
    } else vibrator.vibrate(time)
}