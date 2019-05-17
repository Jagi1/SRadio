package pl.qwisdom.simpleradio.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.model.MyViewHolder
import pl.qwisdom.simpleradio.view.model.Station
import pl.qwisdom.simpleradio.view.util.OnItemClickListener

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