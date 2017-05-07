package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.lyeeedar.Util.getXml
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SoundscapeProcessor
{
	init
	{
		findFilesRecursive(File("SoundScapes"))
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
		val xml = getXml(file.path)

		val occurances = xml.getChildrenByNameRecursively("File")

		val zipFile = File("SoundScapes/" + file.nameWithoutExtension + ".zip")
		val out = ZipOutputStream(FileOutputStream(zipFile))

		fun tryWriteFile(path: String, overrideEntryName: String? = null): Boolean
		{
			if (!Gdx.files.local(path).exists()) return false

			val dataBytes = Gdx.files.local(path).file().readBytes()

			val name = overrideEntryName ?: path

			val e = ZipEntry(name)
			out.putNextEntry(e)

			out.write(dataBytes, 0, dataBytes.size)
			out.closeEntry()

			return true
		}

		tryWriteFile("SoundScapes/" + file.nameWithoutExtension + ".xml", "SoundScape.xml")

		for (occurance in occurances)
		{
			var found = tryWriteFile("Music/"+occurance.text+".ogg")
			if (!found) found = tryWriteFile("Sounds/"+occurance.text+".ogg")

			if (!found) throw Exception("Invalid music or sound '" + occurance.text + "' in file '" + file.path)
		}

		out.close()
	}
}