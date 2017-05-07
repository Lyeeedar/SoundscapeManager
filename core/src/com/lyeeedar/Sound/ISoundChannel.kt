package com.lyeeedar

import com.badlogic.gdx.utils.XmlReader
import java.util.zip.ZipFile

interface ISoundChannel
{
	fun create()

	fun update(delta: Float)
	fun play()
	fun stop()

	fun changeVolume(volume: Float)

	fun isComplete(): Boolean

	fun dispose()

	fun parse(zip: ZipFile,  xml: XmlReader.Element)
}