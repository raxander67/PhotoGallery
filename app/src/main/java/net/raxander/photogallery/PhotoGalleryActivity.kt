package net.raxander.photogallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class PhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)
        Log.d("M_PhotoGalleryActivity","onCreate")
        val isFragmentContainerEmpty=savedInstanceState==null
        if(isFragmentContainerEmpty){
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_Container,PhotoGalleryFragment.newInstance())
                .commit()
        }
    }
}