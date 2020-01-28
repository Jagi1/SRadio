package pl.sbandurski.simpleradio.view.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

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
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

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
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeParcelable(image, flags)
        parcel.writeString(url)
        parcel.writeString(type)
        parcel.writeString(bitrate)
        parcel.writeString(drawableID)
        parcel.writeString(logoUrl)
        parcel.writeString(country)
        parcel.writeByte(if (new) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}