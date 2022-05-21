package com.under.tptr.recyclerCurrent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.under.tptr.R
import com.under.tptr.model.PackageClient

class PackageCurrentAdapter: RecyclerView.Adapter<PackageCurrentView>() {

    private val packages = ArrayList<PackageClient>()
    var listener : Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageCurrentView {
        var inflater = LayoutInflater.from(parent.context)
        val row = inflater.inflate(R.layout.guide_current_row, parent, false)
        val packageCurrentView = PackageCurrentView(row)
        packageCurrentView.listener = listener
        return packageCurrentView
    }

    override fun onBindViewHolder(holder: PackageCurrentView, position: Int) {
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

    fun deletePack(pack: PackageClient?) {
        val index = packages.indexOf(pack)
        packages.removeAt(index)
        notifyItemRemoved(index)
        listener?.onRemovePackListener(pack)
        if(packages.size == 0)listener?.onZeroPackagesListener()
    }

    fun clean(){
        packages.clear()
        notifyDataSetChanged()
    }

    interface Listener{
        fun onZeroPackagesListener()
        fun onAddPackListener()
        fun onRemovePackListener(pack: PackageClient?)
        fun onNext(pack: PackageClient?)
    }
}