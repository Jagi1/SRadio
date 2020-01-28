package pl.sbandurski.simpleradio.view.viewmodel

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.listener.ILoadingStationAnimationListener
import pl.sbandurski.simpleradio.view.listener.TrackChangeListener
import pl.sbandurski.simpleradio.view.model.GradientPalette
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.model.Track
import pl.sbandurski.simpleradio.view.service.RadioService
import pl.sbandurski.simpleradio.view.util.ParsingHeaderData
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MainViewModel : ViewModel() {

    var mGradientDrawable: MutableLiveData<GradientDrawable> = MutableLiveData()
    var mGradientPalette : MutableLiveData<GradientPalette> = MutableLiveData()
    var mPalette: MutableLiveData<Palette> = MutableLiveData()
    var mTracks: MutableLiveData<ArrayList<Track>> =
        MutableLiveData<ArrayList<Track>>().default(ArrayList<Track>())
    var mCurrentStation: MutableLiveData<Station> = MutableLiveData()
    var mCurrentTrackData: MutableLiveData<ParsingHeaderData.TrackData> = MutableLiveData()
    var m12Hour: Boolean = false
    var mBound: Boolean = false
    var mRotating: Boolean = false
    var mService: RadioService? = null
    var trackListener: TrackChangeListener? = null
    var animationListener: ILoadingStationAnimationListener? = null
    val PACKAGE = "pl.qwisdom.simpleradio"
    var resources: Resources? = null
    var mOrientation: Int? = null

    // Database data
    var mStations: MutableLiveData<ArrayList<Station>> = MutableLiveData()
    var mAllStations : MutableLiveData<ArrayList<Station>> = MutableLiveData()
    var mCountries : Array<String?>? = null
    var mGenres : Array<String?>? = null
    var firstOpen = true

    val mConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            mService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioService.LocalBinder
            mService = binder.getService()
            mService!!.setTrackListener(trackListener!!)
            mService!!.setLoadingAnimationListener(animationListener!!)
            mBound = true
            mService!!.setStation(mCurrentStation.value!!)
            mService!!.mPlayer.playWhenReady = true
        }
    }

    fun fetchGenres() {
        val list = ArrayList<String>()
        val database = FirebaseFirestore.getInstance()
        database.collection("genres")
            .get()
            .addOnSuccessListener { genres ->
                for (genre in genres) {
                    list.add(genre.data["name"].toString())
                }
                mGenres = arrayOfNulls(list.size)
                list.toArray(mGenres)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun fetchCountries() {
        val list = ArrayList<String>()
        val database = FirebaseFirestore.getInstance()
        database.collection("countries")
            .get()
            .addOnSuccessListener { countries ->
                for (country in countries) {
                    list.add(country.data["name"].toString())
                }
                mCountries = arrayOfNulls(list.size)
                list.toArray(mCountries)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun filterStations(filter : SearchFilter) {
        val metrics = resources!!.displayMetrics
        val width = (metrics.widthPixels * 0.9).toInt()
        val height = (metrics.heightPixels * 0.52).toInt()

        val list = ArrayList<Station>()
        var stations = mAllStations.value
        if (stations != null) {
            for (station in stations) {
                if (filter.name?.isNotEmpty() == true) {
                    if (!station.getName().toLowerCase().contains(
                            filter.name,
                            true
                        )
                    ) {
                        continue
                    }
                }
                if (filter.country?.isNotEmpty() == true) {
                    if (!station.getCountry().toLowerCase().contains(
                            filter.country,
                            true
                        )
                    ) {
                        continue
                    }
                }
                if (filter.genre?.isNotEmpty() == true) {
                    if (!station.getType().toLowerCase().contains(
                            filter.genre,
                            true
                        )
                    ) {
                        continue
                    }
                }
                val id = station.getDrawableID()
                val image = BitmapFactory.decodeResource(resources, R.drawable.item_logo)
                val resizedImage = Bitmap.createScaledBitmap(image, width, width, true)
                list.add(
                    Station(
                        name = station.getName(),
                        image = resizedImage,
                        url = station.getUrl(),
                        drawableID = id,
                        type = station.getType(),
                        bitrate = station.getBitrate(),
                        logoUrl = id,
                        country = station.getCountry(),
                        new = station.getNew()
                    )
                )
            }
        }
        mStations.value = list
    }

    fun setAllStations(stations : ArrayList<Station>) {
        mAllStations.value = stations
    }

    fun showStationsIfFirstOpen() {
        if (firstOpen) {
            firstOpen = false
            val handler = Handler()
            val runnable = Runnable {
                val filter = SearchFilter("", "", "")
                filterStations(filter)

            }
            handler.postDelayed(runnable, 500)
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
        val dominantSwatch = mPalette.value?.dominantSwatch?.rgb
        val vibrantSwatch = mPalette.value?.vibrantSwatch?.rgb
        val lightVibrantSwatch = mPalette.value?.lightVibrantSwatch?.rgb
        var color: Int?
        color = when {
            darkVibrant != null -> darkVibrant
            darkMuted != null -> darkMuted
            muted != null -> muted
            lightMuted != null -> lightMuted
            dominantSwatch != null -> dominantSwatch
            vibrantSwatch != null -> vibrantSwatch
            lightVibrantSwatch != null -> lightVibrantSwatch
            else -> 0xFFfafafa.toInt()
        }

        val gradientPalette = GradientPalette(darkVibrant, darkMuted, muted, lightMuted, dominantSwatch, vibrantSwatch, lightVibrantSwatch)
        mGradientPalette.value = gradientPalette

        mGradientDrawable.value = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            listOf(color, 0xFF131313.toInt()).toIntArray()
        )
    }

    fun setDefaultGradientPalette() {
        val palette = GradientPalette(
            0xFFfafafa.toInt(), 0xFFfafafa.toInt(), 0xFFfafafa.toInt(), 0xFFfafafa.toInt(), 0xFFfafafa.toInt(), 0xFFfafafa.toInt(), 0xFFfafafa.toInt()
        )
        mGradientPalette.value = palette
    }

    fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { value = initialValue }

    fun getGradientDrawable(): MutableLiveData<GradientDrawable> = mGradientDrawable

    /**
     * It hides animation of loading radio station.
     * */
    fun hideLoadingAnimation(animation: SpinKitView?) {
        animation?.visibility = View.GONE
    }

    /**
     * It shows animation of loading radio station.
     * */
    fun showLoadingAnimation(animation: SpinKitView?) {
        animation?.visibility = View.VISIBLE
    }

    fun changeFilterAlpha(image: ImageView?, floatingButton: FloatingActionButton?, newState: Int) {
        if (newState != 2) {
            when (floatingButton?.alpha) {
                1.0f -> 0.4f
                else -> 1.0f
            }.apply {
                floatingButton?.alpha = this
                image?.alpha = this
                floatingButton?.alpha = this
            }
        }
    }

    private fun getDrawable(name: String): Int = getId(name, "drawable")

    private fun getId(name: String, type: String) = resources!!.getIdentifier(name, type, PACKAGE)
}