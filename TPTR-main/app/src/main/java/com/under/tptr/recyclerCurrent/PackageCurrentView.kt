package com.under.tptr.recyclerCurrent

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.under.tptr.R
import com.under.tptr.model.PackageClient

class PackageCurrentView(itemView: View): RecyclerView.ViewHolder(itemView) {
    var pack: PackageClient? = null
    var listener: PackageCurrentAdapter.Listener? = null

    var guideNumberText: TextView = itemView.findViewById(R.id.guideTXT)
    var addressText: TextView = itemView.findViewById(R.id.addressTXT)
    var nextButton: ImageButton = itemView.findViewById(R.id.nextButton)

    init {
        nextButton.setOnClickListener{
            listener?.onNext(pack)
        }
    }
}