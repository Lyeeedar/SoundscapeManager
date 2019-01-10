package com.lyeeedar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.badlogic.gdx.backends.android.AndroidApplication2
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration







class AndroidLauncher : AndroidApplication2()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		//Fabric.with(this, Crashlytics())

		val config = AndroidApplicationConfiguration()
		config.resolutionStrategy.calcMeasures(360, 640)
		config.disableAudio = false
		config.useWakelock = true

		//Global.android = true
		Global.game = MainGame()

		initialize(Global.game, config)

		Global.applicationChanger = AndroidApplicationChanger()
		Global.applicationChanger.updateApplication(Global.applicationChanger.prefs)

		val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
		val wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock")
		wakeLock.acquire()

		startService(Intent(this, AndroidService::class.java))
	}
}
