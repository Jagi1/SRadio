package pl.sbandurski.simpleradio.view.model

import android.os.Parcel
import android.os.Parcelable

data class GradientPalette(
    val darkVibrant : Int? = null,
    val darkMuted : Int? = null,
    val muted : Int? = null,
    val lightMuted : Int? = null,
    val dominantSwatch : Int? = null,
    val vibrantSwatch : Int? = null,
    val lightVibrantSwatch : Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(darkVibrant)
        parcel.writeValue(darkMuted)
        parcel.writeValue(muted)
        parcel.writeValue(lightMuted)
        parcel.writeValue(dominantSwatch)
        parcel.writeValue(vibrantSwatch)
        parcel.writeValue(lightVibrantSwatch)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GradientPalette> {
        override fun createFromParcel(parcel: Parcel): GradientPalette = GradientPalette(parcel)
        override fun newArray(size: Int): Array<GradientPalette?> = arrayOfNulls(size)
    }
}