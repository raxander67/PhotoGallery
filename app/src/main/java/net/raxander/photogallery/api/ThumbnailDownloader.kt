package net.raxander.photogallery.api

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import net.raxander.photogallery.FlickrFetchr
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

/**
 * мы создадим фоновый поток. Этот поток будет получать и пооче-
 * редно обрабатывать запросы на загрузку, а также предоставлять результирующее
 * изображение для каждого отдельного запроса по мере завершения загрузки.
 * единственной целью ThumbnailDownloader является загрузка и
 * передача изображений в PhotoGalleryFragment.
 * Пользователю ThumbnailDownloader понадобится объект для идентификации
 * каждой загрузки и определения элемента пользовательского интерфейса,
 * который должен обновляться после завершения загрузки.
 * Вместо того чтобы ограничивать пользователя одним
 * конкретным типом объекта, мы используем обобщенный параметр и сделаем
 * реализацию более гибкой.
 */
class ThumbnailDownloader<in T>(
    private val responseHandler:Handler,
    private val onThumbnailDownloaded:(T, Bitmap)->Unit
) : HandlerThread(TAG) {

    val fragmentLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Starting background thread")
                start()
                looper
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()
            }
        }

    val viewLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }
    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        responseHandler.post(Runnable {
            if(requestMap[target] != url || hasQuit){
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    /**
     * Функция queueThumbnail() ожидает получить объект типа T, выполняющий
     * функции идентификатора загрузки, и String с URL-адресом для загрузки. Эта
     * функция будет вызываться PhotoAdapter в его реализации onBindViewHolder(...).
     */
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "queueThumbnail: Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }



}