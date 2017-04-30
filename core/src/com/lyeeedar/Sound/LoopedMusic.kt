package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.clamp

class LoopedMusic : ISoundChannel
{
	lateinit var name: String
	var music: Music? = null
	var volume: Float = 1f

	var fadeTime: Float = 0f
	var fadePoint: Float = 0f
	var isFading: Boolean = false
	var fadeIn: Boolean = false

	val isFadeComplete: Boolean
		get() = music == null || fadePoint >= fadeTime

	var parentVolume: Float = 1f

	override fun create()
	{
		music = Gdx.audio.newMusic(Gdx.files.internal("Music/$name.ogg"))
		music!!.isLooping = true
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume

		if (!isFading && music != null)
		{
			music!!.volume = parentVolume * this.volume
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

		if (isFading)
		{
			fadePoint += delta

			val alpha = (fadePoint / fadeTime).clamp(0f, 1f)

			if (fadeIn)
			{
				if (!music!!.isPlaying) music!!.play()
				music!!.volume = volume * alpha * parentVolume
			}
			else
			{
				music!!.volume = volume * (1f - alpha) * parentVolume
			}

			if (isFadeComplete)
			{
				isFading = false

				if (fadeIn)
				{
					music!!.volume = volume * parentVolume
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

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("File")
		volume = xml.getFloat("Volume")
	}
}