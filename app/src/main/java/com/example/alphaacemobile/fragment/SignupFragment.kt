package com.example.alphaacemobile.fragment
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.FragmentSignupBinding
import org.json.JSONObject
import java.util.regex.Pattern

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var PasswordPattern = Pattern.compile("(^)" + "(?=.*[0-9])" + "(?=.*[a-z])" +
                "(?=.*[A-Z])" + "(?=.*[@#!$%^&+=.])" + "(?=\\S+$)" + ".{8,}" + "$")

        /*  Inflate the layout for this fragment  */
        binding = FragmentSignupBinding.inflate(layoutInflater, container, false)

        /*  Setup Link  */
        Log.d("SIGNUP_URL", "${Hostname.BASE_URL}")

        /*  SharedPreference  */
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        editor = preferences.edit()

        /*  Text Layout Signup Email  */
        binding.tietSignupEmail.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                binding.tilSignupEmail.error = "Required"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.tietSignupEmail.text).matches()) {
                binding.tilSignupEmail.error = "Required Valid Email Address"
            } else {
                binding.tilSignupEmail.error = null
            }
        }

        /*  Text Layout Signup Account  */
        binding.tietSignupUsername.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                binding.tilSignupUsername.error = "Required"
            } else if (text.length <= 4) {
                binding.tilSignupUsername.error = "Username must contain at least 5 characters"
            } else {
                binding.tilSignupUsername.error = null
            }
        }
        binding.tietSignupPassword.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                binding.tilSignupPassword.error = "Required"
            } else if (!PasswordPattern.matcher(text).matches()){
                binding.tilSignupPassword.error = "Requirements: \n Min. 8 Characters \n At least 1 Uppercase and Lowercase \n At least 1 Numeric Value \n At least 1 Special Character"
            } else {
                binding.tilSignupPassword.error = null
            }
        }


        /*  Button Email Verification Section (Step 1)  */
        binding.btnSignupVerifyEmail.setOnClickListener {
            /*  Setup Email Widget  */
            var etEmail = binding.tietSignupEmail.text.toString()

            if (etEmail.isEmpty()) {
                binding.tilSignupEmail.error = "Required"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail).matches()) {
                binding.tilSignupEmail.error = "Required Valid Email Address"
            } else {
                val url = Hostname.BASE_URL + "get_email_data"
                val requestQueue = Volley.newRequestQueue(requireActivity())

                val stringRequest = object : StringRequest(Method.POST,url,
                    Response.Listener { response ->
                        // Debugging purposes
                        Log.d("SIGNUP_RESPONSE", response)
                        val jsonObject = JSONObject(response)

                        if(jsonObject.get("response").equals("Approved")) {
                            binding.cvEmail.visibility = View.GONE
                            binding.llSignupLogin.visibility = View.GONE
                            binding.cvAccount.visibility = View.VISIBLE
                            addUser(etEmail)

                        } else if(jsonObject.get("response").equals("Taken")) {
                            binding.tilSignupEmail.error = "Email is already taken"
                        } else {
                            Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener { error ->
                        Toast.makeText(requireActivity(),"Hey Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                    }) {
                    override fun getParams(): HashMap<String, String> {
                        val map = HashMap<String,String>()
                        map["request"] = "Sent"
                        map["email"] = etEmail
                        return map
                    }
                }

                requestQueue.add(stringRequest)
            }
        }

        /*  Account Successful Section  */
        binding.btnSignupReturn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        /*  Login Section  */
        binding.btnSignupLogin.setOnClickListener {
            requireActivity().onBackPressed()
            findNavController().navigate(R.id.loginFragment)
        }

        return binding.root
    }

    /*  Button Account Verification Section (Step 2)  */
    private fun addUser(etEmail: String) {
        binding.btnSignupCreate.setOnClickListener {
            var etUsername = binding.tietSignupUsername.text.toString()
            var etPassword = binding.tietSignupPassword.text.toString()
            var PasswordPattern = Pattern.compile("(^)" + "(?=.*[0-9])" + "(?=.*[a-z])" +
                    "(?=.*[A-Z])" + "(?=.*[@#!$%^&+=.])" + "(?=\\S+$)" + ".{8,}" + "$")

            if (etUsername.isEmpty()) {
                binding.tilSignupUsername.error = "Required"
            } else if (etUsername.length <= 4) {
                binding.tilSignupUsername.error = "Username must contain at least 5 characters"
            } else if (etPassword.isEmpty()) {
                binding.tilSignupPassword.error = "Required"
            }
            else if (!PasswordPattern.matcher(etPassword).matches()){
                binding.tilSignupPassword.error = "Requirements: \n Min. 8 Characters \n At least 1 Uppercase and Lowercase \n At least 1 Numeric Value \n At least 1 Special Character"
            }

            else {
                val url = Hostname.BASE_URL + "create_user_data"
                val requestQueue = Volley.newRequestQueue(requireActivity())

                val stringRequest = object : StringRequest(Method.POST,url,
                    Response.Listener { response ->

                        // Debugging purposes
                        Log.d("SIGNUP_CREATE", response)

                        val jsonObject = JSONObject(response)

                        if(jsonObject.get("response").equals("Successful")) {
                            binding.cvAccount.visibility = View.GONE
                            binding.cvSuccessful.visibility = View.VISIBLE
                            editor.apply {
                                putString("username", etUsername)
                                putString("email", etEmail)
                                commit()
                            }

                        } else if(jsonObject.get("response").equals("Taken")) {
                            binding.tilSignupUsername.error = "Username is already taken"
                        } else if(jsonObject.get("response").equals("Retry")) {
                            Toast.makeText(requireActivity(), "Sending message error", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener { error ->
                        Toast.makeText(requireActivity(),"HI Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                    }) {
                    override fun getParams(): HashMap<String, String> {
                        val map = HashMap<String,String>()
                        map["request"] = "Sent"
                        map["email"] = etEmail
                        map["username"] = etUsername
                        map["password"] = etPassword
                        return map
                    }
                }
                requestQueue.add(stringRequest)
            }
        }
    }
}