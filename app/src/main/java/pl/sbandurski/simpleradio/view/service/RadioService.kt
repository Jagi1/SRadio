package pl.sbandurski.simpleradio.view.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.listener.ILoadingStationAnimationListener
import pl.sbandurski.simpleradio.view.listener.TrackChangeListener
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.util.ParsingHeaderData
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import java.net.URL
import java.util.*

class RadioService: Service(), MediaPlayer.OnPreparedListener {

    private lateinit var mNotificationLayout: RemoteViews
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mTask: MyTimerTask
    private lateinit var mTimer: Timer
    private lateinit var mNotification: Notification
    private lateinit var mTrackListener: TrackChangeListener
    private lateinit var mLoadingStationAnimationListener: ILoadingStationAnimationListener
    private lateinit var mStation: Station
    private lateinit var mWifiManager: WifiManager
    private lateinit var mWifiLock: WifiManager.WifiLock
    private val PACKAGE = "pl.qwisdom.simpleradio"
    var mTrackData: ParsingHeaderData.TrackData? = null
    private val iBinder = LocalBinder()
    private var mUrl: String? = null
    var mStarted = false
    lateinit var mPlayer: MediaPlayer
    var mPrepared = false

    override fun onBind(intent: Intent?): IBinder? {
        val name = intent?.getStringExtra("NAME")
        mUrl = intent?.getStringExtra("URL")
        val id = intent?.getStringExtra("DRAWABLE_ID")

        mPlayer = MediaPlayer().apply {
            setOnPreparedListener(this@RadioService)
            setDataSource(mUrl)
            prepareAsync()
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock")
        mWifiLock.acquire()



        val notificationIntent = Intent(this, MainActivity::class.java)
//        mPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.action = Intent.ACTION_MAIN
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mPendingIntent = PendingIntent.getActivity(applicationContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        mNotificationLayout = RemoteViews(packageName, R.layout.notification_foreground)
        mNotificationLayout.setTextViewText(R.id.title, name)

        mTask = MyTimerTask()
        mTimer = Timer()
        mTimer.schedule(mTask, 0, 2000)

        return iBinder
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mPlayer.start()
        mPrepared = true
        mStarted = true
        mNotificationLayout.setImageViewBitmap(R.id.station_logo_civ, mStation.getImage())
        mNotification = createNotification()
        mLoadingStationAnimationListener.onLoadingStationAnimationChange()
        startForeground(1, mNotification)
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, resources.getString(R.string.CHANNEL_ID))
            .setSmallIcon(R.drawable.ic_stat_radio)
            .setContentIntent(mPendingIntent)
            .setContent(mNotificationLayout)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(Notification.PRIORITY_MAX)
            .build().also {
                it.flags = Notification.FLAG_FOREGROUND_SERVICE and Notification.FLAG_ONGOING_EVENT
            }

    override fun onUnbind(intent: Intent?): Boolean {
        if (mPrepared)
            mPlayer.release()
        mTimer.cancel()
        mTimer.purge()
        mPrepared = false
        mWifiLock.release()
        return super.onUnbind(intent)
    }

    inner class LocalBinder: Binder() {
        fun getService(): RadioService = this@RadioService
    }

    inner class NotificationStopHandler: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (mPrepared) {
                Log.d("NOTIFICATION", "Stop clicked.")
                mPlayer.release()
                onDestroy()
            }
        }
    }

    inner class MyTimerTask: TimerTask() {
        private var task: MyAsyncTask? = null

        override fun run() {
            task = MyAsyncTask()
            task!!.execute()
        }
    }

    inner class MyAsyncTask: AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            val url = URL(mUrl)
            val streaming = ParsingHeaderData()
            val tD = streaming.getTrackDetails(url)
            if (mTrackData == null) {
                if (tD.toString() != " - ") {
                    mTrackListener.onTrackChange(tD)
                    mTrackData = tD
                    updateNotification()
                }
                return
            }
            if (!checkMeta(tD)) {
                if (tD.toString() != " - ") {
                    mTrackListener.onTrackChange(tD)
                    mTrackData = tD
                    updateNotification()
                }
            }
        }
    }

    private fun checkMeta(track: ParsingHeaderData.TrackData) : Boolean {
        return (track.title == mTrackData!!.title && track.artist == mTrackData!!.artist)
    }

    private fun updateNotification() {
        val text = mTrackData!!.artist + " - " + mTrackData!!.title
        mNotification = getMyActivityNotification(text)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, mNotification)
    }

    private fun getMyActivityNotification(text: String): Notification {
        mNotificationLayout.setTextViewText(R.id.song, text)
        return createNotification()
    }

    fun setTrackListener(listener: TrackChangeListener) {
        mTrackListener = listener
    }

    fun setStation(station: Station) {
        this.mStation = station
    }

    fun setLoadingAnimationListener(listener: ILoadingStationAnimationListener) {
        mLoadingStationAnimationListener = listener
    }

    private fun getDrawable(name: String): Int = getId(name, "drawable")

    private fun getId(name: String, type: String) = resources.getIdentifier(name, type, PACKAGE)
}
