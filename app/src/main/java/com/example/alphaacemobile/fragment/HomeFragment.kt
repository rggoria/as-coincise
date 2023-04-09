package com.example.alphaacemobile.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.activities.CalculatorSimulationActivity
import com.example.alphaacemobile.adapter.HomeFavoriteAdapter
import com.example.alphaacemobile.adapter.HomeNewsAdapter
import com.example.alphaacemobile.adapter.HomeTopAdapter
import com.example.alphaacemobile.databinding.FragmentHomeBinding
import com.example.alphaacemobile.model.HomeFavoriteModel
import com.example.alphaacemobile.model.HomeNewsModel
import com.example.alphaacemobile.model.HomeTopModel
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

private lateinit var binding: FragmentHomeBinding

/*  Setup SharedPreference  */
private lateinit var preferences: SharedPreferences
private lateinit var simulate: SharedPreferences
private lateinit var guide: SharedPreferences
private lateinit var editorGuide: SharedPreferences.Editor


class HomeFragment : Fragment() {

    /*  Setup List and Adapters  */
    // Home Favorite Model
    private var homeFavoriteList: ArrayList<HomeFavoriteModel> = ArrayList()
    private var rvHomeFavorite = HomeFavoriteAdapter(homeFavoriteList, this@HomeFragment)

    // Home News Model
    private var homeNewsList: ArrayList<HomeNewsModel> = ArrayList()
    private var rvHomeNews = HomeNewsAdapter(homeNewsList, this@HomeFragment)

    // Home Top Model
    private var homeTopList: ArrayList<HomeTopModel> = ArrayList()
    private var rvHomeTop = HomeTopAdapter(homeTopList, this@HomeFragment)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*  Setup Link  */
        Log.d("HOMEPAGE_URL", "${Hostname.BASE_URL}")

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        /*  Setup SharedPreference  */
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var email = preferences.getString("email", "")

        simulate = requireActivity().getSharedPreferences("simulation", Context.MODE_PRIVATE)
        var checkSimulate = simulate.getString("statusSimulation", "")

        guide = requireActivity().getSharedPreferences("guide", Context.MODE_PRIVATE)
        var checkGuide = guide.getString("statusGuide", "")
        editorGuide = guide.edit()

        setupRecyclerView()

        if (checkGuide.isNullOrEmpty()){
            setupGuide()
        }

        if (checkSimulate == "1"){
            getSimulationData()
        } else {
            getHomeTopData()
        }

        if (email.isNullOrEmpty()) {
            binding.cvHomeFavorite.visibility = View.GONE
        } else {
            binding.cvHomeFavorite.visibility = View.VISIBLE
        }

        /*  Setup Swipe Refresh Layout  */
        binding.sflHome.setOnRefreshListener {
            if (checkSimulate == "1"){
                getSimulationData()
                Toast.makeText(requireActivity(), "SIMULATION ON", Toast.LENGTH_SHORT).show()
            } else {
                getHomeTopData()
                Toast.makeText(requireActivity(), "SIMULATION OFF", Toast.LENGTH_SHORT).show()
            }
            binding.sflHome.isRefreshing = false
        }

