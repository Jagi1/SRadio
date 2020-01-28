package pl.sbandurski.simpleradio.view.view.activity

import android.content.*
import android.content.res.ColorStateList
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_radio.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.adapter.TrackListAdapter
import pl.sbandurski.simpleradio.view.adapter.ViewPagerAdapter
import pl.sbandurski.simpleradio.view.listener.ILoadingStationAnimationListener
import pl.sbandurski.simpleradio.view.listener.TrackChangeListener
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.view.fragment.ListFragment
import pl.sbandurski.simpleradio.view.view.fragment.RadioFragment
import pl.sbandurski.simpleradio.view.view.fragment.SettingsFragment
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.model.StationsCache
import pl.sbandurski.simpleradio.view.model.Track
import pl.sbandurski.simpleradio.view.service.RadioService
import pl.sbandurski.simpleradio.view.util.*
import pl.sbandurski.simpleradio.view.viewmodel.MainViewModel
import java.util.*

class MainActivity :
    AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener,
    ViewPager.OnPageChangeListener,
    OnItemClickListener,
    TrackChangeListener,
    ILoadingStationAnimationListener{

    lateinit var viewModel: MainViewModel
    private var prevMenuItem: MenuItem? = null
    private var updateUIHandler: Handler? = null
    var mAdapter: TrackListAdapter? = null
    private val MESSAGE_UPDATE_TRACKS = 1
    private lateinit var mReceiver: HeadsetReceiver
    private val PACKAGE = "pl.qwisdom.simpleradio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        StationsCache.stations?.let {
            viewModel.setAllStations(it)
            StationsCache.stations = null
        }

        viewModel.getGradientDrawable().observe(this, androidx.lifecycle.Observer { gradient ->
            main_layout.apply {
                setBackgroundDrawable(gradient)
            }
        })

        val color = resources.getColor(R.color.colorAccent)
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled), // enabled
            intArrayOf(-android.R.attr.state_enabled), // disabled
            intArrayOf(-android.R.attr.state_checked), // unchecked
            intArrayOf(android.R.attr.state_pressed)  // pressed
        )
        val colors = intArrayOf(color, color, color, color)
        navigation_view.itemIconTintList = ColorStateList(states, colors)
        navigation_view.itemTextColor = ColorStateList(states, colors)

        viewModel.mOrientation = resources.configuration.orientation

        viewModel.trackListener = this

        viewModel.animationListener = this

        if (viewModel.mBound) {
//            viewModel.mService!!.mPlayer.start()
        }

        createUpdateUIHandler()
        registerHeadsetUnplugReceiver()
        prepareViewPager()
        val message = Message()
        if (viewModel.mTracks.value?.isEmpty() == false) {
            message.what = MESSAGE_UPDATE_TRACKS
            updateUIHandler?.sendMessage(message)
        }

        viewModel.fetchCountries()
        viewModel.fetchGenres()
    }

    private fun prepareViewPager() {
        pager.addOnPageChangeListener(this)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(RadioFragment.newInstance())
        adapter.addFragment(ListFragment.newInstance())
        adapter.addFragment(SettingsFragment.newInstance())
        pager.adapter = adapter
        pager.offscreenPageLimit = 2
        navigation_view.setOnNavigationItemSelectedListener(this)
    }

    /**
     * Listener for RecyclerView in ListFragment.
     * */
    override fun onItemClick(station: Station) {
        viewModel.showLoadingAnimation(spinningLoadingScreen)
        pager.currentItem = 0
        viewModel.mCurrentStation.value = station
        track_title.text = ""
        track_artist.text = ""
        viewModel.setPalette(station.getImage()!!)
        viewModel.setGradientDrawable()
        play_pause.setImageDrawable(getDrawable(R.drawable.ic_pause_24dp))
        val intent = Intent(this, RadioService::class.java)
        intent.let {
            it.putExtra("NAME", station.getName())
            it.putExtra("URL", station.getUrl())
            it.putExtra("DRAWABLE_ID", station.getDrawableID())
            it.putExtra("PALETTE", viewModel.mGradientPalette.value)
        }
//        title_tv.text = station.getName()
        when (viewModel.mBound) {
            true -> {
                applicationContext.unbindService(viewModel.mConnection)
                applicationContext.bindService(
                    intent,
                    viewModel.mConnection,
                    Context.BIND_AUTO_CREATE
                )
            }
            else -> applicationContext.bindService(
                intent,
                viewModel.mConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onTrackChange(track: ParsingHeaderData.TrackData) {
        val calendar = Calendar.getInstance(Locale.getDefault())
        var hour: String = calendar.get(Calendar.HOUR_OF_DAY).toString()
        var minute: String = calendar.get(Calendar.MINUTE).toString()
        if (viewModel.m12Hour && calendar.get(Calendar.HOUR_OF_DAY)>12) {
            hour = (calendar.get(Calendar.HOUR_OF_DAY)-12).toString()
        }
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) hour = "0$hour"
        if (calendar.get(Calendar.MINUTE) < 10) minute = "0$minute"
        viewModel.mCurrentTrackData.postValue(track)
        if (viewModel.mCurrentStation.value?.getName().equals("Antyradio")) {
            viewModel.mTracks.value?.add(
                0,
                Track(
                    time = "$hour:$minute",
                    artist = track.title,
                    title = track.artist
                )
            )
        } else {
            viewModel.mTracks.value?.add(
                0,
                Track(
                    time = "$hour:$minute",
                    artist = track.artist,
                    title = track.title
                )
            )
        }
        val thread = object : Thread() {
            override fun run() {
                val message = Message()
                message.what = MESSAGE_UPDATE_TRACKS
                updateUIHandler?.sendMessage(message)
            }
        }
        thread.start()
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        Log.d("PAGE_SELECT", "1: ${p0.itemId}")
        pager.currentItem = when (p0.itemId) {
            R.id.radio_item -> 0
            R.id.list_item -> 1
            R.id.settings_item -> 2
            else -> 0
        }
        if (p0.itemId == 1) {
            viewModel.showStationsIfFirstOpen()
        }
        return false
    }

    override fun onPageSelected(p0: Int) {
        if (prevMenuItem != null)
            prevMenuItem!!.isChecked = false
        else
            navigation_view.menu.getItem(0).isChecked = false
        navigation_view.menu.getItem(p0).isChecked = true
        prevMenuItem = navigation_view.menu.getItem(p0)
        if (p0 == 1) {
            viewModel.showStationsIfFirstOpen()
        }
    }

    override fun onPageScrollStateChanged(p0: Int) {
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
    }

    private fun registerHeadsetUnplugReceiver() {
        mReceiver = HeadsetReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_HEADSET_PLUG)
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        registerReceiver(mReceiver, filter)
    }

    private fun playPauseRadio() {
        vibrate(this, 20)
        if (viewModel.mService != null) {
            if (viewModel.mService!!.mPlayer.playWhenReady) {
                if (viewModel.mService!!.mPlayer.playWhenReady) {
                    play_pause.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_24dp))
                } else play_pause.setImageDrawable(getDrawable(R.drawable.ic_pause_24dp))
                when (viewModel.mService!!.mPlayer.playWhenReady) {
                    true -> viewModel.mService!!.mPlayer.playWhenReady = false
                    else -> viewModel.mService!!.mPlayer.playWhenReady = true
                }
            }
        }
    }

    override fun onLoadingStationAnimationChange() {
        if (spinningLoadingScreen.visibility == View.VISIBLE) viewModel.hideLoadingAnimation(spinningLoadingScreen)
        else viewModel.showLoadingAnimation(spinningLoadingScreen)
    }

    private inner class HeadsetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(Intent.ACTION_HEADSET_PLUG) && viewModel.mService != null) {
                val state = intent?.getIntExtra("state", -1)
                if (state == 0 && !viewModel.mRotating) {
                    viewModel.mRotating = false
                    playPauseRadio()
                }
            } else if (intent?.action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                viewModel.mRotating = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.mRotating = this.isRecreating()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        if (!viewModel.mRotating && viewModel.mBound) {
            applicationContext.unbindService(viewModel.mConnection)
        }
    }

    private fun createUpdateUIHandler() {
        if (updateUIHandler == null) {
            updateUIHandler = object : Handler() {
                override fun handleMessage(msg: Message?) {
                    if (msg?.what == MESSAGE_UPDATE_TRACKS) {
                        updateTrackList()
                    }
                }
            }
        }
    }

    fun isRecreating(): Boolean = this.isChangingConfigurations

    fun updateTrackList() {
        mAdapter?.notifyDataSetChanged()
    }

    private fun getDrawable(name: String): Int = getId(name, "drawable")

    private fun getId(name: String, type: String) = resources.getIdentifier(name, type, PACKAGE)
}
