package com.example.alphaacemobile.activities

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.adapter.CalculatorAdapter_simulation
import com.example.alphaacemobile.adapter.DialogAdapter
import com.example.alphaacemobile.adapter.FeedbackAdapter
import com.example.alphaacemobile.databinding.ActivityCalculatorsimulationBinding
import com.example.alphaacemobile.model.CalculatorModel
import com.example.alphaacemobile.model.CalculatorModel_simulation
import com.example.alphaacemobile.model.HomeTopModel
import com.xwray.groupie.GroupieAdapter
import com.zues.searchable_spinner.SearchableSpinner
import org.json.JSONObject
import java.text.DecimalFormat

class CalculatorSimulationActivity : AppCompatActivity() {
    private var result: Double = 0.00
    private var pong = arrayOf<Double>()
    private var resultNewZealand: Double = 0.00
    private var resultNewPhilippines: Double = 0.00
    private lateinit var binding: ActivityCalculatorsimulationBinding
    private lateinit var calculatorList: ArrayList<CalculatorModel_simulation>
    private lateinit var calculatorAdapter_simulation: CalculatorAdapter_simulation
    /*  Setup SharedPreference  */
    private lateinit var preferences: SharedPreferences
    private lateinit var groupieAdapter: GroupieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorsimulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CALCULATOR_SIM_URL", "${Hostname.BASE_URL}")

