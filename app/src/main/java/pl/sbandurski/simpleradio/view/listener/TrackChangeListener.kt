package pl.sbandurski.simpleradio.view.listener

import pl.sbandurski.simpleradio.view.util.ParsingHeaderData

interface TrackChangeListener {
    fun onTrackChange(track: ParsingHeaderData.TrackData)
}