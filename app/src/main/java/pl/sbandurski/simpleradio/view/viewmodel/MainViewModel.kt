package pl.sbandurski.simpleradio.view.viewmodel

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_list.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.listener.ILoadingStationAnimationListener
import pl.sbandurski.simpleradio.view.listener.TrackChangeListener
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.model.Track
import pl.sbandurski.simpleradio.view.service.RadioService
import pl.sbandurski.simpleradio.view.util.ParsingHeaderData
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import pl.sbandurski.simpleradio.view.view.fragment.ListFragment

class MainViewModel : ViewModel() {

    var mGradientDrawable: MutableLiveData<GradientDrawable> = MutableLiveData()
    var mPalette: MutableLiveData<Palette> = MutableLiveData()
    var mTracks: MutableLiveData<ArrayList<Track>> =
        MutableLiveData<ArrayList<Track>>().default(ArrayList<Track>())
    var mCurrentStation: MutableLiveData<Station> = MutableLiveData()
    var mCurrentTrackData: MutableLiveData<ParsingHeaderData.TrackData> = MutableLiveData()
    var mStations: MutableLiveData<ArrayList<Station>> = MutableLiveData()
    var mCountries : Array<String?>? = null
    var mGenres : Array<String?>? = null
    var m12Hour: Boolean = false
    var mBound: Boolean = false
    var mRotating: Boolean = false
    var mService: RadioService? = null
    var trackListener: TrackChangeListener? = null
    var animationListener: ILoadingStationAnimationListener? = null
    val PACKAGE = "pl.qwisdom.simpleradio"
    var resources: Resources? = null
    var mOrientation: Int? = null

    val mConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("IKSDE", "onServiceDisconnected($name)")
            mBound = false
            mService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("IKSDE", "onServiceConnected($name, $service)")
            val binder = service as RadioService.LocalBinder
            mService = binder.getService()
            mService!!.setTrackListener(trackListener!!)
            mService!!.setLoadingAnimationListener(animationListener!!)
            mBound = true
            mService!!.setStation(mCurrentStation.value!!)
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

    fun fetchStations(filter : SearchFilter) {
        val list = ArrayList<Station>()
        val database = FirebaseFirestore.getInstance()
        database.collection("stations")
            .get()
            .addOnSuccessListener { stations ->
                for (station in stations) {
                    if (filter.name?.isNotEmpty() == true) {
                        if (!station.data["name"].toString().toLowerCase().contains(
                                filter.name,
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (filter.country?.isNotEmpty() == true) {
                        if (!station.data["country"].toString().toLowerCase().contains(
                                filter.country,
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (filter.genre?.isNotEmpty() == true) {
                        if (!station.data["type"].toString().toLowerCase().contains(
                                filter.genre,
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

    fun hideFilterCard(card: MaterialCardView?, image: ImageView?) {
        card?.visibility = View.GONE
        image?.visibility = View.GONE
    }

    fun showFilterCard(card: MaterialCardView?, image: ImageView?) {
        card?.visibility = View.VISIBLE
        image?.visibility = View.VISIBLE
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