        /*  Setup SharedPreference details  */
        preferences = this.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "").toString()
        Log.d("CALCULATOR_SIM_URL", "$username")

        /*  Setup Action Bar details  */
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);
        supportActionBar!!.title = "Calculator Simulation"

        /*  Setup List  */
        calculatorList = ArrayList()

        /*  Setup Layout Palettes  */
        val addBtn = binding.fabCalculatorAdd
        var rvCalculator = binding.rvCalculator

        var pong = result

        /*  Setup Adapter  */
        calculatorAdapter_simulation = CalculatorAdapter_simulation(this, calculatorList)

        /*  Setup RecyclerView  */
        rvCalculator.layoutManager = LinearLayoutManager(this)
        rvCalculator.adapter = calculatorAdapter_simulation

        /*  Setup Get Wallet Database  */
        Log.d("CALCULATOR_MARK", "goes to getWallet and simulationResult")
        getWallet(username)

        binding.btnCalculatorCalculate.setOnClickListener {
            Log.d("CALCULATOR_MARK", "button calculate click")
            Log.d("CALCULATOR_MARK", "goes to getWallet")
            getWallet(username)
        }

        binding.btnsimulate.setOnClickListener {
            Log.d("CALCULATOR_MARK", "button generate 30 click")
            Log.d("CALCULATOR_MARK", "goes to simulationResult")
            simulationResult(username)
        }


        /*  Setup Add button  */
        addBtn.setOnClickListener {
            Log.d("CALCULATOR_MARK", "button add click")
            Log.d("CALCULATOR_MARK", "goes to addInfo")
            addInfo()
        }
    }


    /*  Get Wallet (Get Latest Data From The Database And Get Total Amount)  */
    private fun getWallet(username: String): Array<Double> {
                    val url = Hostname.BASE_URL + "get_wallet_token_simulation"
        val requestQueue = Volley.newRequestQueue(this)
        resetTotalAmount()
        Log.d("CALCULATOR_MARK", "goes to resetTotalAmount")
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_SIM_RESPONSE", response)

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
                        val sPrice = jo.getString("token_price").toString()
                        val usd = sPrice.split(' ')[0];
                        val php = sPrice.split(' ')[1];
                        val nzd = sPrice.split(' ')[2];
                        val sStatus = jo.getString("status").toString()
                        if (sStatus == "active"){
                            // updates the data
                            calculatorAdapter_simulation.notifyDataSetChanged()
                            calculatorList.add(CalculatorModel_simulation("$sSelect", "$sAmount"))
                            pong = checkTotalAmount((sAmount.toDouble() * usd.toDouble()).toString(), (sAmount.toDouble() * nzd.toDouble()).toString(), (sAmount.toDouble() * php.toDouble()).toString())
                            Log.d("CALCULATOR_MARK", "goes to checkTotalAmount")
                        } else if (sStatus != "active") {
                            continue
                        } else {
                            binding.cvCalculatorEmpty.visibility = View.VISIBLE
                        }
                    }
                } else if(jsonObject.get("response").equals("Failed")) {
                    binding.cvCalculatorEmpty.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this,"There was a problem occurred. Please try again later. ",
                        Toast.LENGTH_SHORT).show()
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
        return pong
    }

    /*  Get Wallet (Get Latest Data From The Database Only)  */
    private fun updateWallet(username: String) {
        val url = Hostname.BASE_URL + "get_wallet_token_simulation"
        val requestQueue = Volley.newRequestQueue(this)
        resetTotalAmount()
        Log.d("CALCULATOR_MARK", "goes to resetTotalAmount")
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_SIM_UPD", response)

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
                        val sPrice = jo.getString("token_price").toString()
                        val sStatus = jo.getString("status").toString()
                        if (sStatus == "active"){
                            // updates the data
                            calculatorAdapter_simulation.notifyDataSetChanged()
                            calculatorList.add(CalculatorModel_simulation("$sSelect", "$sAmount"))
                            Log.d("CALCULATOR_MARK", "list down all active tokens")
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
                    Toast.makeText(this,"Request error", Toast.LENGTH_SHORT).show()
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
        val url = Hostname.BASE_URL + "get_home_top_token_simulation"
        val requestQueue = Volley.newRequestQueue(this)
        val list_token = mutableListOf<String>()
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->

                Log.d("CALCULATOR_SIM_SPINNER", response)

                list.clear()
                val jsonObject = JSONObject(response)
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")

                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val token_name = jo.getString("token_name").toString()
                        val token_prc = jo.getString("token_price").toString()

                        list_token += token_name
                    }
                    /*  Setup Token List  */
                    calculatorSelect.setItems(list_token)
                    /*  Setup SearchableSpinner  */
                    calculatorSelect.setOnItemSelectListener(object : SearchableSpinner.SearchableItemListener {
                        override fun onItemSelected(view: View?, position: Int) {
                            Toast.makeText(this@CalculatorSimulationActivity, "${calculatorSelect.mSelectedItem}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onSelectionClear() {
                            Toast.makeText(this@CalculatorSimulationActivity, "Clear", Toast.LENGTH_SHORT).show()
                        }
                    })
                }else{
                    Toast.makeText(this,jsonObject.get("response").toString(), Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                Toast.makeText(this,error.toString(), Toast.LENGTH_SHORT).show()
            })
        requestQueue.add(stringRequest)

        /*  Setup Dialog  */
        val addDialog = AlertDialog.Builder(this, R.style.MyDialogStyle)
        Log.d("CALCULATOR_MARK", "goes to addInfo Dialog")
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
                Log.d("CALCULATOR_MARK", "goes to addWallet")
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

        val url = Hostname.BASE_URL + "insert_wallet_data_simulation"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                Log.d("CALCULATOR_SIM_ADD", response)

                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Successful")) {
                    Toast.makeText(this, "Added Token!", Toast.LENGTH_SHORT).show()
                    /*  First List Must Be Cleared  */
                    calculatorList.clear()
                    /*  Second It Must Notify The Changes  */
                    calculatorAdapter_simulation.notifyDataSetChanged()
                    /*  Third Add The New List  */
                    calculatorList.add(CalculatorModel_simulation("$select", "$amount"))
                    /*  Lastly Call getWallet To Get The List Of Data  */
                    Log.d("CALCULATOR_MARK", "goes to updateWallet")
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
                map["select"] = select
                map["amount"] = amount
                map["request"] = "Sent"
                return map
            }
        }
        requestQueue.add(stringRequest)
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

    /*  Check Total Amount Token Function  */
    private fun checkTotalAmount(usd: String, nzd: String, php: String): Array<Double> {
        var cur = arrayOf<Double>()
        result += usd.toDouble()
        cur += result
        resultNewZealand += nzd.toDouble()
        cur += resultNewZealand
        resultNewPhilippines += php.toDouble()
        cur += resultNewPhilippines
        binding.tvCalculatorTokenAmount.text =
            String.format("%.2f", result).toDouble().toString()
        binding.tvCalculatorNewZealandAmount.text =
            String.format("%.2f", resultNewZealand).toDouble().toString()
        binding.tvCalculatorPhilippinesAmount.text =
            String.format("%.2f", resultNewPhilippines).toDouble().toString()
        Log.d("CALCULATOR_MARK", "result: $result")
        return cur
    }

    // 30 days
    private fun simulationResult(username: String){
        Log.d("CALCULATOR_MARK", "goes to dialog")
        dialog()
    }

    private fun dialog(){
        preferences = this.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
        var username = preferences.getString("username", "").toString()
        /*  Setup Variables  */
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_alert, null)
        val Invest = view.findViewById<TextView>(R.id.tvTokenInvestment)
        val Stats = view.findViewById<TextView>(R.id.tvTokenStatus)
        val Percent = view.findViewById<TextView>(R.id.tvTokenPercent)
        val info = view.findViewById<TextView>(R.id.tvInfo)
        val indicator = view.findViewById<LinearLayout>(R.id.llDialog)
        val df = DecimalFormat("#.##")

        val url = Hostname.BASE_URL + "testing_total"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(Method.POST,url,
            Response.Listener { response ->

                // Debugging purposes
                Log.d("CALCULATE_SIM_30", response)

                val jsonObject = JSONObject(response)
                if(jsonObject.get("response").equals("Approved")) {
                    if (jsonObject.get("wallet_sim_status").equals("Gain")) {
                        Stats.setTextColor(Color.parseColor("#00FF00"))
                        Percent.setTextColor(Color.parseColor("#00FF00"))
                        info.text = "Based on the 30 day investment trial period, It states that you did well in your investment. meaning you gained profit over the span of  30 days on holding the inserted token."
                    } else if (jsonObject.get("wallet_sim_status").equals("Loss"))  {
                        Stats.setTextColor(Color.parseColor("#FF0000"))
                        Percent.setTextColor(Color.parseColor("#FF0000"))
                        info.text = "Based on the 30 day investment trial period, It states that you didn't do well in your investment. meaning you loss profit over the 30 day frame on holding the inserted token."
                    } else {
                        indicator.visibility = View.GONE
                        info.text = "Based on the 30 day investment trial period, It states that you didn't select any tokens yet. meaning there are no tokens to be processed."
                    }
                    Stats.text = jsonObject.get("wallet_sim_status").toString()
                    Percent.text = df.format(jsonObject.get("wallet_sim_percentage")) + "%"
                    Invest.text = df.format(jsonObject.get("day_29_total").toString().toDouble() - jsonObject.get("day_1_total").toString().toDouble()).toString()

                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val sSelect = jo.getString("token_name").toString()
                        val sOverall_percentage = jo.getString("overall_percentage").toString()
                        val sStatus = jo.getString("status").toString()
                        if (sStatus == "active"){
                            groupieAdapter.add(DialogAdapter("$sSelect", "$sOverall_percentage"))
                        } else if (sStatus != "active"){
                            continue
                        } else {
                            Toast.makeText(this,"There are no tokens to be processed.",Toast.LENGTH_SHORT).show()
                            //binding.cvCalculatorEmpty.visibility = View.VISIBLE
                        }

                    }
                } else {
                    indicator.visibility = View.GONE
                    info.text = "Based on the 30 day investment trial period, It states that you didn't select any tokens yet. meaning there are no tokens to be processed."
                    Toast.makeText(this,"There are no tokens to be processed.",Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(this,error.toString(),Toast.LENGTH_SHORT).show()
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


        val addDialog = AlertDialog.Builder(this)
        addDialog.setView(view)
        addDialog.setPositiveButton("Confirm"){
                dialog,_->

            dialog.dismiss()
        }
        addDialog.create()
        addDialog.show()

        Log.d("CALCULATOR_MARK", "CALLS GROUPIE")
        groupieAdapter = GroupieAdapter()
        view.findViewById<RecyclerView>(R.id.rvDialog1).adapter = groupieAdapter
    }
}