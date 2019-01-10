package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Util.getRawXml
import java.io.File

class SoundscapeProcessor
{
	val audioFiles = ObjectSet<String>()
	init
	{
		// clear data
		val soundsFolder = File("Sounds")
		if (soundsFolder.exists())
		{
			for (file in soundsFolder.list())
			{
				val deleted = File("Sounds/$file").deleteRecursively()
				if (!deleted)
				{
					error("Failed to delete file $file!")
				}
			}
		}

		val musicFolder = File("Music")
		if (musicFolder.exists())
		{
			for (file in musicFolder.list())
			{
				val deleted = File("Music/$file").deleteRecursively()
				if (!deleted)
				{
					error("Failed to delete file $file!")
				}
			}
		}

		findFilesRecursive(File("../assetsraw/SoundScapes"))

		for (path in audioFiles)
		{
			val file = File("../assetsraw/$path")
			file.copyTo(File(path))

			System.out.println("Copied $path")
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
			else if (file.path.endsWith(".xml") && !file.path.contains("SoundScapeList"))
			{
				processFile(file)
			}
		}
	}

	fun processFile(file: File)
	{
		val xml = getRawXml(file.path)

		val occurances = xml.getChildrenByNameRecursively("File")

		fun tryWriteFile(path: String): Boolean
		{
			if (audioFiles.contains(path)) return true

			if (!Gdx.files.local("../assetsraw/$path").exists()) return false

			audioFiles.add(path)

			return true
		}

		for (occurance in occurances)
		{
			var found = tryWriteFile("Music/"+occurance.text+".ogg")
			if (!found) found = tryWriteFile("Sounds/"+occurance.text+".ogg")

			if (!found) throw Exception("Invalid music or sound '" + occurance.text + "' in file '" + file.path)
		}
	}
}