package pion.tech.pionbase.framework.presentation.selecttool

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import pion.tech.pionbase.databinding.FragmentSelectToolBinding
import pion.tech.pionbase.framework.presentation.common.BaseFragment

@AndroidEntryPoint
class SelectToolFragment : BaseFragment<FragmentSelectToolBinding, SelectToolViewModel>(
    FragmentSelectToolBinding::inflate,
    SelectToolViewModel::class.java
) {
    override fun init(view: View) {
        addTattooEvent()
    }

    override fun subscribeObserver(view: View) {

    }
}