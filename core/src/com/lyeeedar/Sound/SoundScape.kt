package com.lyeeedar

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sound.OneShotSoundEffect
import com.lyeeedar.Util.children

class SoundScape()
{
	var volume: Float = 1f
		set(value)
		{
			field = value

			for (layer in layers)
			{
				layer.changeVolume(volume)
			}

			for (oneShot in oneShots)
			{
				oneShot.changeVolume(volume)
			}
		}

	internal val layers: Array<SoundLayer> = Array()
	val presets: Array<Preset> = Array()
	val oneShots = Array<OneShotSoundEffect>()

	fun applyPreset(preset: Preset)
	{
		for (presetLayer in preset.layers)
		{
			val layer = layers.first{ it.name == presetLayer.layer }

			if (layer.enabled != presetLayer.enabled)
			{
				layer.enabled = presetLayer.enabled

				if (layer.enabled)
				{
					layer.play()
				}
				else
				{
					layer.stop()
				}
			}

			layer.volume = presetLayer.volume
			layer.changeVolume(volume)
		}
	}

	fun create()
	{
		for (layer in layers) layer.create()
		for (oneShot in oneShots) oneShot.create()
	}

	fun play()
	{
		for (layer in layers) layer.play()
	}

	fun update(delta: Float)
	{
		for (layer in layers) layer.update(delta)
	}

	fun stop()
	{
		for (layer in layers) layer.stop()
	}

	fun complete(): Boolean
	{
		return layers.all { it.isComplete() }
	}

	fun dispose()
	{
		for (layer in layers) layer.dispose()
		for (oneShot in oneShots) oneShot.dispose()
	}

	fun parse(xml: XmlReader.Element)
	{
		val layersEl = xml.getChildByName("Layers")
		val presetsEl = xml.getChildByName("Presets")

		if (layersEl == null) return

		for (el in layersEl.children())
		{
			val layer = SoundLayer()
			layer.parse(el)

			layers.add(layer)
		}

		for (el1 in presetsEl.children())
		{
			val name = el1.get("Name")
			val preset = Preset(name)
			presets.add(preset)

			val presetLayersEl = el1.getChildByName("Layers")
			for (el2 in presetLayersEl.children())
			{
				val layer = el2.get("Layer")
				val enabled = el2.getBoolean("Enabled", true)
				val volume = el2.getFloat("Volume", 1f)

				preset.layers.add(PresetLayer(layer, enabled, volume))
			}
		}

		val oneShotsEl = xml.getChildByName("OneShots")
		if (oneShotsEl != null)
		{
			for (el in oneShotsEl.children())
			{
				val oneShot = OneShotSoundEffect()
				oneShot.parse(el)

				oneShots.add(oneShot)
			}
		}
	}
}

data class Preset(val name: String)
{
	val layers: Array<PresetLayer> = Array()
}

data class PresetLayer(val layer: String, val enabled: Boolean, val volume: Float)