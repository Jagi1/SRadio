package pl.qwisdom.simpleradio.view.listener

import pl.qwisdom.simpleradio.view.util.ParsingHeaderData

interface TrackChangeListener {
    fun onTrackChange(track: ParsingHeaderData.TrackData)
}