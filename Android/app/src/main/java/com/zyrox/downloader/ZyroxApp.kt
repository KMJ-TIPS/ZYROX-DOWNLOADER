package com.zyrox.downloader

import android.app.Application
import com.zyrox.downloader.core.DownloadRepository
import com.zyrox.downloader.core.Engine
import com.zyrox.downloader.core.UpdateManager
import com.zyrox.downloader.service.DownloadService
import com.zyrox.downloader.service.UpdateService
import kotlinx.coroutines.launch

class ZyroxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Notification channels pehle
        DownloadService.createChannel(this)
        UpdateService.createChannel(this)
        // Repository (queue/history restore)
        DownloadRepository.initOnce(this)
        // Engine init + silent update check — background thread
        DownloadRepository.appScope.launch {
            Engine.init(this@ZyroxApp)
            UpdateManager.checkOnLaunch(this@ZyroxApp)
        }
    }
}
