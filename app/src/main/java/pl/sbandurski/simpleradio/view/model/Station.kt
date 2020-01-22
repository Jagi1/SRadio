package pl.sbandurski.simpleradio.view.model

import android.graphics.Bitmap

data class Station(
    private val name : String,
    private var image : Bitmap? = null,
    private val url : String,
    private val type : String,
    private val bitrate : String,
    private val drawableID : String,
    private val logoUrl : String,
    private val country : String,
    private val new : Boolean
) {

    fun getName(): String = name

    fun setImage(image: Bitmap) {
        this.image = image
    }

    fun getImage() : Bitmap? = image

    fun getUrl() : String = url

    fun getDrawableID() : String = drawableID

    fun getType() : String = type

    fun getBitrate() : String = bitrate

    fun getLogoUrl() : String = logoUrl

    fun getCountry() : String = country

    fun getNew() : Boolean = new
}