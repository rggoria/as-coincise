package com.example.alphaacemobile.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.activities.*
import com.example.alphaacemobile.adapter.CalculatorAdapter_simulation
import com.example.alphaacemobile.databinding.FragmentMoreBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.util.regex.Pattern

class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    /*  Setup SharedPreference  */
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var notification: SharedPreferences
    private lateinit var editorNotification: SharedPreferences.Editor

    private lateinit var simulate: SharedPreferences
    private lateinit var editorSimulate: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*  Inflate the layout for this fragment  */
        binding = FragmentMoreBinding.inflate(inflater, container, false)

        /*  SharedPreference  */
        // Userdata Logged In
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var email = preferences.getString("email", "")

        // Notification
        notification = requireActivity().getSharedPreferences("notification", Context.MODE_PRIVATE)
        var checkNotification = notification.getString("statusNotification", "")
        editorNotification = notification.edit()

        // Simulation
        simulate = requireActivity().getSharedPreferences("simulation", Context.MODE_PRIVATE)
        var checkSimulate = simulate.getString("statusSimulation", "")
        editorSimulate = simulate.edit()

        binding.apply {

            if (checkNotification == "1"){
                swMoreNotification.isChecked = true;
            } else {
                swMoreNotification.isChecked = false;
            }

            if (checkSimulate == "1"){
                swMoreSimulation.isChecked = true;
            } else {
                swMoreSimulation.isChecked = false;
            }

            if (email.isNullOrEmpty()) {
                btnMoreLogin.visibility = View.VISIBLE
                btnMoreSignup.visibility = View.VISIBLE
                editor = preferences.edit()
                btnMoreAccountActiveEmail.visibility = View.GONE
                btnMoreLogout.visibility = View.GONE
                btnChangePass.visibility = View.GONE
            } else {
                btnMoreLogin.visibility = View.GONE
                btnMoreSignup.visibility = View.GONE
                btnMoreAccountActiveEmail.visibility = View.VISIBLE
                btnMoreAccountActiveEmail.text = "Email: ${email}"
                btnChangePass.visibility = View.VISIBLE
                btnMoreLogout.visibility = View.VISIBLE
            }

            // Switch News Notification
            swMoreNotification.setOnClickListener() {
                if (swMoreNotification.isChecked()) {
                    editorNotification.apply {
                        putString("statusNotification", "1")
                        commit()
                    }
                    Toast.makeText(requireActivity(), "News Notification ON", Toast.LENGTH_SHORT).show()
                } else {
                    editorNotification.apply {
                        putString("statusNotification", "0")
                        commit()
                    }
                    Toast.makeText(requireActivity(), "News Notification OFF", Toast.LENGTH_SHORT).show()
                }
            }

            // Switch Simulation
            swMoreSimulation.setOnClickListener() {
                if (swMoreSimulation.isChecked()) {
                    editorSimulate.apply {
                        putString("statusSimulation", "1")
                        commit()
                    }
                    Toast.makeText(requireActivity(), "Simulation Mode ON", Toast.LENGTH_SHORT).show()
                } else {
                    editorSimulate.apply {
                        putString("statusSimulation", "0")
                        commit()
                    }
                    Toast.makeText(requireActivity(), "Simulation Mode OFF", Toast.LENGTH_SHORT).show()
                }
            }

            /*  Button Login  */
            btnMoreLogin.setOnClickListener(){
                findNavController().navigate(R.id.loginFragment)
            }

            /*  Button Signup  */
            btnMoreSignup.setOnClickListener(){
                findNavController().navigate(R.id.signupFragment)
            }

//            /*  Button Change Password  */
//            btnMoreAccountActiveEmail.setOnClickListener(){
//                val intent = Intent(requireActivity(), AccountActivity::class.java)
//                startActivity(intent)
//            }

            btnChangePass.setOnClickListener(){
                Updatepass()
            }

            // Button Logout
            btnMoreLogout.setOnClickListener(){
                editor = preferences.edit()
                btnMoreLogin.visibility = View.VISIBLE
                btnMoreSignup.visibility = View.VISIBLE
                btnChangePass.visibility = View.VISIBLE
                btnMoreAccountActiveEmail.visibility = View.GONE
                btnMoreLogout.visibility = View.GONE
                editor.apply {
                    clear()
                    commit()
                }
                editorSimulate.apply {
                    putString("statusSimulation", "0")
                    commit()
                }
                requireActivity().onBackPressedDispatcher
                findNavController().navigate(R.id.moreFragment)
                Toast.makeText(context, "Logout!!!", Toast.LENGTH_SHORT).show()
            }

            // Button Send Feedback Activity
            btnMoreFeedback.setOnClickListener(){
                if (email.isNullOrEmpty()) {
                    Toast.makeText(context, "You must be logged in first to use this tool", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(requireActivity(), FeedbackActivity::class.java)
                    startActivity(intent)
                }
            }

            // Button About Developers Activity
            btnMoreAboutUs.setOnClickListener(){
                val intent = Intent(requireActivity(), AboutUsActivity::class.java)
                startActivity(intent)
            }

            // Button Calculator Activity
            btnMoreCalculator.setOnClickListener(){
                if (email.isNullOrEmpty()) {
                    editor = preferences.edit()
                    Toast.makeText(context, "You must be logged in first to use this tool", Toast.LENGTH_SHORT).show()
                } else {

                    simulate = requireActivity().getSharedPreferences("simulation", Context.MODE_PRIVATE)
                    var checkSimulation = simulate.getString("statusSimulation", "")


                    if (checkSimulation == "0") {
                        val intent = Intent(requireActivity(), CalculatorActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(requireActivity(), CalculatorSimulationActivity::class.java)
                        startActivity(intent)
                    }
                }
            }

            return root
        }
    }
    private fun Updatepass() {
        preferences = requireActivity().getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "").toString()
        var email = preferences.getString("email", "").toString()

        /*  Setup Layout  */
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.dialog_forget_password, null)

        var cvPassword = view.findViewById<CardView>(R.id.cvForgetPassword)
        var etForgetPasswordError = view.findViewById<TextInputLayout>(R.id.tilForgetPassword)
        var etForgetPassword = view.findViewById<TextInputEditText>(R.id.tielForgetPassword)
        var btnPassword = view.findViewById<Button>(R.id.btnForgetPassword)
        var cvSuccessful = view.findViewById<CardView>(R.id.cvForgetSuccessful)

        var PasswordPattern = Pattern.compile("(^)" + "(?=.*[0-9])" + "(?=.*[a-z])" +
                "(?=.*[A-Z])" + "(?=.*[@#!$%^&+=.])" + "(?=\\S+$)" + ".{8,}" + "$")


        /*  Setup Dialog  */
        val addDialog = AlertDialog.Builder(requireActivity(), R.style.MyDialogStyle)
        addDialog.setView(view)
        addDialog.create()
        addDialog.setPositiveButton("Done"){ dialog,_->
            dialog.dismiss()
        }
        addDialog.show()

        /*  TextLayout Forget Password  */
        etForgetPassword.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                etForgetPasswordError.error = "Required"
            }
            else if (!PasswordPattern.matcher(text).matches()){
                etForgetPasswordError.error = "Requirements: \n Min. 8 Characters \n At least 1 Uppercase and Lowercase \n At least 1 Numeric Value \n At least 1 Special Character"
            }
            else {
                etForgetPasswordError.error = null
            }
        }


