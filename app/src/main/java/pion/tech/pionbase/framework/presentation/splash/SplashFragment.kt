package pion.tech.pionbase.framework.presentation.splash

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import pion.tech.pionbase.databinding.FragmentSplashBinding
import pion.tech.pionbase.framework.presentation.common.BaseFragment


@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>(
    FragmentSplashBinding::inflate,
    SplashViewModel::class.java
) {


    override fun init(view: View) {
        backEvent()
        startAnimation()
    }

    override fun subscribeObserver(view: View) {

    }

}
