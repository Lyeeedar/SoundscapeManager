package com.lyeeedar.Util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.utils.XmlReader.Element
import java.util.*

class AssetManager
{
	companion object
	{
		private val loadedFonts = HashMap<String, BitmapFont>()

		@JvmOverloads fun loadFont(name: String, size: Int, colour: Color = Color.WHITE, borderWidth: Int = 1, borderColour: Color = Color.BLACK, shadow: Boolean = false): BitmapFont?
		{
			val key = name + size + colour.toString() + borderWidth + borderColour.toString()

			if (loadedFonts.containsKey(key))
			{
				return loadedFonts[key]
			}

			val fgenerator = FreeTypeFontGenerator(Gdx.files.internal(name))
			val parameter = FreeTypeFontParameter()
			parameter.size = size
			parameter.borderWidth = borderWidth.toFloat()
			parameter.kerning = true
			parameter.borderColor = borderColour
			parameter.borderStraight = true
			parameter.color = colour

			if (shadow)
			{
				parameter.shadowOffsetX = -1
				parameter.shadowOffsetY = 1
			}

			val font = fgenerator.generateFont(parameter)
			font.data.markupEnabled = true
			fgenerator.dispose() // don't forget to dispose to avoid memory leaks!

			loadedFonts.put(key, font)

			return font
		}

		private val loadedSounds = HashMap<String, Sound?>()

		fun loadSound(path: String): Sound?
		{
			if (loadedSounds.containsKey(path))
			{
				return loadedSounds[path]
			}

			var file = Gdx.files.internal("Sounds/$path.mp3")
			if (!file.exists())
			{
				file = Gdx.files.internal("Sounds/$path.ogg")

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

		private val prepackedAtlas = TextureAtlas(Gdx.files.internal("Atlases/SpriteAtlas.atlas"))

		private val loadedTextureRegions = HashMap<String, TextureRegion?>()

		fun loadTextureRegion(path: String): TextureRegion?
		{
			if (loadedTextureRegions.containsKey(path))
			{
				return loadedTextureRegions[path]
			}

			var atlasName = path
			if (atlasName.startsWith("Sprites/")) atlasName = atlasName.replaceFirst("Sprites/".toRegex(), "")
			atlasName = atlasName.replace(".png", "")

			val region = prepackedAtlas.findRegion(atlasName)
			if (region != null)
			{
				val textureRegion = TextureRegion(region)
				loadedTextureRegions.put(path, textureRegion)
				return textureRegion
			}
			else
			{
				loadedTextureRegions.put(path, null)
				return null
			}
		}

		private val loadedTextures = HashMap<String, Texture?>()

		fun loadTexture(path: String, filter: TextureFilter = TextureFilter.Linear, wrapping: Texture.TextureWrap = Texture.TextureWrap.ClampToEdge): Texture?
		{
			if (loadedTextures.containsKey(path))
			{
				return loadedTextures[path]
			}

			val file = Gdx.files.internal(path)
			if (!file.exists())
			{
				loadedTextures.put(path, null)
				return null
			}

			val region = Texture(path)
			region.setFilter(filter, filter)
			region.setWrap(wrapping, wrapping)
			loadedTextures.put(path, region)

			return region
		}

		fun loadColour(stringCol: String, colour: Colour = Colour()): Colour
		{
			val cols = stringCol.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			colour.r = java.lang.Float.parseFloat(cols[0]) / 255.0f
			colour.g = java.lang.Float.parseFloat(cols[1]) / 255.0f
			colour.b = java.lang.Float.parseFloat(cols[2]) / 255.0f
			colour.a = if (cols.size > 3) cols[3].toFloat() / 255.0f else 1f

			return colour
		}

		fun loadColour(xml: Element): Colour
		{
			return loadColour(xml.text)
		}
	}
}