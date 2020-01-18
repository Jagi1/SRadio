package pl.sbandurski.simpleradio.view.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.widget_floating.view.*
import kotlinx.android.synthetic.main.widget_floating_remove.view.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.view.activity.MainActivity

class FloatingWidgetService: Service(), View.OnClickListener {

    // Variables
    lateinit var mManager: WindowManager
    lateinit var mFloatingView: View
    lateinit var mCollapsedView: View
    lateinit var mExpandedView: View
    lateinit var mRemoveImageView: ImageView
    lateinit var mRemoveFloatingWidgetView: View
    private var szWindow = Point()
    var x_init_cord = 0
    var y_init_cord = 0
    var x_init_margin = 0
    var y_init_margin = 0
    private var isLeft = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_floating_view -> stopSelf()
            R.id.close_expanded_view -> {
                mCollapsedView.visibility = View.VISIBLE
                mExpandedView.visibility = View.GONE
            }
            R.id.open_activity_button -> {
                val intent = Intent(this@FloatingWidgetService, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Init window manager
        mManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Init layout inflater
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        addRemoveView(inflater)
        addFloatingWidgetView(inflater)
        implementClickListeners()
        implementTouchListenerToFloatingWidgetView()
    }

    private fun addRemoveView(inflater: LayoutInflater): View {
        // Inflate removing view layout
        mRemoveFloatingWidgetView = inflater.inflate(R.layout.widget_floating_remove, null)

        // Add view to the window
        val paramRemove = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        // Specify view position
        paramRemove.gravity = Gravity.TOP and Gravity.START

        // Set default visibility of removing widget to GONE
        mRemoveFloatingWidgetView.visibility = View.GONE

        // Add view to the window
        mManager.addView(mRemoveFloatingWidgetView, paramRemove)

        return mRemoveFloatingWidgetView.remove_img
    }

    private fun addFloatingWidgetView(inflater: LayoutInflater) {
        // Inflate floating view
        mFloatingView = inflater.inflate(R.layout.widget_floating, null)

        // Add view to the window
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // Specify view position
        params.gravity = Gravity.TOP and Gravity.START

        // Add view to the window
        mManager.addView(mFloatingView, params)

        // Find id of collapsed view
        mCollapsedView = mFloatingView.collapse_view

        // Find id of expanded view
        mExpandedView = mFloatingView.expanded_container
    }

    private fun getWindowManagerDefaultDisplay() {
        mManager.defaultDisplay.getSize(szWindow)
    }

    private fun implementTouchListenerToFloatingWidgetView() {
        mFloatingView.root_container.setOnTouchListener(object: View.OnTouchListener {

            // Listener variables
            var time_start: Long = 0
            var time_end: Long = 0
            var isLongClick = false
            var inBounded = false
            var remove_img_width = 0
            var remove_img_height = 0
            var handler_longClick = Handler()
            var runnable_longClick = Runnable {
                isLongClick = true
                mRemoveFloatingWidgetView.visibility = View.VISIBLE
                onFloatingWidgetClick()
            }


            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                // Get floating widget view params
                val layoutParams = mFloatingView.layoutParams as WindowManager.LayoutParams

                // Get touch coordinates
                val x_cord = event?.rawX?.toInt()
                val y_cord = event?.rawY?.toInt()
                var x_cord_Destination: Int
                var y_cord_Destination: Int
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        time_start = System.currentTimeMillis()

                        handler_longClick.postDelayed(runnable_longClick, 600)

                        remove_img_width = mRemoveImageView.layoutParams.width
                        remove_img_height = mRemoveImageView.layoutParams.height

                        x_init_cord = x_cord!!
                        y_init_cord = y_cord!!

                        x_init_margin = layoutParams.x
                        y_init_margin = layoutParams.y

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        isLongClick = false
                        mRemoveFloatingWidgetView.visibility = View.GONE
                        mRemoveImageView.layoutParams.height = remove_img_height
                        mRemoveImageView.layoutParams.width = remove_img_width
                        handler_longClick.removeCallbacks(runnable_longClick)

                        if (inBounded) {
                            stopSelf()
                            inBounded = false
                            return false
                        } else {
                            // Get difference between initial and current coordinate
                            val x_diff = x_cord!! - x_init_cord
                            val y_diff = y_cord!! - y_init_cord

                            if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                                time_end = System.currentTimeMillis()
                                if ((time_end - time_start) < 300) onFloatingWidgetClick()
                            }

                            y_cord_Destination = y_init_margin + y_diff

                            val barHeight = getStatusBarHeight()
                            if (y_cord_Destination < 0) {
                                y_cord_Destination = 0
                            } else if (y_cord_Destination + (mFloatingView.height + barHeight) > szWindow.y){
                                y_cord_Destination = szWindow.y - (mFloatingView.height + barHeight)
                            }
                            layoutParams.y = y_cord_Destination
                            inBounded = false
                            resetPosition(x_cord)
                            return true
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x_diff_move = x_cord!! - x_init_cord
                        val y_diff_move = y_cord!! - y_init_cord

                        x_cord_Destination = x_init_margin + x_diff_move
                        y_cord_Destination = y_init_margin + y_diff_move

                        // If user long click floating view, update remove view
                        if (isLongClick) {
                            val x_bound_left = szWindow.x / 2 - (remove_img_width * 1.5).toInt()
                            val x_bound_right = szWindow.x / 2 - (remove_img_width * 1.5).toInt()
                            val y_bound_top = szWindow.y - (remove_img_height * 1.5).toInt()

                            // If floating view comes under remove view, update window manager
                            if ((x_cord in x_bound_left..x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true
                                val x_cord_remove = ((szWindow.x - (remove_img_height * 1.5)) / 2).toInt()
                                val y_cord_remove = (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight())).toInt()

                                if (mRemoveImageView.layoutParams.height == remove_img_height) {
                                    mRemoveImageView.layoutParams.height = (remove_img_height * 1.5).toInt()
                                    mRemoveImageView.layoutParams.width = (remove_img_width * 1.5).toInt()

                                    val paramRemove = mRemoveFloatingWidgetView.layoutParams as WindowManager.LayoutParams
                                    paramRemove.x = x_cord_remove
                                    paramRemove.y = y_cord_remove

                                    mManager.updateViewLayout(mRemoveFloatingWidgetView, paramRemove)
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(mRemoveFloatingWidgetView.width - mFloatingView.width)) / 2
                                layoutParams.y = y_cord_remove + (Math.abs(mRemoveFloatingWidgetView.height - mFloatingView.height)) / 2

                                // Update layout with new X & Y cords
                                mManager.updateViewLayout(mFloatingView, layoutParams)
                                return true
                            } else {
                                // If floating window gets out of the remove view, update remove view again
                                inBounded = false
                                mRemoveImageView.layoutParams.height = remove_img_height
                                mRemoveImageView.layoutParams.width = remove_img_width
                                onFloatingWidgetClick()
                            }
                        }

                        layoutParams.x = x_cord_Destination
                        layoutParams.y = y_cord_Destination

                        // Update layout with new X & Y cords
                        mManager.updateViewLayout(mFloatingView, layoutParams)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun onFloatingWidgetLongClick() {
        val removeParams = mRemoveFloatingWidgetView.layoutParams as WindowManager.LayoutParams
        val x_cord = (szWindow.x - mRemoveFloatingWidgetView.width) / 2
        val y_cord = szWindow.y - (mRemoveImageView.height + getStatusBarHeight())

        removeParams.x = x_cord
        removeParams.y = y_cord

        // Update remove view params
        mManager.updateViewLayout(mRemoveFloatingWidgetView, removeParams)
    }

    /**
     * Resets position of floating widget view on dragging.
     * */
    private fun resetPosition(x_cord_now: Int) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true
            moveToLeft(x_cord_now)
        } else {
            isLeft = false
            moveToRight(x_cord_now)
        }
    }

    private fun moveToLeft(current_x_cord: Int) {
        val x = (szWindow.x - current_x_cord).toLong()

        object: CountDownTimer(500, 5) {

            val mParams = mFloatingView.layoutParams as WindowManager.LayoutParams

            override fun onFinish() {
                mParams.x = 0

                // Update window manager for floating view
                mManager.updateViewLayout(mFloatingView, mParams)
            }

            override fun onTick(millisUntilFinished: Long) {
                val step = (500 - millisUntilFinished) / 5

                // Not bounce effect
//                mParams.x = 0 - (current_x_cord * current_x_cord * step).toInt()

                // Bounce effect
                mParams.x = 0 - bounceValue(step, x).toDouble().toInt()

                // Update window manager for floating view
                mManager.updateViewLayout(mFloatingView, mParams)
            }
        }.start()
    }

    private fun moveToRight(current_x_cord: Int) {
        val x = szWindow.x - current_x_cord

        object: CountDownTimer(500, 5) {

            val mParams = mFloatingView.layoutParams as WindowManager.LayoutParams

            override fun onFinish() {
                mParams.x = szWindow.x - mFloatingView.width

                // Update window manager for floating view
                mManager.updateViewLayout(mFloatingView, mParams)
            }

            override fun onTick(millisUntilFinished: Long) {
                val step = (500 - millisUntilFinished) / 5

                // Not bounce effect
//                mParams.x = (szWindow.x + (current_x_cord * current_x_cord * step) - mFloatingView.width).toInt()

                // Bounce effect
                mParams.x = szWindow.x + bounceValue(step, current_x_cord.toLong()).toDouble().toInt() - mFloatingView.width

                // Update window manager for floating view
                mManager.updateViewLayout(mFloatingView, mParams)
            }
        }.start()
    }

    private fun bounceValue(step: Long, scale: Long): Double = scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step)

    private fun isViewCollapsed(): Boolean = mFloatingView.collapse_view.visibility == View.VISIBLE

    private fun getStatusBarHeight(): Int = (Math.ceil((25 * applicationContext.resources.displayMetrics.density).toDouble())).toInt()

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        getWindowManagerDefaultDisplay()

        val layoutParams = mFloatingView.layoutParams as WindowManager.LayoutParams

        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (layoutParams.y + (mFloatingView.height + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (mFloatingView.height + getStatusBarHeight())
                mManager.updateViewLayout(mFloatingView, layoutParams)
            }
            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x)
            }
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x)
            }
        }
    }

    private fun onFloatingWidgetClick() {
        if (isViewCollapsed()) {
            mCollapsedView.visibility = View.GONE
            mExpandedView.visibility = View.VISIBLE
        }
    }

    private fun implementClickListeners() {
        mFloatingView.close_floating_view.setOnClickListener(this)
        mFloatingView.close_expanded_view.setOnClickListener(this)
        mFloatingView.open_activity_button.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove both view from manager
        mManager.run {
            removeView(mFloatingView)
            removeView(mRemoveFloatingWidgetView)
        }
    }
}