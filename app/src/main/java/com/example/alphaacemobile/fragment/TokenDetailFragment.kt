package com.example.alphaacemobile.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.adapter.TokenDetailRelatedNewsAdapter
import com.example.alphaacemobile.databinding.FragmentTokenDetailBinding
import com.example.alphaacemobile.model.TokenDetailRelatedNewsModel
import org.json.JSONObject
import java.util.concurrent.Executors

class TokenDetailFragment : Fragment() {

    /*  Setup List and Adapters  */
    private var tokenDetailRelatedNewsList: ArrayList<TokenDetailRelatedNewsModel> = ArrayList()
    private var rvTokenDetailRelatedNews = TokenDetailRelatedNewsAdapter(tokenDetailRelatedNewsList, this@TokenDetailFragment)

    private lateinit var binding: FragmentTokenDetailBinding

    /*  Setup SharedPreference  */
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*  Setup Link  */
        Log.d("TOKEN_DETAIL_URL", "${Hostname.BASE_URL}")

        /*  Inflate the Layout for this Fragment  */
        binding = FragmentTokenDetailBinding.inflate(inflater, container, false)

        binding.apply {

            /*  Get Data from SharedPreference  */
            preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
            var username = preferences.getString("username", "")
            var email = preferences.getString("email", "")

            /*  Get Data Name from Item Selected  */
            val tokenName = arguments?.getString("name")
            val tvName = tvTokenDetailHeader
            tvName.text = "$tokenName"

            /*  Add Token Validation  */
            btnFavorite.setOnClickListener {
                if (email.isNullOrEmpty()) {
                    editor = preferences.edit()
                    Toast.makeText(context, "You must be logged in first to add for favorites", Toast.LENGTH_SHORT).show()
                } else {
                    val username = username.toString()
                    val token_name = tokenName.toString()
                    if (btnFavorite.text == "Favorite"){
                        btnFavorite.text = "Unfavorite"
                        addFavorite(username, token_name)
                    } else {
                        btnFavorite.text = "Favorite"
                        updateFavorite(username, token_name)
                    }
                }
            }

            /*  Setup Token Details  */
            getData(username, tokenName)

            /*  Setup RecyclerView Data  */
            setupRecyclerView(tokenName)

            return root
        }
    }

    private fun setupRecyclerView(tokenName: String?) {
        binding.rvTokenDetailRelatedNews.apply {
            /*  Setup Database Data  */
            getTokenDetailRelatedNewsData(tokenName)

            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = rvTokenDetailRelatedNews
        }
    }

    private fun getTokenDetailRelatedNewsData(tokenName: String?) {
        val url = Hostname.BASE_URL + "get_related_news"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("TOKEN_DETAIL_NEWS", response)

                tokenDetailRelatedNewsList.clear()
                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Approved")) {
                    binding.cvTokenDetailRelatedNewsEmpty.visibility = View.GONE
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val vNewsImage = jo.getString("profile_img").toString()
                        val vNewsDescription = jo.getString("description").toString()
                        val vNewsAuthor = jo.getString("author").toString()
                        val vNewsLink = jo.getString("link").toString()
                        rvTokenDetailRelatedNews.notifyDataSetChanged()
                        tokenDetailRelatedNewsList.add(TokenDetailRelatedNewsModel("$vNewsImage", "$vNewsDescription", "$vNewsAuthor", "$vNewsLink"))
                    }
                } else if(jsonObject.get("response").equals("Failed")) {
                    binding.cvTokenDetailRelatedNewsEmpty.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireActivity(),"Request error",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        {
            override fun getParams(): HashMap<String, String>
            {
                val map = HashMap<String,String>()
                map["request"] = "Sent"
                map["tokenName"] = tokenName.toString()
                return map
            }
        }

        requestQueue.add(stringRequest)
    }

    fun TokenDetailRelatedNewsOnClick(position: Int) {
        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tokenDetailRelatedNewsList[position].link))
        startActivity(urlIntent)
    }

    /*  Add Favorite Token  */
    private fun addFavorite(username: String, tokenName: String) {
        val url = Hostname.BASE_URL + "insert_favorite_token"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)

                // Debugging purposes
                Log.d("TOKEN_DETAIL_ADD_FAV", response)

                if(jsonObject.get("response").equals("Successful")) {
                    Log.d("TOKEN_DETAIL", "ADDED")
                } else {
                    Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        /*  Sending Post Data  */
        {
            override fun getParams(): HashMap<String,String>{
                val map = HashMap<String,String>()
                map["username"] = username
                map["tokenName"] = tokenName
                map["status"] = "active"
                map["request"] = "Sent"
                return map
            }
        }
        requestQueue.add(stringRequest)

        /*  Required for Posting Data  */
        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 20000
            }

            override fun getCurrentRetryCount(): Int {
                return 20000
            }

            override fun retry(error: VolleyError?) {
                return retry(error)
            }
        }
    }

    /*  Update Favorite Token  */
    private fun updateFavorite(username: String, tokenName: String) {
        val url = Hostname.BASE_URL + "update_favorite_token"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)

                // Debugging purposes
                Log.d("TOKEN_DETAIL_UPD_FAV", response)

                if(jsonObject.get("response").equals("Successful")) {
                    Log.d("TOKEN_DETAIL", "UNFAVORITE")
                } else {
                    Toast.makeText(requireActivity(),"Request error", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        /*  Sending Post Data  */
        {
            override fun getParams(): HashMap<String,String>{
                val map = HashMap<String,String>()
                map["username"] = username
                map["tokenName"] = tokenName
                map["status"] = "disabled"
                map["request"] = "Sent"
                return map
            }
        }
        requestQueue.add(stringRequest)

        /*  Required for Posting Data  */
        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 20000
            }

            override fun getCurrentRetryCount(): Int {
                return 20000
            }

            override fun retry(error: VolleyError?) {
                return retry(error)
            }
        }
    }

    /*  Token Data  */
    private fun getData(usernameSP: String?, tokenName: String?) {
        val url = Hostname.BASE_URL + "get_token_detail"
        val requestQueue = Volley.newRequestQueue(requireActivity())

        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                // Debugging purposes
                Log.d("TOKEN_DETAIL_RESPONSE", response)

                val jsonObject = JSONObject(response)

                if(jsonObject.get("response").equals("Successful")) {
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val top = jo.getString("top").toString()
                        val curr_prc = jo.getString("curr_prc").toString()
                        val cir_supp = jo.getString("cir_supp").toString()
                        val total_vol = jo.getString("total_vol").toString()
                        val token_img = jo.getString("token_img").toString()
                        val price_24h = jo.getString("price_24h").toDouble()
                        val price_7d = jo.getString("price_7d").toDouble()
                        val symbol = jo.getString("symbol").toString().uppercase()
                        val holders = jo.getString("holders").toString()
                        val snow_website = jo.getString("snow_website").toString()
                        val reddit_link = jo.getString("reddit_link").toString()
                        val facebook_link = jo.getString("facebook_link").toString()
                        val linkedin = jo.getString("linkedin").toString()
                        val discord_invite = jo.getString("discord_invite").toString()
                        val twitter = jo.getString("twitter").toString()
                        val telegram = jo.getString("telegram").toString()
                        val coin_link = jo.getString("coin_link").toString()
                        val website = jo.getString("website").toString()
                        val status = jo.getString("status").toString()
                        val sentiment_score = jo.getString("sentiment_score").toDouble()

                        val username = jo.getString("username").toString()

                        // Favorite
                        val vFavorite = binding.btnFavorite

                        // Price
                        val vTop = binding.tvTokenDetailTop
                        val vSymbol = binding.tvTokenDetailSymbol
                        val vStatus = binding.tvTokenDetailStatus
                        val vPrice = binding.tvTokenDetailPrice
                        val vCirculatingPrice = binding.tvTokenDetailCirculatingPrice
                        val vTotalVolume = binding.tvTokenDetailVolume
                        val vMarketCap24 = binding.tvTokenDetail24H
                        val vMarketCap7 = binding.tvTokenDetail7D
                        val vHolders = binding.tvTokenDetailHolders

                        // Imageview
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
                            val imageURL = token_img

                            // Tries to get the image and post it in the ImageView
                            // with the help of Handler
                            try {
                                val `in` = java.net.URL(imageURL).openStream()
                                image = BitmapFactory.decodeStream(`in`)

                                // Only for making changes in UI
                                handler.post {
                                    binding.ivTokenDetail.setImageBitmap(image)
                                }
                            }

                            // If the URL doesnot point to
                            // image or any other kind of failure
                            catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        // Setting up data
                        if (username == usernameSP){
                            if (status == "active"){
                                vFavorite.text = "Unfavorite"
                            } else {
                                vFavorite.text = "Favorite"
                            }
                        } else {
                            vFavorite.text = "Favorite"
                        }

                        var result = ""
                        result = if (sentiment_score >= 2) {
                            "😁 Very Positive"
                        } else if (sentiment_score < 2 && sentiment_score >= 1.6) {
                            "🙂 Positive"
                        } else if (sentiment_score < 1.6 && sentiment_score >= 0.8) {
                            "🤨 Neutral"
                        } else if (sentiment_score < 0.8 && sentiment_score > 0.4) {
                            "🙁 Negative"
                        } else {
                            "😡 Very Negative"
                        }

                        vTop.text = "$top"
                        vSymbol.text = "$symbol"
                        vStatus.text  = "$result"
                        vPrice.text = "$curr_prc"
                        vCirculatingPrice.text = "$cir_supp"
                        vTotalVolume.text = "$total_vol"
                        vMarketCap24.text = "$price_24h"
                        vMarketCap7.text = "$price_7d"
                        vHolders.text = "$holders"

                        //Market Price Coloring Coloring
                        if (price_24h > 0) {
                            vMarketCap24.setTextColor(Color.parseColor("#00FF00"))
                        } else {
                            vMarketCap24.setTextColor(Color.parseColor("#FF0000"))
                        }

                        //Market Price Coloring Coloring
                        if (price_7d > 0) {
                            vMarketCap7.setTextColor(Color.parseColor("#00FF00"))
                        } else {
                            vMarketCap7.setTextColor(Color.parseColor("#FF0000"))
                        }

                        // Showing and Hiding Icon Links
                        if (snow_website == "null" || snow_website == ",0"){ // Reddit
                            binding.imTokenDetailSocialSnow.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialSnow.visibility =View.VISIBLE
                            binding.imTokenDetailSocialSnow.setOnClickListener {
                                Toast.makeText(requireActivity(),"Snowtrace",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(snow_website))
                                startActivity(urlIntent)
                            }
                        }
                        if (reddit_link == "null" || reddit_link == ",0"){ // Reddit
                            binding.imTokenDetailSocialReddit.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialReddit.visibility =View.VISIBLE
                            binding.imTokenDetailSocialReddit.setOnClickListener {
                                Toast.makeText(requireActivity(),"Reddit",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(reddit_link))
                                startActivity(urlIntent)
                            }
                        }
                        if (facebook_link == "null"){ // Facebook
                            binding.imTokenDetailSocialFacebook.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialFacebook.visibility =View.VISIBLE
                            binding.imTokenDetailSocialFacebook.setOnClickListener {
                                Toast.makeText(requireActivity(),"Facebook",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(facebook_link))
                                startActivity(urlIntent)
                            }
                        }
                        if (linkedin == "null"){ // Linkedin
                            binding.imTokenDetailSocialLinkedin.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialLinkedin.visibility =View.VISIBLE
                            binding.imTokenDetailSocialLinkedin.setOnClickListener {
                                Toast.makeText(requireActivity(),"Linkedin",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedin))
                                startActivity(urlIntent)
                            }
                        }
                        if (discord_invite == "null"){ // Discord
                            binding.imTokenDetailSocialDiscord.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialDiscord.visibility =View.VISIBLE
                            binding.imTokenDetailSocialDiscord.setOnClickListener {
                                Toast.makeText(requireActivity(),"Discord",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(discord_invite))
                                startActivity(urlIntent)
                            }
                        }
                        if (twitter == "null"){ // Twitter
                            binding.imTokenDetailSocialTwitter.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialTwitter.visibility =View.VISIBLE
                            binding.imTokenDetailSocialTwitter.setOnClickListener {
                                Toast.makeText(requireActivity(),"Twitter",Toast.LENGTH_SHORT).show()
                                val link = twitter.split(",").dropLast(1).joinToString(" ")
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/$link"))
                                startActivity(urlIntent)
                            }
                        }
                        if (telegram == "null"){ // Telegram
                            binding.imTokenDetailSocialTelegram.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialTelegram.visibility =View.VISIBLE
                            binding.imTokenDetailSocialTelegram.setOnClickListener {
                                Toast.makeText(requireActivity(),"Telegram",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegram))
                                startActivity(urlIntent)
                            }
                        }
                        if (coin_link == "null"){ // Coingecko
                            binding.imTokenDetailSocialCoingecko.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialCoingecko.visibility =View.VISIBLE
                            binding.imTokenDetailSocialCoingecko.setOnClickListener {
                                Toast.makeText(requireActivity(),"Coingecko",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(coin_link))
                                startActivity(urlIntent)
                            }
                        }
                        if (website == "null"){ // Website
                            binding.imTokenDetailSocialWebsite.visibility =View.GONE
                        } else {
                            binding.imTokenDetailSocialWebsite.visibility =View.VISIBLE
                            binding.imTokenDetailSocialWebsite.setOnClickListener {
                                Toast.makeText(requireActivity(),"Website",Toast.LENGTH_SHORT).show()
                                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(website.trim()))
                                startActivity(urlIntent)
                            }
                        }
                    }

                }
                else if(jsonObject.get("response").equals("Failed")){
                    Toast.makeText(requireActivity(), "Failed to Load Token Details", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(requireActivity(),"Request error",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        {
            override fun getParams(): HashMap<String, String>
            {
                val map = HashMap<String,String>()
                map["request"] = "Sent"
                map["name"] = "$tokenName"
                return map
            }
        }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(stringRequest)

        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 20000
            }

            override fun getCurrentRetryCount(): Int {
                return 20000
            }

            override fun retry(error: VolleyError?) {
                return retry(error)
            }
        }
    }

}