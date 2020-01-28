package pl.sbandurski.simpleradio.view.model

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
import pl.sbandurski.simpleradio.view.util.OnItemClickListener

data class MyViewHolder(
    private val itemView: View,
    private val root: ConstraintLayout = itemView.list_item_root,
    private val name: TextView = itemView.title,
    private val image: ImageView = itemView.station_logo_civ,
    private var url: String,
    private val type: TextView = itemView.type,
    private val bitrate: TextView = itemView.bitrate
) : RecyclerView.ViewHolder(itemView) {

    fun setViews(station: Station) {
        bitrate.text = station.getBitrate()
        name.text = station.getName()
        type.text = station.getType()
        image.setImageBitmap(station.getImage())
        url = station.getUrl()
    }

    fun bind(station: Station, listener: OnItemClickListener) {
        root.setOnClickListener {
            listener.onItemClick(station)
        }
    }
}