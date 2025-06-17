package com.example.skripsta.data

import android.os.Parcel
import android.os.Parcelable

data class Reminder(
    val id: Int,
    val hour: Int,
    val minute: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Reminder> {
        override fun createFromParcel(parcel: Parcel): Reminder = Reminder(parcel)
        override fun newArray(size: Int): Array<Reminder?> = arrayOfNulls(size)
    }

    val message: String
        get() = "Don't forget to fill ur mood today"
}