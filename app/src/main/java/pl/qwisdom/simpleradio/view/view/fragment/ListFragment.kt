package pl.qwisdom.simpleradio.view.view.fragment

import android.arch.lifecycle.Observer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_list.*
import pl.qwisdom.simpleradio.R
import pl.qwisdom.simpleradio.view.view.activity.MainActivity
import pl.qwisdom.simpleradio.view.adapter.MyRecyclerViewAdapter
import pl.qwisdom.simpleradio.view.model.Station
import pl.qwisdom.simpleradio.view.util.hideSoftKeyboard
import java.lang.Exception

class ListFragment : Fragment(), View.OnClickListener {

    private lateinit var mAdapter: MyRecyclerViewAdapter
    private lateinit var act: MainActivity
    private lateinit var controller: LayoutAnimationController

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_list, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = AnimationUtils.loadLayoutAnimation(context, R.anim.animation_layout_list)
        recycler_view.layoutAnimation = controller
        act = activity as MainActivity
        filter_fab.setOnClickListener(this)
        filter_btn.setOnClickListener(this)
        name_edit.setOnClickListener(act)
        name_check.setOnClickListener(act)
        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        act.viewModel.resources = resources
//        act.viewModel.fetchStations(this)
        act.viewModel.mStations.observe(this, Observer { stations ->
            recycler_view.scheduleLayoutAnimation()
            mAdapter = MyRecyclerViewAdapter(stations!!, act)
            recycler_view.adapter = mAdapter
            if (stations.isEmpty() && nothing_found.visibility == View.INVISIBLE) {
                nothing_found.visibility = View.GONE
            }
            else if (stations.isEmpty() && nothing_found.visibility == View.GONE) {
                nothing_found.visibility = View.VISIBLE
            } else {
                val storage = FirebaseStorage.getInstance()
                var i = 0
                stations.forEach { station ->
                    storage.reference.child("/stations/${station.getLogoUrl()}/${station.getLogoUrl()}.bmp")
                        .getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                            station.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                            mAdapter.notifyDataSetChanged()
                        }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.filter_btn -> act.viewModel.fetchStations(this).also { hideSoftKeyboard(act) }.also {
                filter_card.visibility = View.GONE
                if (nothing_found.visibility == View.VISIBLE) {
                    nothing_found.visibility = View.GONE
                }
            }
            R.id.filter_fab -> {
                filter_card.visibility = when (filter_card.visibility) {
                    View.VISIBLE -> View.GONE
                    else -> View.VISIBLE
                }
            }
        }
    }
}