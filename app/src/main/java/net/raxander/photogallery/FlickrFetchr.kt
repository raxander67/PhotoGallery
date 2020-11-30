package net.raxander.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.raxander.photogallery.api.FlickrApi
import net.raxander.photogallery.api.FlickrResponce
import net.raxander.photogallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class FlickrFetchr {
    private val flickrApi: FlickrApi
    private lateinit var flickrCall: Call<FlickrResponce>
    init {
        val retrofit= Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi =retrofit.create(FlickrApi::class.java)
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap?{
        val response: Response<ResponseBody> =flickrApi.fetchUrlBytes(url).execute()
        val bitmap=response.body()?.byteStream()?.use (BitmapFactory::decodeStream)
        Log.d("M_FlickrFetchr","Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
    fun fetchPhotos():LiveData<List<GalleryItem>>{
        val responseLiveData:MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<FlickrResponce> = flickrApi.fetchPhotos()

        /* Функция Call.enqueue(...) выполняет веб-запрос, находящийся в объекте Call.
            Самое главное, что запрос выполняется в фоновом потоке. Retrofit управляет
            фоновым потоком самостоятельно, и вам не нужно об этом думать.*/
        flickrRequest.enqueue(object : Callback<FlickrResponce> {
            /*Объект Callback позволяет определить, что вы хотите сделать после того,
            как будет получен ответ на запрос.*/
            override fun onFailure(call: Call<FlickrResponce>, t: Throwable) {
                Log.e("M_FlickrFetchr","Failed to photos", t)
            }

            override fun onResponse(call: Call<FlickrResponce>, response: Response<FlickrResponce>) {
                Log.d("M_FlickrFetchrt","Response received")
                val flickrResponce:FlickrResponce?=response.body()
                val photoResponce: PhotoResponse? =flickrResponce?.photos
                var galleryItems: List<GalleryItem> =photoResponce?.galleryItems ?: mutableListOf()
                galleryItems=galleryItems.filterNot { it.url.isBlank() }
                responseLiveData.value=galleryItems
            }
        })
        return responseLiveData
    }

    fun cancelRequestInFlight(){
        if(:: flickrCall.isInitialized) flickrCall.cancel()
        Log.d("M_FlickrFetchr","cancelRequestInFlight")
    }
}