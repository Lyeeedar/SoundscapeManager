package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.clamp
import com.lyeeedar.Util.getHandle
import java.util.zip.ZipFile

class LoopedMusic : ISoundChannel
{
	lateinit var name: String
	var music: Music? = null

	lateinit var zip: ZipFile

	var minVolume: Float = 1f
	var maxVolume: Float = 1f
	var minSwapTime: Float = 1f
	var maxSwapTime: Float = 1f

	var targetVolume: Float = 1f
	var swapTime: Float = 0f
	var swapPoint: Float = 0f
	var lastVolume: Float = 1f

	val currentVolume: Float
		get() = lastVolume + (swapPoint / swapTime).clamp(0f, 1f) * (targetVolume - lastVolume)

	var fadeTime: Float = 0f
	var fadePoint: Float = 0f
	var isFading: Boolean = false
	var fadeIn: Boolean = false

	val isFadeComplete: Boolean
		get() = music == null || fadePoint >= fadeTime

	var parentVolume: Float = 1f

	override fun create()
	{
		music = Gdx.audio.newMusic(zip.getHandle("Music/$name.ogg"))
		music!!.isLooping = true
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume

		if (!isFading && music != null)
		{
			music!!.volume = parentVolume * this.currentVolume
		}
	}

	override fun play()
	{
		fadeIn(0.25f)
	}

	override fun stop()
	{
		fadeOut(0.25f)
	}

	override fun isComplete(): Boolean = isFadeComplete

	override fun dispose()
	{
		music?.stop()
		music?.dispose()
		music = null
	}

	override fun update(delta: Float)
	{
		if (music == null) return

		swapPoint += delta
		music!!.volume = currentVolume * parentVolume

		if (swapPoint >= swapTime)
		{
			lastVolume = targetVolume
			targetVolume = minVolume + Random.random() * (maxVolume - minVolume)
			swapTime = minSwapTime + Random.random() * (maxSwapTime - minSwapTime)
			swapPoint = 0f
		}

		if (isFading)
		{
			fadePoint += delta

			val alpha = (fadePoint / fadeTime).clamp(0f, 1f)

			if (fadeIn)
			{
				if (!music!!.isPlaying) music!!.play()
				music!!.volume = currentVolume * alpha * parentVolume
			}
			else
			{
				music!!.volume = currentVolume * (1f - alpha) * parentVolume
			}

			if (isFadeComplete)
			{
				isFading = false

				if (fadeIn)
				{
					music!!.volume = currentVolume * parentVolume
				}
				else
				{
					music!!.volume = 0f
				}
			}
		}
	}

	fun fadeIn(duration: Float)
	{
		if (!isFading || !fadeIn)
		{
			fadeIn = true
			fadePoint = 0f
			fadeTime = duration
			isFading = true
		}
	}

	fun fadeOut(duration: Float)
	{
		if (!isFading || fadeIn)
		{
			fadeIn = false
			fadePoint = 0f
			fadeTime = duration
			isFading = true
		}
	}

	override fun parse(zip: ZipFile, xml: XmlReader.Element)
	{
		this.zip = zip

		name = xml.get("File")

		val volumeStr = xml.get("Volume", "1,1").split(",")
		minVolume = volumeStr[0].toFloat()
		maxVolume = volumeStr[1].toFloat()

		targetVolume = minVolume + Random.random() * (maxVolume - minVolume)
		lastVolume = targetVolume

		val swapStr = xml.get("SwapTime", "1,1").split(",")
		minSwapTime = swapStr[0].toFloat()
		maxSwapTime = swapStr[1].toFloat()
	}
}