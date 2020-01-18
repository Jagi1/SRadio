package pl.sbandurski.simpleradio.view.view.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import pl.sbandurski.simpleradio.view.view.fragment.introfragment.Page1
import pl.sbandurski.simpleradio.view.view.fragment.introfragment.Page2
import pl.sbandurski.simpleradio.view.view.fragment.introfragment.Page3
import pl.sbandurski.simpleradio.view.view.fragment.introfragment.Page4

class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(Page1.newInstance())
        addSlide(Page2.newInstance())
        addSlide(Page3.newInstance())
        addSlide(Page4.newInstance())
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}
