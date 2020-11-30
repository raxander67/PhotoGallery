package net.raxander.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel:ViewModel() {
    private val flickrFetchr= FlickrFetchr()
    val galleryItemLiveData : LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData=FlickrFetchr().fetchPhotos()
    }

    override fun onCleared() {
        super.onCleared()
        flickrFetchr.cancelRequestInFlight()
    }
}