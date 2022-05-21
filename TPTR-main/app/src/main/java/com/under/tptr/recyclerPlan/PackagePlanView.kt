package com.under.tptr.recyclerPlan

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.under.tptr.R
import com.under.tptr.model.PackageClient

class PackagePlanView(itemView: View): RecyclerView.ViewHolder(itemView){

    var pack: PackageClient? = null
    var listener: OnPackageRemove? = null

    var guideNumberText: TextView = itemView.findViewById(R.id.guideNumberText)
    var addressText: TextView = itemView.findViewById(R.id.addressText)
    var removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

    init {
        removeButton.setOnClickListener{
            listener?.onRemove(pack)
        }
    }

    interface OnPackageRemove{ fun onRemove(pack: PackageClient?) }
}