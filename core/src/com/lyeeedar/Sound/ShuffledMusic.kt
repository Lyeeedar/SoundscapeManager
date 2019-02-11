package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.XmlData
import com.lyeeedar.Util.removeRandom

class ShuffledMusic : ISoundChannel
{
	val tracks: Array<Track> = Array()
	val shuffleIndices: Array<Int> = Array()

	var currentTrack: Track? = null
	var volume: Float = 1f
	var currentMusic: Music? = null

	var channelVolume: Float = 1f

	var canPlay = false

	var parentVolume: Float = 1f

	override fun create()
	{
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume

		if (currentMusic != null)
		{
			currentMusic!!.volume = getVolume(volume * channelVolume * parentVolume)
		}
	}

	fun nextTrack()
	{
		if (!canPlay) return

		if (currentMusic != null)
		{
			currentMusic?.stop()
			currentMusic?.dispose()
			currentMusic = null
		}

		if (shuffleIndices.size == 0) shuffle()

		val index = shuffleIndices.removeIndex(shuffleIndices.size-1)

		currentTrack = tracks[index]

		volume = currentTrack!!.volume

		currentMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/${currentTrack!!.name}.ogg"))

		currentMusic!!.play()
		currentMusic!!.volume = getVolume(volume * channelVolume * parentVolume)
	}

	fun shuffle()
	{
		shuffleIndices.clear()

		val rawIndices = Array<Int>()
		for (i in 0 until tracks.size)
		{
			rawIndices.add(i)
		}

		while (rawIndices.size > 0)
		{
			shuffleIndices.add(rawIndices.removeRandom(Random.random))
		}
	}

	override fun update(delta: Float)
	{
		if (canPlay && currentMusic?.isPlaying == false)
		{
			nextTrack()
		}
	}

	override fun play()
	{
		canPlay = true
		nextTrack()
	}

	override fun stop()
	{
		canPlay = false
		currentMusic?.stop()
		currentMusic?.dispose()
		currentMusic = null
	}

	override fun isComplete(): Boolean = true

	override fun dispose()
	{
		if (currentMusic != null)
		{
			currentMusic?.stop()
			currentMusic?.dispose()
			currentMusic = null
		}
	}

	override fun parse(xml: XmlData)
	{
		channelVolume = xml.getFloatAttribute("Volume", 1f)

		for (el in xml.children())
		{
			val name = el.get("File")
			val volume = el.getFloat("Volume")

			tracks.add(Track(name, volume))
		}
	}
}

data class Track(val name: String, val volume: Float)