package pl.sbandurski.simpleradio.view.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.*
import pl.sbandurski.simpleradio.R
import pl.sbandurski.simpleradio.view.listener.ILoadingStationAnimationListener
import pl.sbandurski.simpleradio.view.listener.TrackChangeListener
import pl.sbandurski.simpleradio.view.model.GradientPalette
import pl.sbandurski.simpleradio.view.model.Station
import pl.sbandurski.simpleradio.view.util.*
import pl.sbandurski.simpleradio.view.view.activity.MainActivity
import java.net.URL
import java.util.*

class RadioService: Service(), Player.EventListener {

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
    private var mGradientPalette : GradientPalette? = null
    private val PACKAGE = "pl.qwisdom.simpleradio"
    var mTrackData: ParsingHeaderData.TrackData? = null
    private val iBinder = LocalBinder()
    private var mUrl: String? = null
    lateinit var mPlayer: ExoPlayer

    override fun onBind(intent: Intent?): IBinder? {
        mUrl = intent?.getStringExtra("URL")
        mGradientPalette = intent?.getParcelableExtra("PALETTE")
        initializePlayer()
        initializeWiFi()
        initializePendingIntent()
        val name = intent?.getStringExtra("NAME")
        initializeNotificationLayout(name)
        initializeSongInfoTask()
        initializeSongInfoTimer()
        return iBinder
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady && playbackState == Player.STATE_READY) {
            mNotificationLayout.setImageViewBitmap(R.id.station_logo_civ, mStation.getImage())
            mNotification = createNotification()
            mLoadingStationAnimationListener.onLoadingStationAnimationChange()
            startForeground(1, mNotification)
        }
        super.onPlayerStateChanged(playWhenReady, playbackState)
    }

    private fun initializePlayer() {
        val userAgent = getUserAgent(applicationContext)
        val dataSourceFactory = getDefaultHttpDataSourceFactory(userAgent)
        val mediaSource = getProgressiveMediaSource(mUrl, dataSourceFactory)
        mPlayer = buildSimpleExoPlayer(this)
        mPlayer.apply {
            prepare(mediaSource)
            addListener(this@RadioService)
            playWhenReady = false
        }
    }
    private fun initializeSongInfoTask() {
        mTask = MyTimerTask()
    }
    private fun initializeSongInfoTimer() {
        mTimer = Timer()
        mTimer.schedule(mTask, 0, 2000)
    }
    private fun initializeWiFi() {
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock")
        mWifiLock.acquire()
    }
    private fun initializeNotificationLayout(stationName : String?) {
        mNotificationLayout = RemoteViews(packageName, R.layout.notification_foreground)
        mNotificationLayout.setTextViewText(R.id.title, stationName)
        val color = mGradientPalette?.let { palette ->
            when {
                palette.lightVibrantSwatch != null -> palette.lightVibrantSwatch
                palette.lightMuted != null -> palette.lightMuted
                palette.vibrantSwatch != null -> palette.vibrantSwatch
                palette.dominantSwatch != null -> palette.dominantSwatch
                else -> 0x000000
            }
        } ?: 0x000000
        mNotificationLayout.setTextColor(R.id.title, color)
        mNotificationLayout.setTextColor(R.id.song, color)
        mNotificationLayout.setTextColor(R.id.app_name, color)
    }
    private fun initializePendingIntent() {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.action = Intent.ACTION_MAIN
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mPendingIntent = PendingIntent.getActivity(applicationContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createNotification(): Notification {
        val channelId = resources.getString(R.string.CHANNEL_ID)
        val builder = NotificationCompat.Builder(this, channelId)
        builder.apply {
            setSmallIcon(R.drawable.ic_stat_radio)
            setContentIntent(mPendingIntent)
            setContent(mNotificationLayout)
            setVisibility(Notification.VISIBILITY_PUBLIC)
            priority = Notification.PRIORITY_MAX
        }
        val notification = builder.build()
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE and Notification.FLAG_ONGOING_EVENT
        return notification
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (mPlayer.playWhenReady)
            mPlayer.release()
        mTimer.cancel()
        mTimer.purge()
        mWifiLock.release()
        return super.onUnbind(intent)
    }

    inner class LocalBinder: Binder() {
        fun getService(): RadioService = this@RadioService
    }

    inner class NotificationStopHandler: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (mPlayer.playWhenReady) {
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
            Log.d("TRACK_DATA", tD.toString())
            if (mTrackData == null) {
                if (tD.toString() != " - ") {
                    if (this@RadioService::mTrackListener.isInitialized) {
                        mTrackListener.onTrackChange(tD)
                        mTrackData = tD
                        updateNotification()
                    }
                }
                return
            }
            if (!checkMeta(tD)) {
                if (tD.toString() != " - ") {
                    if (this@RadioService::mTrackListener.isInitialized) {
                        mTrackListener.onTrackChange(tD)
                        mTrackData = tD
                        updateNotification()
                    }
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
