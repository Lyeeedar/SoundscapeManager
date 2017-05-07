package com.lyeeedar.Util

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ArchiveFileHandle : FileHandle
{
	internal val archive: ZipFile
	internal val archiveEntry: ZipEntry?

	constructor(archive: ZipFile, file: File) : super(file, FileType.Classpath)
	{
		this.archive = archive
		archiveEntry = this.archive.getEntry(file.getPath())
	}

	constructor(archive: ZipFile, fileName: String) : super(fileName.replace('\\', '/'), FileType.Classpath)
	{
		this.archive = archive
		this.archiveEntry = archive.getEntry(fileName.replace('\\', '/'))
	}

	override fun child(name: String): FileHandle
	{
		var name = name
		name = name.replace('\\', '/')
		if (file.path.isEmpty()) return ArchiveFileHandle(archive, File(name))
		return ArchiveFileHandle(archive, File(file, name))
	}

	override fun sibling(name: String): FileHandle
	{
		var name = name
		name = name.replace('\\', '/')
		if (file.path.isEmpty()) throw GdxRuntimeException("Cannot get the sibling of the root.")
		return ArchiveFileHandle(archive, File(file.parent, name))
	}

	override fun parent(): FileHandle
	{
		var parent: File? = file.parentFile
		if (parent == null)
		{
			if (type == FileType.Absolute)
				parent = File("/")
			else
				parent = File("")
		}
		return ArchiveFileHandle(archive, parent)
	}

	override fun read(): InputStream
	{
		try
		{
			return archive.getInputStream(archiveEntry)
		}
		catch (e: IOException)
		{
			throw GdxRuntimeException("File not found: $file (Archive)")
		}

	}

	override fun exists(): Boolean
	{
		return archiveEntry != null
	}

	override fun length(): Long
	{
		return archiveEntry!!.getSize()
	}

	override fun lastModified(): Long
	{
		return archiveEntry!!.getTime()
	}
}

fun ZipFile.getHandle(name: String): ArchiveFileHandle
{
	return ArchiveFileHandle(this, name)
}