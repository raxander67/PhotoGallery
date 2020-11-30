package net.raxander.photogallery.api

import net.raxander.photogallery.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {
    @GET("services/rest" +
            "/?method=flickr.interestingness.getList&api_key=" +
            apiPhotoGallery+
            "&format=json&nojsoncallback=1&extras=url_s")
    fun fetchPhotos(): Call<FlickrResponce>

    @GET
    fun fetchUrlBytes(@Url url:String):Call<ResponseBody>
companion object{
    const val apiPhotoGallery = BuildConfig.PHOTOGALLERY_API
}
}