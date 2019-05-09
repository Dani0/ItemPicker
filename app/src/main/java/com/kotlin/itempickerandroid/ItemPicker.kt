package com.kotlin.itempickerandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes

class ItemPicker<T> : ScrollView {
    private var items: MutableList<T> = mutableListOf()

    private lateinit var views: LinearLayout
    private lateinit var dialogBuilder: AlertDialog.Builder

    private val newCheck = 50
    private val OFF_SET_DEFAULT = 2

    private var offset = OFF_SET_DEFAULT

    private var displayItemCount: Int = 0
    private var selectedIndex = 1
    private var initialY: Int = 0

    private var scrollerTask: Runnable? = null
    private var itemHeight = 0
    private var selectedAreaBorder: IntArray? = null
    private var paint: Paint = Paint()

    private var viewWidth: Int = 0

    private var color: Int = 0
    private var color1: Int = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        color = context.resources.getColor(R.color.colorPrimary)
        color1 = context.resources.getColor(R.color.colorAccent)

        this.dialogBuilder = AlertDialog.Builder(context)
        this.isVerticalScrollBarEnabled = false

        this.views = LinearLayout(context)
        this.views.orientation = LinearLayout.VERTICAL
        this.addView(views)

        scrollerTask = Runnable {
            val newY = scrollY
            if (initialY - newY == 0) { // stopped
                val remainder = initialY % itemHeight
                val divided = initialY / itemHeight

                if (remainder == 0) {
                    selectedIndex = divided + offset
                } else {
                    if (remainder > itemHeight / 2) {
                        this@ItemPicker.post {
                            this@ItemPicker.smoothScrollTo(0, initialY - remainder + itemHeight)
                            selectedIndex = divided + offset + 1
                        }
                    } else {
                        this@ItemPicker.post {
                            this@ItemPicker.smoothScrollTo(0, initialY - remainder)
                            selectedIndex = divided + offset
                        }
                    }
                }

            } else {
                initialY = scrollY
                this@ItemPicker.postDelayed(scrollerTask, newCheck.toLong())
            }
        }
    }

    private fun startScrollerTask() {
        initialY = scrollY
        this.postDelayed(scrollerTask, newCheck.toLong())
    }

    private fun initData() {
        this.displayItemCount = offset * 2 + 1
        for (item in items) {
            views.addView(createView(item))
        }

        refreshItemView(0)
    }

    private fun createView(item: T): TextView {
        val tv = TextView(context)
        tv.layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        tv.setSingleLine(true)
        tv.setTextSize(COMPLEX_UNIT_SP, 16f)
        tv.text = item.toString()
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.gravity = Gravity.CENTER
        val verticalPadding = dip2px(15f)
        val horizontalPadding = dip2px(5f)
        tv.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        if (0 == itemHeight) {
            itemHeight = getViewMeasuredHeight(tv)
            views.layoutParams =
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * displayItemCount)
            val lp = this.layoutParams as LinearLayout.LayoutParams
            this.layoutParams = LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount)
        }
        return tv
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        refreshItemView(t)
    }

    private fun refreshItemView(y: Int) {
        var position = y / itemHeight + offset
        val remainder = y % itemHeight
        val divided = y / itemHeight

        if (remainder == 0) {
            position = divided + offset
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1
            }
        }

        val childSize = views.childCount
        for (i in 0 until childSize) {
            val itemView = views.getChildAt(i) as TextView
            if (position == i) {
                itemView.setTextColor(Color.parseColor("#000000"))
            } else {
                itemView.setTextColor(color1)
            }
        }
    }

    private fun obtainSelectedAreaBorder(): IntArray {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = intArrayOf(itemHeight * offset, itemHeight * (offset + 1))
        }

        return selectedAreaBorder as IntArray
    }

    override fun setBackground(background: Drawable?) {
        if (viewWidth == 0) {
            viewWidth = (context as Activity).windowManager.defaultDisplay.width
        }

        initPaint()

        val mBackground = object : Drawable() {
            override fun draw(canvas: Canvas) {
                canvas.drawLine(
                    0f,
                    obtainSelectedAreaBorder()[0].toFloat(),
                    viewWidth.toFloat(),
                    obtainSelectedAreaBorder()[0].toFloat(),
                    paint
                )
                canvas.drawLine(
                    0f,
                    obtainSelectedAreaBorder()[1].toFloat(),
                    viewWidth.toFloat(),
                    obtainSelectedAreaBorder()[1].toFloat(),
                    paint
                )
            }

            override fun setAlpha(alpha: Int) {
                // empty for now
            }

            override fun setColorFilter(cf: ColorFilter?) {
                // empty for now
            }

            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }

        super.setBackground(mBackground)
    }

    private fun initPaint() {
        paint.color = color
        paint.strokeWidth = dip2px(2f).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        background = null
    }

    override fun fling(velocityY: Int) {
        super.fling(velocityY / 2)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            startScrollerTask()
        }
        return super.onTouchEvent(ev)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        isVerticalFadingEdgeEnabled = true
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun getViewMeasuredHeight(view: View): Int {
        val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.measuredHeight
    }

    fun getSelectedItem(): T {
        return items[selectedIndex]
    }

    fun getOffset(): Int {
        return offset
    }

    /**
     * add the items to display in the dialog (display name is Object#toString)
     * @param list of items
     * @param emptyItem you need to provide one default empty item to fill empty space created by the offset
     * @return this
     */
    fun withItems(emptyItem: T, list: List<T>): ItemPicker<T> {
        items.clear()
        items.addAll(list)

        for (i in 0 until offset) {
            items.add(0, emptyItem)
            items.add(emptyItem)
        }

        initData()

        return this
    }

    /**
     * @param position in the list to be selected
     * @return this
     */
    fun withSelection(position: Int): ItemPicker<T> {
        val p = position - offset
        selectedIndex = position
        this.post { this@ItemPicker.smoothScrollTo(0, p * itemHeight) }

        return this
    }

    /**
     * @param item selects the given @item using the Object#equals() method
     * @return this
     */
    fun withSelection(item: T): ItemPicker<T> {
        for (it in items) {
            if (it == item) {
                return withSelection(items.indexOf(it))
            }
        }

        return this
    }

    fun withOffset(offset: Int): ItemPicker<T> {
        this.offset = offset
        return this
    }

    /**
     * @param title of the dialog
     * @return this
     */
    fun withTitle(title: String): ItemPicker<T> {
        dialogBuilder.setTitle(title)
        return this
    }

    fun withPositiveButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener): ItemPicker<T> {
        dialogBuilder.setPositiveButton(textId, listener)
        return this
    }

    fun withPositiveButton(text: String, listener: DialogInterface.OnClickListener) : ItemPicker<T> {
        dialogBuilder.setPositiveButton(text, listener)
        return this
    }

    fun withNegativeButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener): ItemPicker<T> {
        dialogBuilder.setNegativeButton(textId, listener)
        return this
    }

    fun withNegativeButton(text: String, listener: DialogInterface.OnClickListener) : ItemPicker<T> {
        dialogBuilder.setNegativeButton(text, listener)
        return this
    }

    fun withView(view: View): ItemPicker<T> {
        dialogBuilder.setView(view)
        return this
    }

    fun show() {
        dialogBuilder.show()
    }
}
