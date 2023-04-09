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
import com.example.alphaacemobile.model.HomeTopModel
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class HomeTopAdapter(
    private val homeTopList: ArrayList<HomeTopModel>,
    val listener: HomeFragment
) : RecyclerView.Adapter<HomeTopAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val topRank = itemView.findViewById<TextView>(R.id.tvHomeTopRank)
        val topImage = itemView.findViewById<ImageView>(R.id.ivHomeTopImage)
        val topName = itemView.findViewById<TextView>(R.id.tvHomeTopName)
        val topPrice = itemView.findViewById<TextView>(R.id.tvHomeTopPrice)
        val topStatistics = itemView.findViewById<TextView>(R.id.tvHomeTopStatistics)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                listener.HomeTopOnClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_top_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = homeTopList[position]
        holder.apply {
            topRank.text = item.rank
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
                val imageURL = item.image

                // Tries to get the image and post it in the ImageView
                // with the help of Handler
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image = BitmapFactory.decodeStream(`in`)

                    // Only for making changes in UI
                    handler.post {
                        topImage.setImageBitmap(image)
                    }
                }

                // If the URL does not point to
                // image or any other kind of failure
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            topName.text = item.name
            topPrice.text = item.price
            topStatistics.text = item.statistics
        }
    }

    override fun getItemCount(): Int {
        return homeTopList.size
    }
}