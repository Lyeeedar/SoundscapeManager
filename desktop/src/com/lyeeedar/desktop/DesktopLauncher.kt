package com.lyeeedar.desktop

import com.lyeeedar.Global
import com.lyeeedar.MainGame
import java.awt.MouseInfo
import java.awt.Robot



object DesktopLauncher
{
	@JvmStatic fun main(arg: Array<String>)
	{
		Global.game = MainGame()
		Global.applicationChanger = LwjglApplicationChanger()
		Global.applicationChanger.createApplication()

		// Keep screen alive
		val hal = Robot()
		while (true)
		{
			hal.delay(1000 * 30)
			var pObj = MouseInfo.getPointerInfo().location
			println(pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y)
			hal.mouseMove(pObj.x + 1, pObj.y + 1)
			hal.mouseMove(pObj.x - 1, pObj.y - 1)
			pObj = MouseInfo.getPointerInfo().location
			println(pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y)
		}
	}
}
