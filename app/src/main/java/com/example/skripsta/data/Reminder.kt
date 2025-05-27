package com.example.skripsta.data

import android.os.Parcel
import android.os.Parcelable

data class Reminder(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: List<Int>
) : Parcelable {
    val message: String
        get() = "Don't forget to fill ur mood today"

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        hour = parcel.readInt(),
        minute = parcel.readInt(),
        days = parcel.createIntArray()?.toList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
        parcel.writeIntArray(days.toIntArray())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Reminder> {
        override fun createFromParcel(parcel: Parcel): Reminder {
            return Reminder(parcel)
        }

        override fun newArray(size: Int): Array<Reminder?> {
            return arrayOfNulls(size)
        }
    }
}