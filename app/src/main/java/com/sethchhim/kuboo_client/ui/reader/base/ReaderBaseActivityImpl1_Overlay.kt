package com.sethchhim.kuboo_client.ui.reader.base

import android.annotation.SuppressLint
import com.sethchhim.kuboo_client.Constants
import com.sethchhim.kuboo_client.Extensions.fadeGone
import com.sethchhim.kuboo_client.Extensions.fadeVisible
import com.sethchhim.kuboo_client.Extensions.gone
import com.sethchhim.kuboo_client.Extensions.visible

@SuppressLint("Registered")
open class ReaderBaseActivityImpl1_Overlay : ReaderBaseActivityImpl0_View() {

    internal fun showOverlay(isFadeEnabled: Boolean = true) {
        overlayImageView.visible()
        overlayTextView1.visible()
        overlayLayout.apply {
            bringToFront()
            when (isFadeEnabled) {
                true -> fadeVisible()
                false -> visible()
            }
        }

        showStatusBar()
        showReaderToolBar()

        intent.putExtra(Constants.ARG_OVERLAY, true)
    }

    protected fun restoreOverlay() {
        //show overlay if activity was recreated while overlay is open
        intent.getBooleanExtra(Constants.ARG_OVERLAY, false).apply {
            if (this) showOverlay(isFadeEnabled = false)
        }
    }

    protected fun hideOverlayHardwareNavigation() = if (isOverlayShown()) hideOverlay() else showExitTransition()

    protected fun hideOverlaySoftwareNavigation() {
        if (isOverlayShown()) hideOverlay()
        showExitTransition()
    }

    protected fun hideOverlay() {
        overlayImageView.gone()
        overlayTextView1.gone()
        overlayLayout.fadeGone(200)

        hideStatusBar()
        hideReaderToolbar()

        intent.putExtra(Constants.ARG_OVERLAY, false)
    }

    protected fun setOverlayPageNumberText(progress: Int) {
        overlayPageNumberTextView.text = (progress + 1).toString()
    }

    protected fun setOverlayPosition(position: Int) {
        overlaySeekBar.progress = position
        overlayPageNumberTextView.text = (position + 1).toString()
    }

    private fun isOverlayShown() = overlayLayout.isShown

}