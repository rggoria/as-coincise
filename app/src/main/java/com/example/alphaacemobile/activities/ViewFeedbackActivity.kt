package com.example.alphaacemobile.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.adapter.FeedbackAdapter
import com.example.alphaacemobile.databinding.ActivityViewFeedbackBinding
import com.xwray.groupie.GroupieAdapter
import org.json.JSONObject

class ViewFeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewFeedbackBinding
    private lateinit var groupieAdapter: GroupieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*  Setup Link  */
        Log.d("ADMIN_FEEDBACK_RESPONSE", "${Hostname.BASE_URL}")

        /*  Setup Action Bar details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "View Feedback"

        groupieAdapter = GroupieAdapter()
        binding.rvFeedback.adapter = groupieAdapter

        val url = Hostname.BASE_URL + "get_feedback_data"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.POST, url,
            Response.Listener { response ->

                Log.d("ADMIN_FEEDBACK_RESPONSE", response)

                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Approved")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val username = jo.getString("username").toString()
                        val feedback = jo.getString("feedback").toString()

                        groupieAdapter.add(FeedbackAdapter("$username","$feedback"))

                    }
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        stringRequest.retryPolicy = DefaultRetryPolicy(
            5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(stringRequest)
    }

    /*  For Back  */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}