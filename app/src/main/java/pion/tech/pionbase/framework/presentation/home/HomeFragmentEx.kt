package pion.tech.pionbase.framework.presentation.home

import android.graphics.BitmapFactory
import android.util.Log
import pion.tech.pionbase.R
import pion.tech.pionbase.util.setPreventDoubleClickScaleView

fun HomeFragment.backEvent() {
//    onSystemBack {
//        onBackPressed()
//    }
//    binding.btnBack.setPreventDoubleClickScaleView {
//        onBackPressed()
//    }
}

fun HomeFragment.intiView() {
    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.porsche)
    binding.perspectiveView.setBitmap(bitmap)
}

fun HomeFragment.doneEvent() {
    binding.ivDone.setPreventDoubleClickScaleView {
        //lay vi tri cua 4 diem de tao thanh hinh bo ngoai va bitmap
        val result = binding.perspectiveView.getWarpedBitmapAndPoints()
        binding.ivResult.setImageBitmap(result.first)
        Log.d("CHECKPOSITION", "${result.second}")
    }
}