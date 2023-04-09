package com.example.alphaacemobile.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.ActivityAdminBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.util.regex.Pattern

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*  Setup Link  */
        Log.d("ADMIN_URL", "${Hostname.BASE_URL}")

        /*  Setup Action Bar details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "Admin"

        /*  Button Create New User Button  */
        binding.btnAdminCreateAccount.setOnClickListener {
            addUser()
        }

        /*  Button View Feedback  */
        binding.btnAdminViewFeedback.setOnClickListener {
            val intent = Intent(this, ViewFeedbackActivity::class.java)
            startActivity(intent)
        }
    }

    /*  Add User  */
    private fun addUser() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.admin_create, null)
        var accountUsernameDialog = view.findViewById<TextInputEditText>(R.id.tietAdminCreateUsername)
        var accountEmailDialog = view.findViewById<TextInputEditText>(R.id.tietAdminCreateEmail)
        var accountPasswordDialog = view.findViewById<TextInputEditText>(R.id.tietAdminCreatePassword)
        var accountCreateDialog = view.findViewById<Button>(R.id.btnAdminCreateUser)

        var accountUsernameDialogError = view.findViewById<TextInputLayout>(R.id.tilAdminCreateUsername)
        var accountEmailDialogError = view.findViewById<TextInputLayout>(R.id.tilAdminCreateEmail)
        var accountPasswordDialogError = view.findViewById<TextInputLayout>(R.id.tilAdminCreatePassword)

        var PasswordPattern = Pattern.compile("(^)" + "(?=.*[0-9])" + "(?=.*[a-z])" +
                "(?=.*[A-Z])" + "(?=.*[@#!$%^&+=.])" + "(?=\\S+$)" + ".{8,}" + "$")

        /*  Setup Dialog  */
        val addDialog = AlertDialog.Builder(this)
        addDialog.setView(view)
        addDialog.create()
        addDialog.setPositiveButton("Done"){ dialog,_->
            dialog.dismiss()
        }
        addDialog.show()

        /*  TextLayout Admin Username  */
        accountUsernameDialog.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                accountUsernameDialogError.error = "Required"
            }
            else if (text.length <= 5){
                accountUsernameDialogError.error = "Username must be 6 characters or above"
            }
            else {
                accountUsernameDialogError.error = null
            }
        }

        /*  TextLayout Admin Email  */
        accountEmailDialog.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                accountEmailDialogError.error = "Required"
            }
            else if (!text.contains("@")){
                accountEmailDialogError.error = "Require Valid Email Address"
            }
            else if (!text.contains(".com")){
                accountEmailDialogError.error = "Require Valid Email Address"
            }
            else {
                accountEmailDialogError.error = null
            }
        }

        /*  TextLayout Admin Password  */
        accountPasswordDialog.doOnTextChanged { text, start, before, count ->
            if (text!!.isEmpty()) {
                accountPasswordDialogError.error = "Required"
            }
            else if (!PasswordPattern.matcher(text).matches()){
                accountPasswordDialogError.error = "Requirements: \n Min. 8 Characters \n At least 1 Uppercase and Lowercase \n At least 1 Numeric Value \n At least 1 Special Character"
            }
            else {
                accountPasswordDialogError.error = null
            }
        }

        /*  Button Admin Confirm  */
        accountCreateDialog.setOnClickListener {
            if (accountUsernameDialog.text.toString().isEmpty() ||
                accountUsernameDialog.text.toString().length <= 5) {
                Toast.makeText(this, "Username field is incorrect", Toast.LENGTH_SHORT).show()
            } else if (accountEmailDialog.text.toString().isEmpty() ||
                !(accountEmailDialog.text.toString().contains("@")) ||
                !(accountEmailDialog.text.toString().contains(".com"))) {
                Toast.makeText(this, "Email field is incorrect", Toast.LENGTH_SHORT).show()
            } else if (accountPasswordDialog.text.toString().isEmpty() ||
                PasswordPattern.matcher(accountPasswordDialog.toString()).matches()) {
                Toast.makeText(this, "Password field is incorrect", Toast.LENGTH_SHORT).show()
            } else {
                /*  Setup variables  */
                val username = accountUsernameDialog.text.toString()
                val email = accountEmailDialog.text.toString()
                val password = accountPasswordDialog.text.toString()
                addData(username, email, password)
            }
        }

    }

    private fun addData(username: String, email: String, password: String) {
        val url = Hostname.BASE_URL + "insert_user_data"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("ADMIN_INSERT", response)

                val jsonObject = JSONObject(response)

                if(jsonObject.get("response").equals("Successful")) {
                    Toast.makeText(this, "Account Created!!!", Toast.LENGTH_SHORT).show()
                } else if (jsonObject.get("response").equals("Taken")) {
                    Toast.makeText(this,"Username of Email is already taken", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        {
            override fun getParams(): HashMap<String,String>{
                val map = HashMap<String,String>()
                map["username"] = username
                map["email"] = email
                map["password"] = password
                map["request"] = "Sent"
                return map
            }
        }
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

    /*  For Back  */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}