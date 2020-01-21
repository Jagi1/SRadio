package pl.sbandurski.simpleradio.view.view.fragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_list.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import pl.sbandurski.simpleradio.view.adapter.MyRecyclerViewAdapter
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.util.Codes
import pl.sbandurski.simpleradio.view.util.hideSoftKeyboard
import pl.sbandurski.simpleradio.view.view.activity.FilterActivity

class ListFragment : Fragment(), View.OnClickListener {

    private lateinit var mAdapter: MyRecyclerViewAdapter
    private lateinit var act: MainActivity
    private lateinit var controller: LayoutAnimationController

    companion object {
        fun newInstance(): ListFragment = ListFragment()
        const val FILTER_REQUEST_CODE = 100
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
        recycler_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                act.viewModel.changeFilterAlpha(filter_card_image, filter_fab, newState)
            }
        })
        act = activity as MainActivity
        filter_fab.setOnClickListener(this)
        recycler_view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        act.viewModel.resources = resources
//        act.viewModel.fetchStations(this)
        act.viewModel.mStations.observe(this, Observer { stations ->
            recycler_view.scheduleLayoutAnimation()
            mAdapter = MyRecyclerViewAdapter(stations!!, act)
            recycler_view.adapter = mAdapter
            if (stations.isEmpty()) {
                Snackbar.make(act.navigation_view, "No stations found..", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(resources.getColor(R.color.colorLightBlue))
                    .show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Codes.SEARCH_REQUEST -> {
                when (resultCode) {
                    Codes.SEARCH_OK -> {
                        val filter = data?.getParcelableExtra("filter") as SearchFilter
                        act.viewModel.fetchStations(filter)
                    }
                    Codes.SEARCH_CANCEL -> {}
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.filter_fab -> {
                val filterStations = Intent(context, FilterActivity::class.java)
                filterStations.putExtra("countries", act.viewModel.mCountries)
                filterStations.putExtra("genres", act.viewModel.mGenres)
                startActivityForResult(filterStations, FILTER_REQUEST_CODE)
            }
        }
    }
}