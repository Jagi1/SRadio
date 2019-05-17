package pl.qwisdom.simpleradio.view.viewmodel

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.support.v7.graphics.Palette
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_list.*
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.listener.TrackChangeListener
import pl.qwisdom.simpleradio.view.model.Station
import pl.qwisdom.simpleradio.view.model.Track
import pl.qwisdom.simpleradio.view.service.RadioService
import pl.qwisdom.simpleradio.view.util.ParsingHeaderData
import pl.qwisdom.simpleradio.view.view.fragment.ListFragment

class MainViewModel : ViewModel() {

    var mGradientDrawable: MutableLiveData<GradientDrawable> = MutableLiveData()
    var mPalette: MutableLiveData<Palette> = MutableLiveData()
    var mTracks: MutableLiveData<ArrayList<Track>> =
        MutableLiveData<ArrayList<Track>>().default(ArrayList<Track>())
    var mCurrentStation: MutableLiveData<Station> = MutableLiveData()
    var mCurrentTrackData: MutableLiveData<ParsingHeaderData.TrackData> = MutableLiveData()
    var mStations: MutableLiveData<ArrayList<Station>> =
        MutableLiveData<ArrayList<Station>>().default(
            ArrayList<Station>()
        )
    var m12Hour: Boolean = false
    var mBound: Boolean = false
    var mRotating: Boolean = false
    var mService: RadioService? = null
    var trackListener: TrackChangeListener? = null
    val PACKAGE = "pl.qwisdom.simpleradio"
    var resources: Resources? = null
    var mOrientation: Int? = null

    val mConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            mService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioService.LocalBinder
            mService = binder.getService()
            mService!!.setTrackListener(trackListener!!)
            mBound = true
            mService!!.setStation(mCurrentStation.value!!)
        }
    }

    fun fetchStations(fragment: ListFragment) {
        val list = ArrayList<Station>()
        val database = FirebaseFirestore.getInstance()
        database.collection("stations")
            .get()
            .addOnSuccessListener { stations ->
                for (station in stations) {
                    if (fragment.name_check.isChecked) {
                        if (!station.data["name"].toString().toLowerCase().contains(
                                fragment.name_edit.text.toString(),
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (fragment.country_check.isChecked) {
                        if (!station.data["country"].toString().toLowerCase().contains(
                                fragment.countries.selectedItem.toString(),
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (fragment.type_check.isChecked) {
                        if (!station.data["type"].toString().toLowerCase().contains(
                                fragment.types.selectedItem.toString(),
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    val id = station.id
                    list.add(
                        Station(
                            name = station.data["name"] as String,
                            image = BitmapFactory.decodeResource(resources, R.drawable.item_logo),
                            url = station.data["url"] as String,
                            drawableID = id,
                            type = station.data["type"] as String,
                            bitrate = station.data["bitrate"] as String,
                            logoUrl = station.id
                        )
                    )
                }
                mStations.value = list
            }.addOnFailureListener { ex ->
                ex.printStackTrace()
            }
    }

    fun setPalette(bitmap: Bitmap) {
        mPalette.value = Palette.from(bitmap).generate()
    }

    fun getPalette(): Palette? = mPalette.value

    fun setGradientDrawable() {
        val darkVibrant = mPalette.value?.darkVibrantSwatch?.rgb
        val darkMuted = mPalette.value?.darkMutedSwatch?.rgb
        val muted = mPalette.value?.mutedSwatch?.rgb
        val lightMuted = mPalette.value?.lightMutedSwatch?.rgb
        var color: Int?
        color = when {
            darkVibrant != null -> darkVibrant
            darkMuted != null -> darkMuted
            muted != null -> muted
            lightMuted != null -> lightMuted
            else -> 0x00000000
        }
        mGradientDrawable.value = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            listOf(color, 0xFF131313.toInt()).toIntArray()
        )
    }

    fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { value = initialValue }

    fun getGradientDrawable(): MutableLiveData<GradientDrawable> = mGradientDrawable

    private fun getDrawable(name: String): Int = getId(name, "drawable")

    private fun getId(name: String, type: String) = resources!!.getIdentifier(name, type, PACKAGE)
}