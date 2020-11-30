package net.raxander.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.raxander.photogallery.api.ThumbnailDownloader

class PhotoGalleryFragment: Fragment() {
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        * Обычно следует избегать сохранения фрагментов. Мы будем делать это только
        * здесь, потому что сохранение фрагмента упрощает реализацию и позволяет нам
        * сконцентрироваться на изучении того, как работает HandlerThread.
        * */
        retainInstance=true

        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
        val responseDownloader= Handler()
        thumbnailDownloader= ThumbnailDownloader(responseDownloader){ photoHolder, bitmap->
            val drawable=BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
        val view=inflater.inflate(R.layout.fragment_photo_gallery,container,false)

        photoRecyclerView=view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager=GridLayoutManager(context,3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems->
                photoRecyclerView.adapter=PhotoAdapter(galleryItems)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    private class PhotoHolder(itemImageView: ImageView): RecyclerView.ViewHolder(itemImageView){
        val bindDrawable: (Drawable)->Unit=itemImageView::setImageDrawable
    }

    private inner class PhotoAdapter(private  val galleryItems: List<GalleryItem>): RecyclerView.Adapter<PhotoHolder>(){

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem=galleryItems[position]
            val placeholder: Drawable=ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?:ColorDrawable()
            /*
             если ContextCompat.getDrawable(...) возвращает null,
             мы передаём пустой объект ColorDrawable
            */
            holder.bindDrawable(placeholder)

            thumbnailDownloader.queueThumbnail(holder,galleryItem.url)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(view)
        }

    }

    companion object{
        fun newInstance(): PhotoGalleryFragment {
            /*Log.d("M_PhotoGalleryFragment","newInstance")*/
            return PhotoGalleryFragment()
        }
    }
}