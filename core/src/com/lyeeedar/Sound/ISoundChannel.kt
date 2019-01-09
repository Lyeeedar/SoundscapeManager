package com.lyeeedar

import com.badlogic.gdx.utils.XmlReader

interface ISoundChannel
{
	fun create()

	fun update(delta: Float)
	fun play()
	fun stop()

	fun changeVolume(volume: Float)

	fun isComplete(): Boolean

	fun dispose()

	fun parse(xml: XmlReader.Element)
}