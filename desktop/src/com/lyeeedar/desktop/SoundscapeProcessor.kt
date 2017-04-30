package com.lyeeedar.desktop

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import java.io.File

class SoundscapeProcessor
{
	val files = Array<String>()

	init
	{
		findFilesRecursive(File("SoundScapes"))

		var out = "<SoundScapes>"

		for (file in files)
		{
			out += "\t<SoundScape>$file</SoundScape>"
		}

		out += "</SoundScapes>"

		val outHandle = FileHandle(File("SoundScapes/SoundScapeList.xml"))
		outHandle.writeString(out, false)
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
				var path = file.path
				path = path.replace( "\\", "/" )
				path = path.replace( "SoundScapes/", "" )
				path = path.replace( ".xml", "" )

				files.add(path)
			}
		}
	}
}