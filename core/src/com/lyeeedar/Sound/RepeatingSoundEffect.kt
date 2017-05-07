package com.lyeeedar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.getHandle
import java.util.zip.ZipFile


class RepeatingSoundEffect : ISoundChannel
{
	enum class Type
	{
		Continuous,
		Interval
	}

	lateinit var zip: ZipFile

	private var soundID: Long = 0
	private lateinit var soundName: String
	private var sound: Sound? = null
	private var isPlaying = false
	private var canPlay = false

	private var type: Type? = null
	private var repeatMin: Float = 0f
	private var repeatMax: Float = 0f
	private var pitchMin: Float = 0f
	private var pitchMax: Float = 0f
	private var volumeMin: Float = 0f
	private var volumeMax: Float = 0f

	private val repeat: Float
		get() = repeatMin + Random.random() * (repeatMax - repeatMin)

	private val pitch: Float
		get() = pitchMin + Random.random() * (pitchMax - pitchMin)

	private val volume: Float
		get() = volumeMin + Random.random() * (volumeMax - volumeMin)

	private var nextRepeat: Float = 0f

	private var timeAccumulator: Float = 0f

	private var parentVolume: Float = 1f

	override fun create()
	{
		sound = loadSound(zip, soundName)
	}

	override fun changeVolume(volume: Float)
	{
		parentVolume = volume
	}

	override fun play()
	{
		canPlay = true
	}

	override fun isComplete(): Boolean = true

	override fun dispose()
	{

	}

	override fun update(delta: Float)
	{
		if (!canPlay) return

		if (type == Type.Continuous)
		{
			if (!isPlaying)
			{
				soundID = sound!!.loop(volume * parentVolume)
				isPlaying = true
			}
		}
		else
		{
			timeAccumulator += delta

			if (timeAccumulator >= nextRepeat)
			{
				soundID = sound!!.play(volume * parentVolume, pitch, 0f)
				isPlaying = true

				nextRepeat = repeat
				timeAccumulator = 0f
			}
		}
	}

	override fun stop()
	{
		canPlay = false

		if (isPlaying)
		{
			sound!!.stop(soundID)

			isPlaying = false
			timeAccumulator = 0f
		}
	}

	override fun parse(zip: ZipFile, xml: Element)
	{
		this.zip = zip

		soundName = xml.get("File")
		type = Type.valueOf(xml.get("Type", "Interval"))

		val repeatStr = xml.get("Repeat").split(",")
		repeatMin = repeatStr[0].toFloat()
		repeatMax = repeatStr[1].toFloat()

		val pitchStr = xml.get("Pitch").split(",")
		pitchMin = pitchStr[0].toFloat()
		pitchMax = pitchStr[1].toFloat()

		val volumeStr = xml.get("Volume").split(",")
		volumeMin = volumeStr[0].toFloat()
		volumeMax = volumeStr[1].toFloat()

		nextRepeat = repeat

		timeAccumulator = repeatMin * MathUtils.random()
	}

	companion object
	{
		private val loadedSounds = HashMap<String, Sound?>()

		fun loadSound(zip: ZipFile, path: String): Sound?
		{
			if (loadedSounds.containsKey(path))
			{
				return loadedSounds.get(path)
			}

			var file = zip.getHandle("Sounds/$path.mp3")
			if (!file.exists())
			{
				file = zip.getHandle("Sounds/$path.ogg")

				if (!file.exists())
				{
					loadedSounds.put(path, null)
					return null
				}
			}

			val sound = Gdx.audio.newSound(file)

			loadedSounds.put(path, sound)

			return sound
		}
	}
}
