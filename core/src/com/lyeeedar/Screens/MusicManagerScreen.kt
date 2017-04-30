package com.lyeeedar.Screens

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.SoundScape
import com.lyeeedar.UI.Seperator
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getXml
import ktx.actors.onClick
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton

class MusicManagerScreen : AbstractScreen()
{
	val allSoundScapes: Array<String> by lazy { loadSoundScapeList() }

	fun loadSoundScapeList() : Array<String>
	{
		val out = Array<String>()

		val xml = getXml("SoundScapes/SoundScapeList")
		for (el in xml.children())
		{
			out.add(el.text)
		}

		return out
	}

	var currentSoundScape: SoundScape? = null
	var queuedSoundScape: SoundScape? = null

	lateinit var soundScapeTable: Table

	override fun create()
	{
		soundScapeTable = Table()

		val t = table {
			scrollPane("default", Global.skin) { cell -> cell.width(200f).pad(10f)
				table {
					for (soundScape in allSoundScapes)
					{
						 textButton(soundScape, "default", Global.skin) { cell -> cell.width(150f).pad(5f)
							onClick { inputEvent, kTextButton ->
								queuedSoundScape = loadSoundScape(soundScape)
								fillSoundTable(queuedSoundScape!!, soundScape)
							}
						 }
						row()
					}
				}
			}

			add(Seperator(Global.skin, true)).pad(10f).growY()
			add(soundScapeTable).pad(10f).grow()
		}

		mainTable.add(t).grow()
		t.setFillParent(true)
	}

	fun fillSoundTable(soundScape: SoundScape, name: String)
	{
		soundScapeTable.clear()

		soundScapeTable.add(Label(name, Global.skin, "title")).pad(20f)
		soundScapeTable.row()

		val presetTable = Table()
		soundScapeTable.add(presetTable).growX()
		soundScapeTable.row()

		for (preset in soundScape.presets)
		{
			val button = TextButton(preset.name, Global.skin)
			button.onClick { inputEvent, textButton ->
				soundScape.applyPreset(preset)
				fillSoundTable(soundScape, name)
			}
			presetTable.add(button).width(150f).pad(5f)
		}

		for (layer in soundScape.layers)
		{
			val entry = Table()

			entry.add(Label(layer.name, Global.skin)).width(150f)

			val check = CheckBox("Enabled", Global.skin)
			check.isChecked = layer.enabled
			check.addListener(object : ChangeListener() {
				override fun changed(event: ChangeEvent?, actor: Actor?)
				{
					layer.enabled = check.isChecked

					if (layer.enabled)
					{
						layer.play()
					}
					else
					{
						layer.stop()
					}
				}
			})

			entry.add(check).width(150f)

			val slider = Slider(0f, 1f, 0.05f, false, Global.skin)
			slider.value = layer.volume
			slider.addListener(object : ChangeListener() {
				override fun changed(event: ChangeEvent?, actor: Actor?)
				{
					layer.volume = slider.value
					layer.changeVolume(1f)
				}
			})
			entry.add(slider).width(200f)

			soundScapeTable.add(entry).expandX().left()
			soundScapeTable.row()
		}
	}

	override fun doRender(delta: Float)
	{
		currentSoundScape?.update(delta)

		if (queuedSoundScape != null)
		{
			if (currentSoundScape?.complete() ?: true)
			{
				currentSoundScape?.dispose()

				currentSoundScape = queuedSoundScape
				queuedSoundScape = null

				currentSoundScape!!.create()
				currentSoundScape!!.play()
			}
		}
	}

	fun loadSoundScape(name: String): SoundScape
	{
		val xml = getXml("SoundScapes/$name")

		val soundScape = SoundScape()
		soundScape.parse(xml)

		soundScape.applyPreset(soundScape.presets.first())

		return soundScape
	}
}