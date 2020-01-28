package pl.sbandurski.simpleradio.view.view.activity

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_filter.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.model.GradientPalette
import pl.sbandurski.simpleradio.view.model.SearchFilter
import pl.sbandurski.simpleradio.view.util.Codes

class FilterActivity : AppCompatActivity() {

    var mCountries : Array<String>? = null
    var mGenres : Array<String>? = null
    lateinit var mGradientPalette : GradientPalette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val extras = intent
        mCountries = extras.getStringArrayExtra("countries")
        mGenres = extras.getStringArrayExtra("genres")
        mGradientPalette = extras.getParcelableExtra("color")

        mGradientPalette.lightVibrantSwatch?.let {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled), // enabled
                intArrayOf(-android.R.attr.state_enabled), // disabled
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_pressed)  // pressed
            )
            val colors = intArrayOf(it, it, it, it)
            filter_button.setBackgroundColor(it)
            search_name_container.hintTextColor = ColorStateList(states, colors)
            search_country_container.hintTextColor = ColorStateList(states, colors)
            search_genere_container.hintTextColor = ColorStateList(states, colors)
            search_name_container.defaultHintTextColor = ColorStateList(states, colors)
            search_country_container.defaultHintTextColor = ColorStateList(states, colors)
            search_genere_container.defaultHintTextColor = ColorStateList(states, colors)
        }

        setCountryAutoComplete(this)
        setGenreAutoComplete(this)

        search_country_content.setOnFocusChangeListener { _, hasFocus ->
            run {
                if (hasFocus) {
                    search_country_content.showDropDown()
                }
            }
        }

        search_genere_content.setOnFocusChangeListener { _, hasFocus ->
            run {
                if (hasFocus) {
                    search_genere_content.showDropDown()
                }
            }
        }

        filter_button.setOnClickListener {
            val result = Intent()
            val filter = SearchFilter(
                name = search_name_content.text.toString(),
                country = search_country_content.text.toString(),
                genre = search_genere_content.text.toString()
            )
            result.putExtra("filter", filter)
            setResult(Codes.SEARCH_OK, result)
            finish()
        }

        search_close.setOnClickListener {
            setResult(Codes.SEARCH_CANCEL)
            finish()
        }

    }

    private fun setCountryAutoComplete(activity : FilterActivity) {
        val countries = when (mCountries.isNullOrEmpty()) {
            true -> activity.resources.getStringArray(R.array.countries)
            else -> mCountries
        }
        val countriesAdapter = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, countries)
        search_country_content.setAdapter(countriesAdapter)
    }

    private fun setGenreAutoComplete(activity : FilterActivity) {
        val genres = when (mGenres.isNullOrEmpty()) {
            true -> activity.resources.getStringArray(R.array.types)
            else -> mGenres
        }
        val genresAdapter = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, genres)
        search_genere_content.setAdapter(genresAdapter)
    }
}
