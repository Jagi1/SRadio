package pl.sbandurski.simpleradio.view.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_history.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.adapter.TrackListAdapter
import pl.sbandurski.simpleradio.view.listener.TrackClickedListener
import pl.sbandurski.simpleradio.view.model.GradientPalette
import pl.sbandurski.simpleradio.view.model.Track

class HistoryActivity : AppCompatActivity(), TrackClickedListener {

    var mGradientPalette : GradientPalette? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        intent?.extras?.let {
            mGradientPalette = it.getParcelable("color")
        }

        val tracks = intent?.extras?.getParcelableArrayList<Track>("tracks") ?: ArrayList()

        history_recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = TrackListAdapter(tracks, this, mGradientPalette)
        history_recycler_view.adapter = adapter

        history_close.setOnClickListener {
            finish()
        }
    }

    override fun onTrackClicked(trackName: String) {
        val intent = Intent(Intent.ACTION_SEARCH)
        intent.setPackage("com.google.android.youtube")
        intent.putExtra("query", trackName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
