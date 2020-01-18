package pl.sbandurski.simpleradio.view.view.fragment.introfragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.sbandurski.simpleradio.R

class Page2: Fragment() {

    companion object {
        fun newInstance(): Page2 = Page2()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_page_2, container, false)

}