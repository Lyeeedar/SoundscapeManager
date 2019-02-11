package com.lyeeedar.Sound

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Array
import com.lyeeedar.ISoundChannel
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.XmlData
import com.lyeeedar.getVolume

class OneShotSound()
{
	private var soundID: Long = 0
	private lateinit var soundName: String
	private var sound: Sound? = null

	private var pitchMin: Float = 0f
	private var pitchMax: Float = 0f
	private var volumeMin: Float = 0f
	private var volumeMax: Float = 0f

	private val pitch: Float
		get() = pitchMin + Random.random() * (pitchMax - pitchMin)

	private val volume: Float
		get() = volumeMin + Random.random() * (volumeMax - volumeMin)

	fun create()
	{
		sound = AssetManager.loadSound(soundName)!!
	}

	fun play(volume: Float)
	{
		if (sound == null)
		{
			create()
		}

		soundID = sound!!.play(getVolume(volume * this.volume), pitch, 0f)
	}

	fun parse(xml: XmlData)
	{
		soundName = xml.get("File")

		val pitchStr = xml.get("Pitch").split(",")
		pitchMin = pitchStr[0].toFloat()
		pitchMax = pitchStr[1].toFloat()

		val volumeStr = xml.get("Volume").split(",")
		volumeMin = volumeStr[0].toFloat()
		volumeMax = volumeStr[1].toFloat()
	}
}

class OneShotSoundEffect : ISoundChannel
{
	lateinit var name: String

	val sounds = Array<OneShotSound>()

	private var parentVolume: Float = 1f

	override fun create()
	{
		if (sounds.size == 0)
		{
			for (sound in sounds)
			{
				sound.create()
			}
		}
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume
	}

	override fun play()
	{
		create()

		val chosen = sounds.random()
		chosen.play(parentVolume)
	}

	override fun isComplete(): Boolean = true

	override fun dispose()
	{

	}

	override fun update(delta: Float)
	{

	}

	override fun stop()
	{

	}

	override fun parse(xml: XmlData)
	{
		name = xml.get("Name")

		val soundsEl = xml.getChildByName("Sounds")!!
		for (el in soundsEl.children)
		{
			val sound = OneShotSound()
			sound.parse(el)

			sounds.add(sound)
		}
	}
}
