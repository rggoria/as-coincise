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
import com.example.alphaacemobile.fragment.TokenDetailFragment
import com.example.alphaacemobile.model.HomeNewsModel
import com.example.alphaacemobile.model.TokenDetailRelatedNewsModel
import java.util.concurrent.Executors


@Suppress("DEPRECATION")
class TokenDetailRelatedNewsAdapter(
    private val tokenDetailRelatedNewsList: ArrayList<TokenDetailRelatedNewsModel>,
    val listener: TokenDetailFragment
) : RecyclerView.Adapter<TokenDetailRelatedNewsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val newsImage = itemView.findViewById<ImageView>(R.id.ivTokenDetailRelatedNews)
        val newsDescription = itemView.findViewById<TextView>(R.id.tvTokenDetailRelatedNewsDescription)
        val newsAuthor = itemView.findViewById<TextView>(R.id.tvTokenDetailRelatedNewsAuthor)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                listener.TokenDetailRelatedNewsOnClick(position)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.token_detail_related_news_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = tokenDetailRelatedNewsList[position]
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

            newsDescription.text = item.description
            newsAuthor.text = item.author
        }
    }

    override fun getItemCount(): Int {
        return tokenDetailRelatedNewsList.size
    }
}