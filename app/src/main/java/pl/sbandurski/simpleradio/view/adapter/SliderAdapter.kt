package pl.sbandurski.simpleradio.view.adapter

import android.graphics.BitmapFactory
import android.view.View
import com.github.islamkhsh.CardSliderAdapter
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_radio.*
import kotlinx.android.synthetic.main.slider_item.view.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import pl.sbandurski.simpleradio.view.view.fragment.RadioFragment

class SliderAdapter(val context : RadioFragment, val stations : ArrayList<Station>) : CardSliderAdapter<Station>(stations) {

    override fun bindView(position: Int, itemContentView: View, item: Station?) {
        itemContentView.slider_item_content.setImageBitmap(item?.getImage())
        itemContentView.slider_item_name.text = item?.getName()
        itemContentView.slider_item_content.setOnClickListener {
            item?.let {
                (context.activity as MainActivity).onItemClick(item)
            }
        }
    }

    override fun getItemContentLayout(position: Int): Int = R.layout.slider_item

    fun initializeImages() {
        val storage = FirebaseStorage.getInstance()
        stations.forEach { station ->
            storage.reference.child("/stations/${station.getLogoUrl()}/${station.getLogoUrl()}.bmp")
                .getBytes(1024 * 1024)
                .addOnSuccessListener { bytes ->
                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    station.setImage(image)
                    if (stations.indexOf(station) >= 2) {
                        context.fragment_radio_slider_vp.adapter = this
                    }
                }
        }
    }
}