package pl.sbandurski.simpleradio.view.view.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_prepare.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.model.StationsCache

class PrepareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare)
        fetchAllStations()
    }

    private fun fetchAllStations() {
        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.9).toInt()
        val height = (metrics.heightPixels * 0.52).toInt()

        val filter = SearchFilter("", "", "")
        val list = ArrayList<Station>()
        val database = FirebaseFirestore.getInstance()
        database.collection("stations")
            .get()
            .addOnSuccessListener { stations ->
                for (station in stations) {
                    if (filter.name?.isNotEmpty() == true) {
                        if (!station.data["name"].toString().toLowerCase().contains(
                                filter.name,
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (filter.country?.isNotEmpty() == true) {
                        if (!station.data["country"].toString().toLowerCase().contains(
                                filter.country,
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    if (filter.genre?.isNotEmpty() == true) {
                        if (!station.data["type"].toString().toLowerCase().contains(
                                filter.genre,
                                true
                            )
                        ) {
                            continue
                        }
                    }
                    val id = station.id
                    val isStationNew =
                        if (station.contains("new")) station.data["new"] as Boolean
                        else false
                    val image = BitmapFactory.decodeResource(resources, R.drawable.item_logo)
                    val resizedImage = Bitmap.createScaledBitmap(image, width, width, true)
                    list.add(
                        Station(
                            name = station.data["name"] as String,
                            image = resizedImage,
                            url = station.data["url"] as String,
                            drawableID = id,
                            type = station.data["type"] as String,
                            bitrate = station.data["bitrate"] as String,
                            logoUrl = station.id,
                            country = station.data["country"] as String,
                            new = isStationNew
                        )
                    )
                }
                saveStationsToCache(list)
            }.addOnFailureListener { ex ->
                ex.printStackTrace()
            }
    }

    private fun saveStationsToCache(list : ArrayList<Station>) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 500
            interpolator = LinearOutSlowInInterpolator()
            addUpdateListener {
                prepare_animation.alpha = it.animatedValue as Float
            }
            doOnEnd {
                prepare_animation.visibility = View.GONE
                StationsCache.stations = list
                startMainActivity()
            }
            start()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
