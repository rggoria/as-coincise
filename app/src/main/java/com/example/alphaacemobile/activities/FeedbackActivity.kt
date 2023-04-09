package com.example.alphaacemobile.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.databinding.ActivityFeedbackBinding
import org.json.JSONObject

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    /*  Setup SharedPreference  */
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*  Setup Link  */
        Log.d("FEEDBACK_URL", "${Hostname.BASE_URL}")

        /*  Setup Action Bar Details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "Send Feedback"

        /*  SharedPreference  */
        preferences = this.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "")

        /*  Text Layout Signup Email  */
        binding.tietFeedbackMessage.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                binding.tilFeedbackMessage.error = "Required"
            } else {
                binding.tilFeedbackMessage.error = null
            }
        }

        binding.btnFeedbackSubmit.setOnClickListener {
            if (binding.tietFeedbackMessage.text.toString().isEmpty()){
                binding.tilFeedbackMessage.error = "Required"
            } else {
                val message =  binding.tietFeedbackMessage.text.toString()
                sendMessage(username, message)
            }
        }

    }

    private fun sendMessage(username: String?, message: String) {
        val url = Hostname.BASE_URL + "send_feedback_message"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("FEEDBACK_SEND", response)

                // Debugging purposes
                Log.d("Feedback Activity", response)
                val jsonObject = JSONObject(response)

                if(jsonObject.get("response").equals("Approved")) {
                    Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Internet connection is required",Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): HashMap<String, String> {
                val map = HashMap<String,String>()
                map["request"] = "Sent"
                map["username"] = username.toString()
                map["feedback"] = message
                return map
            }
        }

        requestQueue.add(stringRequest)

        /*  RetryPolicy  */
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}