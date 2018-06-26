package drawables.dom

import android.view.Gravity
import com.intellij.util.ui.UIUtil
import drawables.ItemDrawableInflater
import drawables.Utils
import org.w3c.dom.Element
import java.awt.image.BufferedImage

class ScaleDrawable : Drawable() {

    companion object {
        private const val SCALE_HEIGHT = "android:scaleHeight"
        private const val SCALE_WIDTH = "android:scaleWidth"
        private const val SCALE_GRAVITY = "android:scaleGravity"
    }

    private var drawable: Drawable? = null
    private var scaleWidth = 1F
    private var scaleHeight = 1F
    private var gravity = Gravity.LEFT or Gravity.TOP

    override fun inflate(element: Element) {
        super.inflate(element)
        drawable = ItemDrawableInflater.getDrawableWithInflate(element)

        scaleHeight = Utils.parseAttributeAsPercent(element.getAttribute(SCALE_HEIGHT), scaleHeight)
        scaleWidth = Utils.parseAttributeAsPercent(element.getAttribute(SCALE_WIDTH), scaleWidth)
        gravity = Utils.parseAttributeAsGravity(element.getAttribute(SCALE_GRAVITY), gravity)
    }

    override fun draw(image: BufferedImage) {
        super.draw(image)

        drawable?.also { drawable ->
            val width = Math.round(image.width * scaleWidth)
            val height = Math.round(image.height * scaleHeight)
            if (width <= 0 || height <= 0) {
                return
            }

            resolveGravity(image.height, height, image.width, width).also { resolvedGravity ->
                UIUtil.createImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB).also { scaledImage ->
                    drawable.draw(scaledImage)
                    image.graphics.apply {
                        drawImage(scaledImage, resolvedGravity.first, resolvedGravity.second, width, height, null)
                        dispose()
                    }
                }
            }
        }
    }

    private fun resolveGravity(parentHeight: Int, containerHeight: Int, parentWidth: Int, containerWidth: Int): Pair<Int, Int> {
        var xValue =
                when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.LEFT, Gravity.START -> 0
                    Gravity.CENTER_HORIZONTAL -> (parentWidth - containerWidth) / 2
                    Gravity.RIGHT, Gravity.END -> parentWidth - containerWidth
                    else -> 0
                }
        var yValue =
                when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                    Gravity.TOP -> 0
                    Gravity.CENTER_VERTICAL -> (parentHeight - containerHeight) / 2
                    Gravity.BOTTOM -> parentHeight - containerHeight
                    else -> 0
                }

        if (xValue < 0) xValue = 0
        if (yValue < 0) yValue = 0

        return Pair(xValue, yValue)
    }
}