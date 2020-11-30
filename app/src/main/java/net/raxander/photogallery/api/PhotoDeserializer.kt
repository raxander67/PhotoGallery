package net.raxander.photogallery.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.raxander.photogallery.GalleryItem
import java.lang.reflect.Type

class PhotoDeserializer: JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse? {
        // Вытяните объект фотографий из JsonElement
        // и преобразуйте его в объект PhotoResponse
        //JsonElement, JsonObject и Gson
        val photoJsonObj= json?.asJsonObject?.get("photo")?.asJsonArray
        var photoItem :MutableList<GalleryItem>
        if (photoJsonObj != null) {
            for (photo in photoJsonObj){
                photo
            }
        }
        return null
    }

}