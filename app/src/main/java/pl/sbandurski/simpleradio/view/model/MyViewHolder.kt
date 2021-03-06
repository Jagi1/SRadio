package pl.sbandurski.simpleradio.view.model

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.list_item.view.*
import pl.sbandurski.simpleradio.view.util.OnItemClickListener

data class MyViewHolder(
    private val itemView: View,
    private val card: MaterialCardView = itemView.card,
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
        card.setOnClickListener {
            listener.onItemClick(station)
        }
    }

    fun setColor(color : Int?) {
        color?.let { nonNullColor ->
            val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled), // enabled
                intArrayOf(-android.R.attr.state_enabled), // disabled
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_pressed)  // pressed
            )
            val colors = intArrayOf(nonNullColor, nonNullColor, nonNullColor, nonNullColor)
            card.backgroundTintList = ColorStateList(states, colors)
        }
    }

}