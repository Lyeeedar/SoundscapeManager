package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.getChildrenByAttributeRecursively
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

/**
 * Created by Philip on 17-Jan-16.
 */
class AtlasCreator
{
	private val packedPaths = ObjectSet<String>()
	private val localGeneratedImages = ObjectMap<String, BufferedImage>()

	init
	{
		findFilesRecursive(File("../assetsraw").absoluteFile)
		parseCodeFilesRecursive(File("../../core/src").absoluteFile)

		var doPack = true
		val thisHash = packedPaths.sorted().joinToString().hashCode()

		val cacheFile = File("../assetsraw/atlasPackCache")
		if (cacheFile.exists())
		{
			val cacheHash = cacheFile.readText().toInt()

			if (cacheHash == thisHash)
			{
				System.out.println("Atlas identical, no work to be done.")
				//doPack = false
			}
		}

		if (doPack)
		{
			val outDir = File("../assetsraw/Atlases")
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

			val packer = TexturePacker(File("../assetsraw/Sprites"), settings)

			for (path in packedPaths)
			{
				val file = File("../assetsraw/$path")
				if (file.exists())
				{
					packer.addImage(File("../assetsraw/$path"))
				}
				else
				{
					val local = localGeneratedImages[path] ?: error("Failed to pack $path")

					packer.addImage(local, path)
				}
			}

			packer.pack(outDir, "SpriteAtlas")

			cacheFile.writeText(thisHash.toString())
		}
	}

	private fun findFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				findFilesRecursive(file)
			}
			else if (file.path.endsWith(".xml"))
			{
				parseXml(file.path)
			}
		}
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

	private fun parseXml(file: String)
	{
		val reader = XmlReader()
		var xml: XmlReader.Element? = null

		try
		{
			xml = reader.parse(Gdx.files.internal(file))
		} catch (e: Exception)
		{
			return
		}

		if (xml == null)
		{
			return
		}

		val spriteElements = Array<XmlReader.Element>()

		spriteElements.addAll(xml.getChildrenByAttributeRecursively("meta:RefKey", "Sprite"))
		spriteElements.addAll(xml.getChildrenByAttributeRecursively("RefKey", "Sprite"))

		for (el in spriteElements)
		{
			val found = processSprite(el)
			if (!found)
			{
				throw RuntimeException("Failed to find sprite for file: " + file)
			}
		}

		val particleElements = xml.getChildrenByNameRecursively("TextureKeyframes")

		for (el in particleElements)
		{
			val succeed = processParticle(el)
			if (!succeed)
			{
				throw RuntimeException("Failed to process particle in file: " + file)
			}
		}
	}

	private fun processParticle(xml: XmlReader.Element) : Boolean
	{
		val streamsEl = xml.getChildrenByName("Stream")
		if (streamsEl.size == 0)
		{
			return processParticleStream(xml)
		}
		else
		{
			for (el in streamsEl)
			{
				if (!processParticleStream(el)) return false
			}
		}

		return true
	}

	private fun processParticleStream(xml: XmlReader.Element) : Boolean
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			var path: String

			if (el.text != null)
			{
				val split = el.text.split("|")
				path = split[1]
			}
			else
			{
				path = el.get("Value")
			}

			val found = processSprite(path)
			if (!found) return false
		}

		return true
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

		val handle = Gdx.files.internal("../assetsraw/$path")

		if (handle.exists())
		{
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

	companion object
	{
		fun <T> powerSet(originalSet: Set<T>): Set<Set<T>>
		{
			val sets = HashSet<Set<T>>()
			if (originalSet.isEmpty())
			{
				sets.add(HashSet<T>())
				return sets
			}
			val list = ArrayList(originalSet)
			val head = list[0]
			val rest = HashSet(list.subList(1, list.size))
			for (set in powerSet(rest))
			{
				val newSet = HashSet<T>()
				newSet.add(head)
				newSet.addAll(set)
				sets.add(newSet)
				sets.add(set)
			}
			return sets
		}
	}
}
