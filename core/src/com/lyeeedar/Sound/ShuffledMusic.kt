package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.*
import java.util.zip.ZipFile

class ShuffledMusic : ISoundChannel
{
	lateinit var zip: ZipFile

	val tracks: Array<Track> = Array()
	val shuffleIndices: Array<Int> = Array()

	var currentTrack: Track? = null
	var volume: Float = 1f
	var currentMusic: Music? = null

	var channelVolume: Float = 1f

	var fadeTime: Float = 0f
	var fadePoint: Float = 0f
	var isFading: Boolean = false

	var canPlay = false

	val isFadeComplete: Boolean
		get() = fadePoint >= fadeTime

	var parentVolume: Float = 1f

	override fun create()
	{
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume

		if (currentMusic != null && !isFading)
		{
			currentMusic!!.volume = volume * channelVolume * parentVolume
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

		currentMusic = Gdx.audio.newMusic(zip.getHandle("Music/${currentTrack!!.name}.ogg"))
		currentMusic!!.setOnCompletionListener { nextTrack() }

		currentMusic!!.play()
		currentMusic!!.volume = volume * channelVolume * parentVolume
	}

	fun shuffle()
	{
		shuffleIndices.clear()

		val rawIndices = Array<Int>()
		for (i in 0..tracks.size-1)
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
		if (isFading)
		{
			fadePoint += delta

			val alpha = (fadePoint / fadeTime).clamp(0f, 1f)

			currentMusic!!.volume = volume * (1f - alpha) * channelVolume * parentVolume

			if (isFadeComplete) isFading = false
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
		fadePoint = 0f
		fadeTime = 0.25f
		isFading = true
	}

	override fun isComplete(): Boolean = isFadeComplete

	override fun dispose()
	{
		if (currentMusic != null)
		{
			currentMusic?.stop()
			currentMusic?.dispose()
			currentMusic = null
		}
	}

	override fun parse(zip: ZipFile, xml: XmlReader.Element)
	{
		this.zip = zip

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