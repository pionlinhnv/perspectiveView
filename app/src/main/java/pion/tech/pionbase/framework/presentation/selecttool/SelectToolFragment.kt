package pion.tech.pionbase.framework.presentation.selecttool

import android.graphics.Bitmap
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import dagger.hilt.android.AndroidEntryPoint
import pion.tech.pionbase.R
import pion.tech.pionbase.customview.SelectToolView
import pion.tech.pionbase.databinding.FragmentSelectToolBinding
import pion.tech.pionbase.framework.presentation.common.BaseFragment
import pion.tech.pionbase.util.displayToast
import pion.tech.pionbase.util.gone

@AndroidEntryPoint
class SelectToolFragment : BaseFragment<FragmentSelectToolBinding, SelectToolViewModel>(
    FragmentSelectToolBinding::inflate,
    SelectToolViewModel::class.java
), SelectToolView.Listener {
    override fun init(view: View) {
        backEvent()
        addTattooEvent()
        binding.selectToolView.setListener(this)
        binding.ivMain.post {
            val bitmapBg = bitmapBackground(
                resource = R.drawable.girl,
                width = binding.ivMain.width,
                height = binding.ivMain.height,
                hasToExif = false
            )

            binding.selectToolView.setBitmapBg(bitmapBg)
        }
        setSizeStickerEvent()
        setStickerPositionEvent()
        setStickerBlurEvent()
    }

    override fun subscribeObserver(view: View) {

    }

    override fun onDrawSelect(bitmap: Bitmap) {
        binding.ivPreview.setImageBitmap(bitmap)
    }

    override fun onPreviewChange(isFirstQuadrant: Boolean) {
        val layoutParams = binding.ivPreview.layoutParams as ConstraintLayout.LayoutParams
        if (!isFirstQuadrant) {
            // Gắn góc trên bên trái
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID

            // Xóa ràng buộc end để tránh lỗi
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
        } else {
            // Gắn góc trên bên phải
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

            // Xóa ràng buộc start
            layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
        }

        // Gán lại layoutParams
        binding.ivPreview.layoutParams = layoutParams
    }

    override fun onSelectTooSmall() {
//        displayToast("Too Small")
    }

    override fun onAddSticker() {
        binding.selectToolView.setBitmapSticker(createTattooBitmap())
    }

    override fun onPreviewDone() {
        binding.ivPreview.gone()
    }
}