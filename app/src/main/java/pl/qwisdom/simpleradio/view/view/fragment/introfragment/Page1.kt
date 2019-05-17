package pl.qwisdom.simpleradio.view.view.fragment.introfragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.qwisdom.simpleradio.R

class Page1: Fragment() {

    companion object {
        fun newInstance(): Page1 = Page1()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_page_1, container, false)

}