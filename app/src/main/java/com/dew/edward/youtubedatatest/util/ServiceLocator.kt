package com.dew.edward.youtubedatatest.util

import android.app.Application
import android.content.Context
import android.support.annotation.VisibleForTesting
import com.dew.edward.youtubedatatest.api.YoutubeAPI
import com.dew.edward.youtubedatatest.repository.InMemoryByPageKeyedRepository
import com.dew.edward.youtubedatatest.repository.YoutubePostRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Created by Edward on 6/21/2018.
 */

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun getInstance(context: Context): ServiceLocator {
            instance ?: synchronized(LOCK) {
                instance ?: DefaultServiceLocator(app = context.applicationContext as Application,
                        useInMemoryDb = false).also { instance = it }
            }
            return instance!!
        }

        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(type: YoutubePostRepository.Type): YoutubePostRepository
    fun getNetworkExecutor(): Executor
    fun getDiskIOExecutor(): Executor
    fun getYoutubeAPi(): YoutubeAPI
}

class DefaultServiceLocator(val app: Application,
                                 val useInMemoryDb: Boolean) : ServiceLocator {

    private val DISK_IO = Executors.newSingleThreadExecutor()
    private val NETWORK_IO = Executors.newFixedThreadPool(5)
    //    private val  db by lazy {  }
    private val api by lazy { YoutubeAPI.create() }

    override fun getRepository(type: YoutubePostRepository.Type): YoutubePostRepository {
         when (type) {
            YoutubePostRepository.Type.IN_MEMORY ->
                return InMemoryByPageKeyedRepository(
                    youtubeApi = getYoutubeAPi(),
                    networkExecutor = getNetworkExecutor()
            )

            YoutubePostRepository.Type.DB ->
                return InMemoryByPageKeyedRepository(
                    youtubeApi = getYoutubeAPi(),
                    networkExecutor = getNetworkExecutor()
            )

        }
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getYoutubeAPi(): YoutubeAPI = api
}