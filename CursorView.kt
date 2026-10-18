package pk.cinejoy.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** A simple on-screen pointer drawn above the WebView so a TV remote can "click" web pages. */
class CursorView(context: Context) : View(context) {

    var cursorX = 0f
    var cursorY = 0f

    private val d = resources.displayMetrics.density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF95FF50.toInt(); style = Paint.Style.FILL
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f * d
    }
    private val arrow = Path()

    override fun onDraw(canvas: Canvas) {
        val s = 22f * d
        arrow.reset()
        arrow.moveTo(cursorX, cursorY)
        arrow.lineTo(cursorX, cursorY + s)
        arrow.lineTo(cursorX + s * 0.30f, cursorY + s * 0.75f)
        arrow.lineTo(cursorX + s * 0.55f, cursorY + s * 1.15f)
        arrow.lineTo(cursorX + s * 0.72f, cursorY + s * 1.05f)
        arrow.lineTo(cursorX + s * 0.48f, cursorY + s * 0.68f)
        arrow.lineTo(cursorX + s * 0.78f, cursorY + s * 0.68f)
        arrow.close()
        canvas.drawPath(arrow, fill)
        canvas.drawPath(arrow, stroke)
    }
}
