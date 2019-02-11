package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.XmlData
import com.lyeeedar.Util.clamp

class LoopedMusic : ISoundChannel
{
	lateinit var name: String
	var music: Music? = null

	var minVolume: Float = 1f
	var maxVolume: Float = 1f
	var minSwapTime: Float = 1f
	var maxSwapTime: Float = 1f

	var targetVolume: Float = 1f
	var swapTime: Float = 0f
	var swapPoint: Float = 0f
	var lastVolume: Float = 1f

	val currentVolume: Float
		get() = if (swapTime > 0f) lastVolume + (swapPoint / swapTime).clamp(0f, 1f) * (targetVolume - lastVolume) else lastVolume

	var parentVolume: Float = 1f

	override fun create()
	{
		if (music == null)
		{
			music = Gdx.audio.newMusic(Gdx.files.internal("Music/$name.ogg"))
			music!!.isLooping = true
		}
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume

		if (music != null)
		{
			music!!.volume = getVolume(parentVolume * this.currentVolume)
		}
	}

	override fun play()
	{
		create()

		music!!.play()
		music!!.volume = getVolume(parentVolume * this.currentVolume)
		music!!.isLooping = true
	}

	override fun stop()
	{
		music!!.stop()
	}

	override fun isComplete(): Boolean = true

	override fun dispose()
	{
		music?.stop()
		music?.dispose()
		music = null
	}

	var lastSetVolume: Float = -1f
	override fun update(delta: Float)
	{
		if (music == null) return

		swapPoint += delta

		val requestedVolume = getVolume(currentVolume * parentVolume)

		if (lastSetVolume != requestedVolume)
		{
			lastSetVolume = requestedVolume

			music!!.volume = requestedVolume
		}

		if (swapPoint >= swapTime)
		{
			lastVolume = targetVolume
			targetVolume = minVolume + Random.random() * (maxVolume - minVolume)
			swapTime = minSwapTime + Random.random() * (maxSwapTime - minSwapTime)
			swapPoint = 0f
		}
	}

	override fun parse(xml: XmlData)
	{
		name = xml.get("File")

		val volumeStr = xml.get("Volume", "1,1")!!.split(",")
		minVolume = volumeStr[0].toFloat()
		maxVolume = volumeStr[1].toFloat()

		targetVolume = minVolume + Random.random() * (maxVolume - minVolume)
		lastVolume = targetVolume

		val swapStr = xml.get("SwapTime", "1,1")!!.split(",")
		minSwapTime = swapStr[0].toFloat()
		maxSwapTime = swapStr[1].toFloat()
	}
}