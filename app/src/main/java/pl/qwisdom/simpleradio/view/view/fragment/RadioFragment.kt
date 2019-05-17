package pl.qwisdom.simpleradio.view.view.fragment

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.common.io.Resources.getResource
import kotlinx.android.synthetic.main.fragment_radio.*
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.adapter.TrackListAdapter
import pl.qwisdom.simpleradio.view.listener.TrackClickedListener
import pl.qwisdom.simpleradio.view.util.vibrate
import pl.qwisdom.simpleradio.view.view.activity.MainActivity

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

        prepareTracks()

        // Update current track data
        act.viewModel.mCurrentTrackData.observe(this, Observer { track ->
            track_artist.text = track!!.artist
            track_title.text = track.title
        })

        // Update current station data
        act.viewModel.mCurrentStation.observe(this, Observer { station ->
            station_name.text = station?.getName()
            station_logo_civ_radio.setImageBitmap(station?.getImage())
        })

        if (act.viewModel.mService != null) {
            if (act.viewModel.mService!!.mStarted) play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_pause_24dp))
            else play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.play_pause -> playPauseRadio()
            R.id.stop -> stopRadio()
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

    /**
     * Prepare RecyclerView with tracks.
     * */
    private fun prepareTracks() {
        track_list.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        act.mAdapter = TrackListAdapter(tracks = act.viewModel.mTracks.value!!, listener = this)
        track_list.adapter = act.mAdapter
    }

    private fun playPauseRadio() {
        vibrate(act, 20)
        if (act.viewModel.mService != null) {
            if (act.viewModel.mService!!.mPrepared) {
                if (act.viewModel.mService!!.mPlayer.isPlaying) {
                    play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
                } else play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_pause_24dp))
                when (act.viewModel.mService!!.mPlayer.isPlaying) {
                    true -> act.viewModel.mService!!.mPlayer.pause()
                    else -> act.viewModel.mService!!.mPlayer.start()
                }
            }
        }
    }

    private fun stopRadio() {
        vibrate(context!!, 20)
        if (act.viewModel.mService != null) {
            station_logo_civ_radio.setImageResource(R.drawable.web_hi_res_512)
            station_name.text = ""
            track_title.text = ""
            track_artist.text = ""
            if (act.viewModel.mService!!.mPrepared) {
                if (act.viewModel.mService!!.mPlayer.isPlaying) {
                    play_pause.setImageDrawable(act.getDrawable(R.drawable.ic_play_arrow_24dp))
                    act.viewModel.mService!!.mPlayer.stop()
                }
                act.applicationContext.unbindService(act.viewModel.mConnection)
                act.viewModel.mBound = false
            }
        }
    }
}