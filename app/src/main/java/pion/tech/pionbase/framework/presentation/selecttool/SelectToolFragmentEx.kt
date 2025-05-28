package pion.tech.pionbase.framework.presentation.selecttool

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.addCallback
import androidx.core.graphics.scale
import androidx.navigation.fragment.findNavController
import pion.tech.pionbase.R
import pion.tech.pionbase.util.setPreventDoubleClickScaleView

fun SelectToolFragment.addTattooEvent() {
    binding.btnAddTattoo.setPreventDoubleClickScaleView {
        binding.selectToolView.setBitmapSticker(createTattooBitmap())
    }
}

fun SelectToolFragment.createTattooBitmap(): Bitmap {
    return BitmapFactory.decodeResource(requireContext().resources, R.drawable.anime9)
}

fun SelectToolFragment.backEvent() {
    activity?.onBackPressedDispatcher?.addCallback(this, true) {
        onBackPressed()
    }
}

fun SelectToolFragment.onBackPressed() {
    findNavController().navigateUp()
}

fun SelectToolFragment.setSizeStickerEvent() {
    binding.seekBarSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                binding.selectToolView.setSizeSticker(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    })
}

fun SelectToolFragment.bitmapBackground(
    resource: Int,
    width: Int,
    height: Int,
    hasToExif: Boolean
): Bitmap {
    val result = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    var bitmap = BitmapFactory.decodeResource(resources, resource)

    if (hasToExif) {
        val matrix = Matrix()
        matrix.postRotate(90f)

        bitmap = createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    val scaleBitmap = bitmap.scale(width, height, true)

    canvas.drawBitmap(scaleBitmap, 0f, 0f, null)

    return result
}