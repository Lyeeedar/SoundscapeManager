package com.lyeeedar

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.children

class SoundLayer : ISoundChannel
{
	internal val sounds: Array<ISoundChannel> = Array()
	lateinit var name: String
	var enabled = true
	var volume = 1f

	var disposed = true

	override fun create()
	{
		if (!enabled) return

		for (sound in sounds) sound.create()
	}

	override fun changeVolume(volume: Float)
	{
		for (sound in sounds) sound.changeVolume(volume * this.volume)
	}

	override fun play()
	{
		if (disposed)
		{
			create()
			disposed = false
		}

		for (sound in sounds) sound.play()
	}

	override fun update(delta: Float)
	{
		if (disposed) return

		if (!enabled)
		{
			if (isComplete())
			{
				dispose()
			}
		}

		for (sound in sounds) sound.update(delta)
	}

	override fun stop()
	{
		if (disposed) return

		for (sound in sounds) sound.stop()
	}

	override fun isComplete(): Boolean
	{
		return disposed || sounds.all { it.isComplete() }
	}

	override fun dispose()
	{
		if (disposed) return

		for (sound in sounds) sound.dispose()

		disposed = true
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name")

		val soundsEl = xml.getChildByName("Sounds")
		for (el in soundsEl.children())
		{
			val action = when (el.name.toUpperCase())
			{
				"LOOPEDMUSIC" -> LoopedMusic()
				"REPEATINGSOUND" -> RepeatingSoundEffect()
				"SHUFFLEDMUSIC" -> ShuffledMusic()

				else -> throw Exception("Unknown sound type '" + el.name + "'!")
			}

			action.parse(el)

			sounds.add(action)
		}

		changeVolume(1f)
	}
}