package com.lyeeedar.Sound

import com.badlogic.gdx.audio.Sound
import com.lyeeedar.ISoundChannel
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.XmlData

class OneShotSoundEffect : ISoundChannel
{
	lateinit var name: String

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

	private var parentVolume: Float = 1f

	override fun create()
	{
		sound = AssetManager.loadSound(soundName)!!
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume
	}

	override fun play()
	{
		soundID = sound!!.play(volume * parentVolume, pitch, 0f)
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
		soundName = xml.get("File")

		val pitchStr = xml.get("Pitch").split(",")
		pitchMin = pitchStr[0].toFloat()
		pitchMax = pitchStr[1].toFloat()

		val volumeStr = xml.get("Volume").split(",")
		volumeMin = volumeStr[0].toFloat()
		volumeMax = volumeStr[1].toFloat()
	}
}
