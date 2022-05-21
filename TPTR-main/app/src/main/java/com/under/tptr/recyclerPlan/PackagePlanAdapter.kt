package com.under.tptr.recyclerPlan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.under.tptr.R
import com.under.tptr.model.PackageClient

class PackagePlanAdapter: RecyclerView.Adapter<PackagePlanView>(), PackagePlanView.OnPackageRemove {

    private val packages = ArrayList<PackageClient>()
    var listener : Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackagePlanView {
        var inflater = LayoutInflater.from(parent.context)
        val row = inflater.inflate(R.layout.guide_plan_row, parent, false)
        val packagePlanView = PackagePlanView(row)
        packagePlanView.listener = this
        return packagePlanView
    }

    override fun onBindViewHolder(holder: PackagePlanView, position: Int) {
        val pack = packages[position]
        holder.pack = pack
        holder.addressText.text = pack.direccion
        holder.guideNumberText.text = pack.guia
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    fun addPack(pack:PackageClient){
        packages.add(pack)
        notifyItemInserted(packages.size-1)
        listener?.onAddPackListener()
    }

    override fun onRemove(pack: PackageClient?) {
        val index = packages.indexOf(pack)
        packages.removeAt(index)
        notifyItemRemoved(index)
        if(packages.size == 0)listener?.onZeroPackagesListener()
        listener?.onRemovePackListener(pack)
    }

    interface Listener{
        fun onZeroPackagesListener()
        fun onAddPackListener()
        fun onRemovePackListener(pack: PackageClient?)
    }
}