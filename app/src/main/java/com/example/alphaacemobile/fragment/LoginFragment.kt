package com.example.alphaacemobile.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.activities.AdminActivity
import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.FragmentLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*  Inflate the layout for this fragment  */
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        /*  Setup Link  */
        Log.d("LOGIN_URL", "${Hostname.BASE_URL}")

        /*  SharedPreference  */
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)

        editor = preferences.edit()

        binding.apply {

            /*  Button Login Confirm  */
            btnLoginConfirm.setOnClickListener {
                val email = tietLoginEmail.text.toString()
                val password = tietLoginPassword.text.toString()
                getData(email, password)
            }

            /*  TextView Signup  */
            btnLoginSignup.setOnClickListener {
                requireActivity().onBackPressed()
                findNavController().navigate(R.id.signupFragment)
            }

            return root
        }
    }

    private fun getData(email: String, password: String) {
        val url = Hostname.BASE_URL + "get_login_data"
        val requestQueue = Volley.newRequestQueue(requireActivity())

        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->
                // Debugging purposes
                Log.d("LOGIN_GET", response)
                val jsonObject = JSONObject(response)

                if(jsonObject.get("response").equals("Approved")) {
                    if (jsonObject.get("status").toString() == "HIGH"){
                        val intent = Intent(requireActivity(), AdminActivity::class.java)
                        startActivity(intent)
                    }  else if (jsonObject.get("status").toString() == "DISABLED"){
                        Toast.makeText(requireActivity(), "Sorry this account is disabled", Toast.LENGTH_SHORT).show()
                    } else{
                        editor.apply {
                            putString("username", jsonObject.get("username").toString())
                            putString("email", jsonObject.get("email").toString())
                            commit()
                        }
                        requireActivity().onBackPressed()
                    }
                }
                else if(jsonObject.get("response").equals("Failed")) {
                    Toast.makeText(requireActivity(), "Email or Password is Incorrect", Toast.LENGTH_SHORT).show()
                }
                else {
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
                map["email"] = email
                map["password"] = password
                return map
            }
        }

        requestQueue.add(stringRequest)
    }
}