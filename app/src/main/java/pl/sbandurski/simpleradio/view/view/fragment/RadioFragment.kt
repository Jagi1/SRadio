package pl.sbandurski.simpleradio.view.view.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_radio.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.adapter.SliderAdapter
import pl.sbandurski.simpleradio.view.adapter.TrackListAdapter
import pl.sbandurski.simpleradio.view.listener.TrackClickedListener
import pl.sbandurski.simpleradio.view.util.vibrate
import pl.sbandurski.simpleradio.view.view.activity.HistoryActivity
import pl.sbandurski.simpleradio.view.view.activity.MainActivity

class RadioFragment : Fragment(), View.OnClickListener, TrackClickedListener {

    private lateinit var act: MainActivity

    companion object {
        fun newInstance(): RadioFragment = RadioFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_radio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        act = activity as MainActivity
        play_pause.setOnClickListener(this)
        stop.setOnClickListener(this)
        history.setOnClickListener(this)

        setSlider()

        act.viewModel.mGradientPalette.observe(this, Observer { palette ->
            val color = palette?.let {
                when {
                    it.lightVibrantSwatch != null -> it.lightVibrantSwatch
                    it.lightMuted != null -> it.lightMuted
                    it.vibrantSwatch != null -> it.vibrantSwatch
                    it.dominantSwatch != null -> it.dominantSwatch
                    else -> 0xfafafa
                }
            } ?: 0xfafafa
            fragment_radio_bottom_bar.setBackgroundColor(color)
            track_artist.setTextColor(color)
            track_title.setTextColor(color)
            play_pause.clearColorFilter()
            play_pause.setColorFilter(color)
            history.clearColorFilter()
            history.setColorFilter(color)
            stop.clearColorFilter()
            stop.setColorFilter(color)
            val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled), // enabled
                intArrayOf(-android.R.attr.state_enabled), // disabled
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_pressed)  // pressed
            )
            val colors = intArrayOf(color, color, color, color)
            act.navigation_view.itemIconTintList = ColorStateList(states, colors)
            act.navigation_view.itemTextColor = ColorStateList(states, colors)
            //            act.navigation_view.menu.findItem(R.id.radio_item).setIconTintList()
        })

        // Update current track data
        act.viewModel.mCurrentTrackData.observe(this, Observer { track ->
            track_artist.text = track!!.artist
            track_title.text = track.title
            Log.d("TRACK_DATA", "Artist: ${track_artist.text}")
            Log.d("TRACK_DATA", "Title: ${track_title.text}")
        })

        // Update current station data
        act.viewModel.mCurrentStation.observe(this, Observer { station ->
            station_logo_civ_radio.setImageBitmap(station?.getImage())
        })

        if (act.viewModel.mService != null) {
            if (act.viewModel.mService!!.mPlayer.playWhenReady) play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_pause_24dp))
            else play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.play_pause -> playPauseRadio()
            R.id.stop -> stopRadio()
            R.id.history -> showHistory()
        }
    }

    /**
     * This method open youtube app and search for track.
     * @param trackName Name of track to find.
     * */
    override fun onTrackClicked(trackName: String) {
        val intent = Intent(Intent.ACTION_SEARCH)
        intent.setPackage("com.google.android.youtube")
        intent.putExtra("query", trackName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun playPauseRadio() {
        vibrate(act, 20)
        if (act.viewModel.mService != null) {
            if (act.viewModel.mService!!.mPlayer.playWhenReady) {
                if (act.viewModel.mService!!.mPlayer.playWhenReady) {
                    play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
                } else play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_pause_24dp))
                when (act.viewModel.mService!!.mPlayer.playWhenReady) {
                    true -> act.viewModel.mService!!.mPlayer.playWhenReady = false
                    else -> act.viewModel.mService!!.mPlayer.playWhenReady = true
                }
            }
        }
    }

    private fun stopRadio() {
        vibrate(context!!, 20)
        act.viewModel.setDefaultGradientPalette()
        if (act.viewModel.mService != null) {
            act.viewModel.hideLoadingAnimation(spinningLoadingScreen)
            station_logo_civ_radio.setImageResource(R.drawable.web_hi_res_512)
            track_title.text = ""
            track_artist.text = ""
            if (act.viewModel.mService!!.mPlayer.playWhenReady) {
                act.pager.currentItem = 1
                if (act.viewModel.mService!!.mPlayer.playWhenReady) {
                    play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
                    act.viewModel.mService!!.mPlayer.stop()
                }
                act.applicationContext.unbindService(act.viewModel.mConnection)
                act.viewModel.mBound = false
            }
        }
    }

    private fun showHistory() {
        if (act.viewModel.mTracks.value?.size == 0) {
            Snackbar.make(act.navigation_view, "Track history is empty", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(act.resources.getColor(R.color.colorLightBlue))
                .show()
            return
        }
        val showHistory = Intent(act, HistoryActivity::class.java)
        val tracks = act.viewModel.mTracks.value
        val color = act.viewModel.mGradientPalette.value
        showHistory.putExtra("tracks", tracks)
        showHistory.putExtra("color", color)
        startActivity(showHistory)
    }

    private fun setSlider() {
        act.viewModel.mAllStations.observe(this, Observer {
            ArrayList(it.filter { it.getNew() }).let { newStations ->
                val adapter = SliderAdapter(this, newStations)
                adapter.initializeImages()
                fragment_radio_slider_vp.baseShadow = 0f
                fragment_radio_slider_vp.minShadow = 0f
            }
        })
    }
}