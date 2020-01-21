package pl.sbandurski.simpleradio.view.model

import android.os.Parcel
import android.os.Parcelable

data class SearchFilter(
    val name : String?,
    val country : String?,
    val genre : String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeString(name)
            writeString(country)
            writeString(genre)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SearchFilter> {
        override fun createFromParcel(parcel: Parcel): SearchFilter {
            return SearchFilter(parcel)
        }

        override fun newArray(size: Int): Array<SearchFilter?> {
            return arrayOfNulls(size)
        }
    }
}