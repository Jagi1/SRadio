package pl.sbandurski.simpleradio.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.model.MyViewHolder
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.util.OnItemClickListener

class MyRecyclerViewAdapter(
    private val list: ArrayList<Station>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        return MyViewHolder(
            itemView = LayoutInflater.from(p0.context).inflate(R.layout.list_item, p0, false),
            url = list[p1].getUrl()
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        p0.setViews(
            station = list[p1]
        )
        p0.bind(list[p1], listener)
    }
}