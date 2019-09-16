package com.webssa.guestbest.ui.document

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.webssa.library.widget.AnimatedOpenerView

internal class FoldableSectionDescriptor(private val vSection: LinearLayout, private val vSectionHeader: AnimatedOpenerView, private val vRowsContainer: ViewGroup) {

    private var isFolded: Boolean = false

    init {
        this.isFolded = vSectionHeader.isOpened

        vRowsContainer.pivotY = 0f

        vSectionHeader.setCallback(object : AnimatedOpenerView.Callback {
            override fun onAnimationStart(openPart: Float) {
                vRowsContainer.visibility = View.VISIBLE
                vRowsContainer.scaleY = openPart
            }

            override fun onAnimationUpdate(openPart: Float) {
                vRowsContainer.scaleY = openPart
            }

            override fun onAnimationEnd(isOpened: Boolean) {
                isFolded = !isOpened
                if (isOpened) {
                    vRowsContainer.visibility = View.VISIBLE
                    vRowsContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    vRowsContainer.visibility = View.GONE
                }

            }
        })

    }

}