        return binding.root
    }

    private fun setupGuide() {
        /* Setup Widget */
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_guide)
        var cv1 = dialog.findViewById<CardView>(R.id.cvHomeModule)
        var btn1 = dialog.findViewById<Button>(R.id.btnnext1)
        var cv2 = dialog.findViewById<CardView>(R.id.cvSearchModule)
        var btn2 = dialog.findViewById<Button>(R.id.btnnext2)
        var cv3 = dialog.findViewById<CardView>(R.id.cvMoreModule)
        val dialogButtonYes = dialog.findViewById(R.id.btnnext3) as Button
        dialogButtonYes.setOnClickListener {
            editorGuide.putString("statusGuide", "1")
            editorGuide.commit()
            dialog.dismiss()
        }
        dialog.show()
        btn1.setOnClickListener{
            cv1.visibility = View.GONE
            cv2.visibility = View.VISIBLE
        }

        btn2.setOnClickListener{
            cv2.visibility = View.GONE
            cv3.visibility = View.VISIBLE
        }
    }

    private fun getSimulationData() {
        val url = Hostname.BASE_URL + "get_home_top_token_simulation"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = StringRequest(Request.Method.POST, url,
            { response ->

                Log.d("HOMEPAGE_SIM_RESPONSE: ", response)

                homeTopList.clear()  //Use to block the repetition

                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    // Log.d("json", jsonArray.toString())

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val top = jo.getString("top").toString()
                        val token_img = jo.getString("token_img").toString()
                        val token_name = jo.getString("token_name").toString()
                        val curr_prc = jo.getString("token_price").toString()
                        val price = curr_prc.split(' ')[0];
                        val sentiment_score = jo.getString("sentiment_score").toDouble()
                        var result = ""
                        result = if (sentiment_score >= 2) {
                            "😁"
                        } else if (sentiment_score < 2 && sentiment_score >= 1.6) {
                            "🙂"
                        } else if (sentiment_score < 1.6 && sentiment_score >= 0.8) {
                            "🤨"
                        } else if (sentiment_score < 0.8 && sentiment_score > 0.4) {
                            "🙁"
                        } else {
                            "😡"
                        }
                        val user = HomeTopModel(top, token_img, token_name, price, result)
                        homeTopList.add(user)

                    }
                    rvHomeTop.notifyDataSetChanged()
                }else{
                    Toast.makeText(requireActivity(),jsonObject.get("response").toString(),Toast.LENGTH_SHORT).show()
                }

            }, { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
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
                return 0
            }

            override fun retry(error: VolleyError?) {
                return retry(error)
            }
        }
    }

    /*  Home Favorite Data Function  */
    private fun getHomeFavoriteData() {
        var usernameSP = preferences.getString("username", "")

        val url = Hostname.BASE_URL + "get_home_favorite_token"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = StringRequest(Request.Method.POST, url,
            Response.Listener { response ->

                Log.d("HOMEPAGE_FAV_RESPONSE", response)

                homeFavoriteList.clear()  //Use to block the repetition
                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val username = jo.getString("username").toString()
                        val token_name = jo.getString("token_name").toString()
                        val status = jo.getString("status").toString()

                        val token_image = jo.getString("token_img").toString()
                        val token_current = jo.getString("curr_prc").toString()
                        val price = token_current.split(' ')[0];
                        if (username == usernameSP){
                            if (status == "active"){
                                val user = HomeFavoriteModel(token_image, token_name, price)

                                if (user != null)
                                    binding.cvHomeFavoriteEmpty.visibility = View.GONE
                                homeFavoriteList.add(user)
                            }
                        }
                    }
                    rvHomeFavorite.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })

        stringRequest.retryPolicy = DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(stringRequest)


    }

    private fun getHomeNewsData() {
        val url = Hostname.BASE_URL + "get_home_news"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = StringRequest(Request.Method.POST, url,
            Response.Listener { response ->

                Log.d("HOMEPAGE_NEWS_RESPONSE", response)

                homeNewsList.clear()  //Use to block the repetition
                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Approved")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val author = jo.getString("author").toString()
                        val description = jo.getString("description").toString()
                        val link = jo.getString("link").toString()
                        val profile_img = jo.getString("profile_img").toString()
                        val token_img = jo.getString("token_img").toString()

                        val news = HomeNewsModel("$profile_img", "$token_img", "$description", "$author", "$link" )
                        homeNewsList.add(news)

                    }
                    rvHomeNews.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        stringRequest.retryPolicy = DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(stringRequest)
    }

    /*  Home Top Data Function  */
    private fun getHomeTopData() {
        val url = Hostname.BASE_URL + "get_home_top_token"
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val stringRequest = StringRequest(Request.Method.POST, url,
            Response.Listener { response ->

                Log.d("HOMEPAGE_TOP_RESPONSE: ", response)

                homeTopList.clear()  //Use to block the repetition
                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")
                    // Log.d("json", jsonArray.toString())

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val top = jo.getString("top").toString()
                        val token_img = jo.getString("token_img").toString()
                        val token_name = jo.getString("token_name").toString()
                        val curr_prc = jo.getString("curr_prc").toString()
                        val price = curr_prc.split(' ')[0];
                        val sentiment_score = jo.getString("sentiment_score").toDouble()
                        var result = ""
                        result = if (sentiment_score >= 2) {
                            "😁"
                        } else if (sentiment_score < 2 && sentiment_score >= 1.6) {
                            "🙂"
                        } else if (sentiment_score < 1.6 && sentiment_score >= 0.8) {
                            "🤨"
                        } else if (sentiment_score < 0.8 && sentiment_score > 0.4) {
                            "🙁"
                        } else {
                            "😡"
                        }
                        val user = HomeTopModel(top, token_img, token_name, price, result)
                        homeTopList.add(user)

                    }
                    rvHomeTop.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
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
                return 0
            }

            override fun retry(error: VolleyError?) {
                return retry(error)
            }
        }
    }

    fun HomeFavoriteOnClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("name", homeFavoriteList[position].name)
        Toast.makeText(requireActivity(), "Clicked on token: ${homeFavoriteList[position].name}", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.tokenDetailFragment, bundle)
    }

    fun HomeNewsOnClick(position: Int) {
        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(homeNewsList[position].link))
        startActivity(urlIntent)
    }

    fun HomeTopOnClick(position: Int) {
        /*  SharedPreference  */
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var email = preferences.getString("email", "")

        simulate = requireActivity().getSharedPreferences("simulation", Context.MODE_PRIVATE)
        var checkSimulate = simulate.getString("statusSimulation", "")


        val bundle = Bundle()
        bundle.putString("name", homeTopList[position].name)
        //Toast.makeText(requireActivity(), "Clicked on token: ${homeTopList[position].name}", Toast.LENGTH_SHORT).show()
        if (checkSimulate == "1"){
            if (email.isNullOrEmpty()){
                Toast.makeText(requireActivity(), "Cannot use simulation mode user must login first", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireActivity(), CalculatorSimulationActivity::class.java)
                startActivity(intent)
            }
        } else {
            findNavController().navigate(R.id.tokenDetailFragment, bundle)
        }
    }

    private fun setupRecyclerView() {
        binding.rvHomeFavoriteList.apply {
            getHomeFavoriteData()
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = rvHomeFavorite
        }

        binding.rvHomeNewsList.apply {
            getHomeNewsData()
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = rvHomeNews
        }

        binding.rvHomeTopList.apply {
            getHomeTopData()
            layoutManager = LinearLayoutManager(context)
            adapter = rvHomeTop
        }
    }
}