package net.raxander.photogallery.api

import com.google.gson.annotations.SerializedName
import net.raxander.photogallery.GalleryItem

class PhotoResponse{
    @SerializedName("photo") lateinit var galleryItems: List<GalleryItem>
}