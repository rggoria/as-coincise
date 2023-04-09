package com.example.alphaacemobile.activities
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.adapter.CalculatorAdapter
import com.example.alphaacemobile.databinding.ActivityCalculatorBinding
import com.example.alphaacemobile.model.CalculatorModel
import com.example.alphaacemobile.model.HomeTopModel
import com.zues.searchable_spinner.SearchableSpinner
import org.json.JSONObject
import kotlin.math.log

class CalculatorActivity : AppCompatActivity() {

    private var result: Double = 0.00
    private var resultNewZealand: Double = 0.00
    private var resultNewPhilippines: Double = 0.00
    private lateinit var binding: ActivityCalculatorBinding
    private lateinit var calculatorList: ArrayList<CalculatorModel>
    private lateinit var calculatorAdapter: CalculatorAdapter
    /*  Setup SharedPreference  */
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CALCULATOR_URL", "${Hostname.BASE_URL}")

        /*  Setup SharedPreference details  */
        preferences = this.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "").toString()

        /*  Setup Action Bar details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "Calculator"

        /*  Setup List  */
        calculatorList = ArrayList()

        /*  Setup Layout Palettes  */
        val addBtn = binding.fabCalculatorAdd
        var rvCalculator = binding.rvCalculator

        /*  Setup Adapter  */
        calculatorAdapter = CalculatorAdapter(this, calculatorList)

        /*  Setup RecyclerView  */
        rvCalculator.layoutManager = LinearLayoutManager(this)
        rvCalculator.adapter = calculatorAdapter

        /*  Setup Get Wallet Database  */
        getWallet(username)
        binding.btnCalculatorCalculate.setOnClickListener {
            getWallet(username)
        }

        /*  Setup Add button  */
        addBtn.setOnClickListener {
            addInfo()
        }
    }

    /*  Get Wallet (Get Latest Data From The Database And Get Total Amount)  */
    private fun getWallet(username: String) {
        val url = Hostname.BASE_URL + "get_wallet_token"
        val requestQueue = Volley.newRequestQueue(this)
        resetTotalAmount()
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_RESPONSE", response)
                Log.d("hello", username)

                calculatorList.clear()
                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Approved")) {
                    binding.cvCalculatorEmpty.visibility = View.GONE
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val sSelect = jo.getString("token_name").toString()
                        val sAmount = jo.getString("amount").toString()
                        val sPrice = jo.getString("curr_prc").toString()
                        Log.d("usdeto", sPrice)
                        val sStatus = jo.getString("status").toString()
                        val usd = sPrice.split(' ')[0];
                        val nzd = sPrice.split(' ')[1];
                        val php = sPrice.split(' ')[2];
                        Log.d("phpto", php)
                        if (sStatus == "active"){
                            // updates the data
                            calculatorAdapter.notifyDataSetChanged()
                            calculatorList.add(CalculatorModel("$sSelect", "$sAmount"))
                            checkTotalAmount((sAmount.toDouble() * usd.toDouble()).toString(), (sAmount.toDouble() * nzd.toDouble()).toString(), (sAmount.toDouble() * php.toDouble()).toString())
                        } else if (sStatus != "active") {
                            continue
                        } else {
                            binding.cvCalculatorEmpty.visibility = View.VISIBLE
                        }
                    }
                } else if(jsonObject.get("response").equals("Failed")) {
                    binding.cvCalculatorEmpty.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        {
            override fun getParams(): HashMap<String, String>
            {
                val map = HashMap<String,String>()
                map["request"] = "Sent"
                map["username"] = username
                return map
            }
        }

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

    /*  Get Wallet (Get Latest Data From The Database Only)  */
    private fun updateWallet(username: String) {
        val url = Hostname.BASE_URL + "get_wallet_token"
        val requestQueue = Volley.newRequestQueue(this)
        resetTotalAmount()
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_UPDATE", response)

                calculatorList.clear()
                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Approved")) {
                    binding.cvCalculatorEmpty.visibility = View.GONE
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val sSelect = jo.getString("token_name").toString()
                        val sAmount = jo.getString("amount").toString()
                        val sPrice = jo.getString("curr_prc").toString()
                        val sStatus = jo.getString("status").toString()
                        if (sStatus == "active"){
                            // updates the data
                            calculatorAdapter.notifyDataSetChanged()
                            calculatorList.add(CalculatorModel("$sSelect", "$sAmount"))
                        } else if (sStatus != "active") {
                            continue
                        } else {
                            binding.cvCalculatorEmpty.visibility = View.VISIBLE
                        }
                    }
                } else if(jsonObject.get("response").equals("Failed")) {
                    binding.cvCalculatorEmpty.visibility = View.VISIBLE
                }
                else {
                    Toast.makeText(this,"Request error",Toast.LENGTH_SHORT).show()
                }

            }, Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        {
            override fun getParams(): HashMap<String, String>
            {
                val map = HashMap<String,String>()
                map["request"] = "Sent"
                map["username"] = username
                return map
            }
        }

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


    /*  Add Token Information Function  */
    private fun addInfo() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.add_wallet_item, null)

        /*  Setup Variables  */
        val calculatorSelect = view.findViewById<SearchableSpinner>(R.id.ssCalculatorSelect)
        val calculatorAmount = view.findViewById<EditText>(R.id.etCalculatorAmount)
        var list: ArrayList<HomeTopModel> = ArrayList()

        /*  Setup Searchable Spinner  */
        val url = Hostname.BASE_URL + "get_home_top_token"
        val requestQueue = Volley.newRequestQueue(this)
        val list_token = mutableListOf<String>()
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->

                Log.d("CALCULATOR_SPINNER", response)

                list.clear()
                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val token_name = jo.getString("token_name").toString()
                        val token_prc = jo.getString("curr_prc").toString()

                        list_token += token_name
                    }
                    /*  Setup Token List  */
                    calculatorSelect.setItems(list_token)
                    /*  Setup SearchableSpinner  */
                    calculatorSelect.setOnItemSelectListener(object : SearchableSpinner.SearchableItemListener {
                        override fun onItemSelected(view: View?, position: Int) {
                            Toast.makeText(this@CalculatorActivity, "${calculatorSelect.mSelectedItem}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onSelectionClear() {
                            Toast.makeText(this@CalculatorActivity, "Clear", Toast.LENGTH_SHORT).show()
                        }
                    })
                }else{
                    Toast.makeText(this,jsonObject.get("response").toString(),Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)

        /*  Setup Dialog  */
        val addDialog = AlertDialog.Builder(this, R.style.MyDialogStyle)
        addDialog.setView(view)
        addDialog.setPositiveButton("Confirm"){
                dialog,_->
            val select = calculatorSelect.mSelectedItem.toString()
            val amount = calculatorAmount.text.toString()
            if (select == "null" || amount == ""){
                dialog.dismiss()
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            } else {
                /*  Use to Hide Empty message  */
                binding.cvCalculatorEmpty.visibility = View.GONE
                addWallet(select, amount)

                dialog.dismiss()
            }
        }
        addDialog.setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
        }
        addDialog.create()
        addDialog.show()
    }

    private fun addWallet(select: String, amount: String) {
        /*  Setup SharedPreference details  */
        preferences = this.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "")

        val url = Hostname.BASE_URL + "insert_wallet_data"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_INSERT", response)

                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Successful")) {
                    /*  First List Must Be Cleared  */
                    calculatorList.clear()
                    /*  Second It Must Notify The Changes  */
                    calculatorAdapter.notifyDataSetChanged()
                    /*  Third Add The New List  */
                    calculatorList.add(CalculatorModel("$select", "$amount"))
                    /*  Lastly Call getWallet To Get The List Of Data  */
                    updateWallet(username.toString())
                } else {
                    Toast.makeText(this,"Request error", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
            })
        /*  Sending Post Data  */
        {
            override fun getParams(): HashMap<String,String>{
                val map = HashMap<String,String>()
                map["username"] = username.toString()
                Log.d("Username: ", username.toString())
                map["select"] = select
                Log.d("Select: ", select.toString())
                map["amount"] = amount
                Log.d("Amount: ", amount.toString())
                map["request"] = "Sent"
                return map
            }
        }
        requestQueue.add(stringRequest)
    }

    /*  Check Total Amount Token Function  */
    private fun checkTotalAmount(usd: String, nzd: String, php: String) {
        result += usd.toString().toDouble()
        resultNewZealand += nzd.toString().toDouble()
        resultNewPhilippines += php.toString().toDouble()
        binding.tvCalculatorTokenAmount.text =
            String.format("%.2f", result).toDouble().toString()
        binding.tvCalculatorNewZealandAmount.text =
            String.format("%.2f", resultNewZealand).toDouble().toString()
        binding.tvCalculatorPhilippinesAmount.text =
            String.format("%.2f", resultNewPhilippines).toDouble().toString()
    }


    private fun resetTotalAmount() {
        result = 0.00
        resultNewZealand = result
        resultNewPhilippines = result
        binding.tvCalculatorTokenAmount.text =
            String.format("%.2f", result).toDouble().toString()
        binding.tvCalculatorNewZealandAmount.text =
            String.format("%.2f", resultNewZealand).toDouble().toString()
        binding.tvCalculatorPhilippinesAmount.text =
            String.format("%.2f", resultNewPhilippines).toDouble().toString()
    }

    /*  For Back  */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}