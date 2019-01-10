package com.lyeeedar.Screens

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.SoundScape
import com.lyeeedar.UI.CollapsibleWidget
import com.lyeeedar.UI.Seperator
import com.lyeeedar.UI.addClickListener
import com.lyeeedar.Util.getXml
import ktx.actors.onClick
import ktx.scene2d.scrollPane
import ktx.scene2d.slider
import ktx.scene2d.table
import ktx.scene2d.textButton

class MusicManagerScreen : AbstractScreen()
{
	init
	{
		instance = this
	}

	var exitThread = false

	val allSoundScapes: Array<String> by lazy { loadSoundScapeList() }

	fun loadSoundScapeList() : Array<String>
	{
		val out = Array<String>()

		val soundScapesXml = getXml("SoundScapes/SoundScapeList.xml")

		for (el in soundScapesXml.children())
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

			val s = slider(0f, 1f, 0.05f, false, "default-horizontal", Global.skin) {  cell -> cell.pad(10f, 10f, 0f, 10f).growX()
				value = 1f
				addListener(object : ChangeListener() {
					override fun changed(event: ChangeEvent?, actor: Actor?)
					{
						synchronized(this@MusicManagerScreen)
						{
							currentSoundScape?.volume = value
						}
					}
				})
			}

			row()

			val scroll = scrollPane("default", Global.skin) { cell -> cell.growX()

				table {
					for (soundScape in allSoundScapes)
					{
						 textButton(soundScape, "default", Global.skin) { cell -> cell.width(150f).pad(15f)
							onClick { inputEvent, kTextButton ->
								synchronized(this@MusicManagerScreen)
								{
									queuedSoundScape = loadSoundScape(soundScape)
									queuedSoundScape!!.volume = currentSoundScape?.volume ?: s.value
									fillSoundTable(queuedSoundScape!!, soundScape)
								}
							}
						 }
					}
				}
			}
			scroll.setForceScroll(true, false)
			scroll.setScrollingDisabled(false, true)
			scroll.setFadeScrollBars(false)

			row()

			add(Seperator(Global.skin, false)).pad(0f, 10f, 10f, 10f).growX()

			row()

			add(soundScapeTable).grow()
		}

		mainTable.add(t).grow()
		//t.setFillParent(true)
	}

	fun fillSoundTable(soundScape: SoundScape, name: String)
	{
		soundScapeTable.clear()

		soundScapeTable.add(Label(name, Global.skin, "title")).pad(10f)
		soundScapeTable.row()

		val levelsTables = Table()
		val levelsCollapser = CollapsibleWidget(levelsTables)

		val presetTable = Table()
		for (preset in soundScape.presets)
		{
			val button = TextButton(preset.name, Global.skin)
			button.onClick { inputEvent, textButton ->
				synchronized(this@MusicManagerScreen)
				{
					soundScape.applyPreset(preset)
				}
				fillSoundTable(soundScape, name)
			}
			presetTable.add(button).width(150f).pad(5f)
		}

		val presetScroll = ScrollPane(presetTable)
		presetScroll.setForceScroll(true, false)
		presetScroll.setScrollingDisabled(false, true)
		presetScroll.setFadeScrollBars(false)

		levelsTables.add(presetScroll).growX()
		levelsTables.row()

		for (layer in soundScape.layers)
		{
			val entry = Table()

			entry.add(Label(layer.name, Global.skin)).width(Value.percentWidth(0.3f, soundScapeTable))

			val check = CheckBox("Enabled", Global.skin)
			check.isChecked = layer.enabled
			check.addListener(object : ChangeListener() {
				override fun changed(event: ChangeEvent?, actor: Actor?)
				{
					synchronized(this@MusicManagerScreen)
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
				}
			})

			entry.add(check).width(Value.percentWidth(0.3f, soundScapeTable)).right()

			val slider = Slider(0f, 1f, 0.05f, false, Global.skin)
			slider.value = layer.volume
			slider.addListener(object : ChangeListener() {
				override fun changed(event: ChangeEvent?, actor: Actor?)
				{
					synchronized(this@MusicManagerScreen)
					{
						layer.volume = slider.value
						layer.changeVolume(1f)
					}
				}
			})
			entry.add(slider).width(Value.percentWidth(0.4f, soundScapeTable)).growX()

			levelsTables.add(entry).expandX().left()
			levelsTables.row()
		}

		val levelsButton = TextButton("Levels", Global.skin)
		var showLevels = true
		levelsButton.addClickListener {
			showLevels = !showLevels

			levelsCollapser.setCollapsed(!showLevels, true)
		}

		soundScapeTable.add(levelsButton).growX()
		soundScapeTable.row()
		soundScapeTable.add(levelsCollapser).width(Value.percentWidth(1.0f, soundScapeTable))
		soundScapeTable.row()

		soundScapeTable.add(Label("One Shots", Global.skin)).pad(10f)
		soundScapeTable.row()

		val oneShotsTable = Table()
		for (oneShot in soundScape.oneShots)
		{
			val button = TextButton(oneShot.name, Global.skin)
			button.addClickListener {
				oneShot.play()
			}
			oneShotsTable.add(button).growX()
			oneShotsTable.row()
		}

		val oneShotsScroll = ScrollPane(oneShotsTable)
		oneShotsScroll.setForceScroll(false, true)
		oneShotsScroll.setScrollingDisabled(true, false)
		oneShotsScroll.setFadeScrollBars(false)

		soundScapeTable.add(oneShotsScroll)
		soundScapeTable.row()

		soundScapeTable.add(Table()).expand()
	}

	override fun doRender(delta: Float)
	{



	}

	var hasThread = false
	fun launchThread()
	{
		if (hasThread) throw RuntimeException("Already has thread!")
		hasThread = true

		object : Thread()
		{
			private var startTime: Long = 0
			override fun run()
			{
				while (true)
				{
					val delta = (System.currentTimeMillis() - startTime).toFloat() / 1000.0f
					startTime = System.currentTimeMillis()

					synchronized(this@MusicManagerScreen)
					{
						if (exitThread)
						{
							return
						}

						if (queuedSoundScape != null)
						{
							if (currentSoundScape?.complete() != false)
							{
								currentSoundScape?.dispose()

								currentSoundScape = queuedSoundScape
								queuedSoundScape = null

								currentSoundScape!!.create()
								currentSoundScape!!.play()
							}
						}

						currentSoundScape?.update(delta)
					}
				}
			}
		}.start()
	}

	fun loadSoundScape(name: String): SoundScape
	{
		val xml = getXml("SoundScapes/$name")

		val soundScape = SoundScape(name)
		soundScape.parse(xml)

		if (soundScape.presets.size > 0) soundScape.applyPreset(soundScape.presets.first())

		return soundScape
	}

	companion object
	{
		var instance: MusicManagerScreen? = null

		fun launchThread()
		{
			object : Thread()
			{
				override fun run()
				{
					while (true)
					{
						if (instance != null)
						{
							instance!!.launchThread()
							break
						}
					}
				}
			}.start()
		}
	}
}