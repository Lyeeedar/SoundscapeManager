package com.lyeeedar

import com.lyeeedar.Util.XmlData

const val MAX_VOLUME = 50.0

fun getVolume(volume: Float): Float
{
	val soundVolume = (volume * MAX_VOLUME).toInt()
	val finalvolume = 1.0f - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)).toFloat()
	return volume
}

interface ISoundChannel
{
	fun create()

	fun update(delta: Float)
	fun play()
	fun stop()

	fun changeVolume(volume: Float)

	fun isComplete(): Boolean

	fun dispose()

	fun parse(xml: XmlData)
}