package pl.qwisdom.simpleradio.view.adapter

import android.support.design.card.MaterialCardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.track_item.view.*
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.listener.TrackClickedListener
import pl.qwisdom.simpleradio.view.model.Track

class TrackListAdapter(
    val tracks: ArrayList<Track>,
    val listener: TrackClickedListener
): RecyclerView.Adapter<TrackListAdapter.TrackHolder>() {

    class TrackHolder(
        val itemView: View,
        val card: MaterialCardView = itemView.card,
        val time: TextView = itemView.time,
        val artist: TextView = itemView.artist,
        val title: TextView = itemView.title
        ) : RecyclerView.ViewHolder(itemView) {

        fun setViews(track: Track) {
            time.text = track.time
            artist.text = track.artist
            title.text = track.title
        }

        fun bind(track: Track, listener: TrackClickedListener) {
            card.setOnClickListener {
                listener.onTrackClicked(track.artist + " " + track.title)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): TrackHolder {
        return TrackHolder(
            itemView = LayoutInflater.from(p0.context).inflate(R.layout.track_item, p0, false)
        )
    }

    override fun getItemCount(): Int = tracks.size

    override fun onBindViewHolder(p0: TrackHolder, p1: Int) {
        p0.setViews(
            track = tracks[p1]
        )
        p0.bind(tracks[p1], listener)
    }
}