//        etForgetPassword.doOnTextChanged { text, start, before, count ->
//            val number = Regex("[0-9]")
//            if (text!!.isEmpty()) {
//                etForgetPasswordError.error = "Required"
//            }
//            else if (isLettersOrDigits(text.toString())){
//                etForgetPasswordError.error = "Password must at least have 1 number and Uppercase Character"
//            }
//            else {
//                etForgetPasswordError.error = null
//            }
//        }

        btnPassword.setOnClickListener {
            var etPassword = etForgetPassword.text.toString()
            if (etPassword.isEmpty()) {
                etForgetPasswordError.error = "Required"
            } else if (etPassword.length <= 7) {
                etForgetPasswordError.error = "Password must contain at least 8 characters"
            } else {
                val url = Hostname.BASE_URL + "insert_alteration_data"
                val requestQueue = Volley.newRequestQueue(requireActivity())

                val stringRequest = object : StringRequest(Method.POST,url,
                    Response.Listener { response ->

                        // Debugging purposes
                        Log.d("LOGIN_INSERT_ALT", response)

                        val jsonObject = JSONObject(response)

                        if(jsonObject.get("response").equals("Successful")) {
                            cvPassword.visibility = View.GONE
                            cvSuccessful.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(requireActivity(),"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener { error ->
                        Toast.makeText(requireActivity(),"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                    }) {
                    override fun getParams(): HashMap<String, String> {
                        val map = HashMap<String,String>()
                        map["request"] = "Sent"
                        map["username"] = username
                        map["email"] = email
                        map["password"] = etPassword
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
        }

    }

    private fun isLettersOrDigits(chars: String): Boolean {
        for (c in chars)
        {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c !in '0'..'9') {
                return false
            }
        }
        return true
    }

}