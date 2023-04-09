package com.example.alphaacemobile.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alphaacemobile.R
import com.example.alphaacemobile.fragment.HomeFragment
import com.example.alphaacemobile.model.HomeFavoriteModel
import com.example.alphaacemobile.model.HomeNewsModel
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class HomeNewsAdapter(
    private val homeNewsList: ArrayList<HomeNewsModel>,
    val listener: HomeFragment
) : RecyclerView.Adapter<HomeNewsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val newsImage = itemView.findViewById<ImageView>(R.id.ivHomeNewsImage)
        val newsToken = itemView.findViewById<ImageView>(R.id.ivHomeNewsToken)
        val newsDescription = itemView.findViewById<TextView>(R.id.tvHomeNewsDescription)
        val newsAuthor = itemView.findViewById<TextView>(R.id.tvHomeNewsAuthor)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                listener.HomeNewsOnClick(position)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_news_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = homeNewsList[position]
        holder.apply {
            // Declaring executor to parse the URL
            val executor = Executors.newSingleThreadExecutor()

            // Once the executor parses the URL
            // and receives the image, handler will load it
            // in the ImageView
            val handler = Handler(Looper.getMainLooper())

            // Initializing the image
            var image: Bitmap? = null

            // Only for Background process (can take time depending on the Internet speed)
            executor.execute {

                // Image URL
                val imageURL = item.newsImage

                // Tries to get the image and post it in the ImageView
                // with the help of Handler
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                    // Only for making changes in UI
                    handler.post {
                        newsImage.setImageBitmap(image)
                    }
                }

                // If the URL does not point to
                // image or any other kind of failure
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Declaring executor to parse the URL
            val executor2 = Executors.newSingleThreadExecutor()

            // Once the executor parses the URL
            // and receives the image, handler will load it
            // in the ImageView
            val handler2 = Handler(Looper.getMainLooper())

            // Initializing the image
            var image2: Bitmap? = null

            // Only for Background process (can take time depending on the Internet speed)
            executor2.execute {

                // Image URL
                val imageURL = item.tokenImage

                // Tries to get the image and post it in the ImageView
                // with the help of Handler
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image2 = BitmapFactory.decodeStream(`in`)
                    // Only for making changes in UI
                    handler.post {
                        newsToken.setImageBitmap(image2)
                    }
                }

                // If the URL does not point to
                // image or any other kind of failure
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            newsDescription.text = item.description
            newsAuthor.text = item.author
        }
    }

    override fun getItemCount(): Int {
        return homeNewsList.size
    }
}