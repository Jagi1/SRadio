package pl.sbandurski.simpleradio.view.util

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util

fun getUserAgent(context: Context): String = Util.getUserAgent(context, "Simple Radio")

fun getDefaultHttpDataSourceFactory(userAgent: String) = DefaultHttpDataSourceFactory(
    userAgent, null,
    DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
    DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
    true
)

fun getProgressiveMediaSource(
    url: String?,
    dataSourceFactory: DefaultHttpDataSourceFactory
): ProgressiveMediaSource =
    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))

fun buildSimpleExoPlayer(context: Context): ExoPlayer = SimpleExoPlayer.Builder(context).build()