package com.lyeeedar

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.lyeeedar.Screens.MusicManagerScreen




class AndroidService : Service()
{
	override fun onBind(p0: Intent?): IBinder?
	{
		return null
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (intent != null)
		{
			MusicManagerScreen.launchThread()

			if (!hasThread)
			{
				hasThread = true

				object : Thread()
				{
					override fun run()
					{
						while (true)
						{
							if (exitThread)
							{
								exitThread = false
								hasThread = false
								return
							}

							updateNotification()
							sleep(5000)
						}
					}
				}.start()
			}

			updateNotification()
		}

		return Service.START_REDELIVER_INTENT
	}

	private fun updateNotification()
	{
		val currentSoundScape = MusicManagerScreen.instance?.currentSoundScape?.name ?: ""

		val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
		val mBuilder = NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.notificationicon)
			.setLargeIcon(bitmap)
			.setContentTitle("Soundscape Manager")
			.setContentText(currentSoundScape)
			.setColor(ContextCompat.getColor(this, R.color.notification_colour))
			.setOngoing(true)

		val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build())
	}

	override fun onTaskRemoved(rootIntent: Intent?)
	{
		val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		mNotificationManager.cancel(NOTIFICATION_ID)

		exitThread = true
		MusicManagerScreen.instance!!.exitThread = true

		stopSelf()
	}

	private var hasThread = false
	private var exitThread = false
	private val NOTIFICATION_ID = 1
}