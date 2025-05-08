package pion.tech.pionbase.framework.presentation.home

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import pion.tech.pionbase.databinding.FragmentHomeBinding
import pion.tech.pionbase.framework.presentation.common.BaseFragment

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(
    FragmentHomeBinding::inflate,
    HomeViewModel::class.java
) {

    override fun init(view: View) {
        backEvent()
        intiView()
        doneEvent()
    }

    override fun subscribeObserver(view: View) {

    }

}
