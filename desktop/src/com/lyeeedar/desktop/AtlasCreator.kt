package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import java.io.File

/**
 * Created by Philip on 17-Jan-16.
 */
class AtlasCreator
{
	private val packer: TexturePacker

	private val packedPaths = ObjectSet<String>()

	init
	{
		val settings = TexturePacker.Settings()
		settings.combineSubdirectories = true
		settings.duplicatePadding = true
		settings.maxWidth = 2048
		settings.maxHeight = 2048
		settings.paddingX = 4
		settings.paddingY = 4
		settings.useIndexes = false
		settings.filterMin = Texture.TextureFilter.MipMapLinearLinear
		settings.filterMag = Texture.TextureFilter.MipMapLinearLinear

		packer = TexturePacker(File("Sprites"), settings)

		parseCodeFilesRecursive(File("../../core/src").absoluteFile)

		val outDir = File("Atlases")
		val contents = outDir.listFiles()
		if (contents != null)
			for (file in contents)
			{
				if (file.path.endsWith(".png"))
				{
					file.delete()
				}
				else if (file.path.endsWith(".atlas"))
				{
					file.delete()
				}
			}

		packer.pack(outDir, "SpriteAtlas")
	}

	private fun parseCodeFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				parseCodeFilesRecursive(file)
			}
			else
			{
				parseCodeFile(file.path)
			}
		}
	}

	private fun parseCodeFile(file: String)
	{
		val contents = File(file).readText()
		val regex = Regex("AssetManager.loadSprite\\(\".*?\"")//(\".*\")")

		val occurances = regex.findAll(contents)

		for (occurance in occurances)
		{
			var path = occurance.value
			path = path.replace("AssetManager.loadSprite(\"", "")
			path = path.replace("\"", "")

			processSprite(path)
		}

		val regex2 = Regex("AssetManager.loadTextureRegion\\(\".*?\"")//(\".*\")")

		val occurances2 = regex2.findAll(contents)

		for (occurance in occurances2)
		{
			var path = occurance.value
			path = path.replace("AssetManager.loadTextureRegion(\"", "")
			path = path.replace("\"", "")

			processSprite(path)
		}
	}

	private fun tryPackSprite(element: XmlReader.Element): Boolean
	{
		val name = element.get("Name")
		val exists = tryPackSprite(name)
		if (!exists)
		{
			System.err.println("Could not find sprites with name: " + name)
			return false
		} else
		{
			println("Added sprites for name: " + name)
			return true
		}
	}

	private fun tryPackSprite(name: String): Boolean
	{
		var path = name
		if (!path.startsWith("Sprites/")) path = "Sprites/" + path
		if (!path.endsWith(".png")) path += ".png"

		if (packedPaths.contains(path))
		{
			return true
		}

		val handle = Gdx.files.internal(path)

		if (handle.exists())
		{
			packer.addImage(handle.file())
			packedPaths.add(path)
			return true
		}
		else
		{
			return false
		}
	}

	private fun processSprite(spriteElement: XmlReader.Element): Boolean
	{
		val name = spriteElement.get("Name", null) ?: return true

		return processSprite(name)
	}

	private fun processSprite(name: String): Boolean
	{
		var foundCount = 0

		// Try 0 indexed sprite
		var i = 0
		while (true)
		{
			val exists = tryPackSprite(name + "_" + i)
			if (!exists)
			{
				break
			} else
			{
				foundCount++
			}

			i++
		}

		// Try 1 indexed sprite
		if (foundCount == 0)
		{
			i = 1
			while (true)
			{
				val exists = tryPackSprite(name + "_" + i)
				if (!exists)
				{
					break
				} else
				{
					foundCount++
				}

				i++
			}
		}

		// Try sprite without indexes
		if (foundCount == 0)
		{
			val exists = tryPackSprite(name)
			if (exists)
			{
				foundCount++
			}
		}

		if (foundCount == 0)
		{
			System.err.println("Could not find sprites with name: " + name)
		} else
		{
			println("Added sprites for name: " + name)
		}

		return foundCount > 0
	}
}
