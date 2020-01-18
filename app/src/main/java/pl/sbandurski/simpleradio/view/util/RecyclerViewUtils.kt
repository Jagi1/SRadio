package pl.sbandurski.simpleradio.view.util

import android.support.v7.widget.RecyclerView
import pl.sbandurski.simpleradio.view.model.Station

interface OnItemClickListener {
    fun onItemClick(station: Station)
}

class CustomItemAnimator: RecyclerView.ItemAnimator() {
    override fun isRunning(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun animatePersistence(
        p0: RecyclerView.ViewHolder,
        p1: ItemHolderInfo,
        p2: ItemHolderInfo
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runPendingAnimations() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun endAnimation(p0: RecyclerView.ViewHolder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun animateDisappearance(
        p0: RecyclerView.ViewHolder,
        p1: ItemHolderInfo,
        p2: ItemHolderInfo?
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun animateChange(
        p0: RecyclerView.ViewHolder,
        p1: RecyclerView.ViewHolder,
        p2: ItemHolderInfo,
        p3: ItemHolderInfo
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun animateAppearance(
        p0: RecyclerView.ViewHolder,
        p1: ItemHolderInfo?,
        p2: ItemHolderInfo
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun endAnimations() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}