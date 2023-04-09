package com.example.alphaacemobile.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.model.CalculatorModel_simulation
import org.json.JSONObject
import java.math.RoundingMode
import java.text.DecimalFormat

@Suppress("DEPRECATION")
class CalculatorAdapter_simulation(val c: Context, val calculatorList: ArrayList<CalculatorModel_simulation>) :RecyclerView.Adapter<CalculatorAdapter_simulation.CalculatorViewHolder> () {
    private lateinit var preferences: SharedPreferences

    inner class CalculatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var calculatorToken: TextView
        var calculatorAmount: TextView
        var calculatorValue: TextView
        var calculatorMenu: ImageView

        init {
            calculatorToken = itemView.findViewById(R.id.tvCalculatorSelect)
            calculatorAmount = itemView.findViewById(R.id.tvCalculatorAmount)
            calculatorValue = itemView.findViewById(R.id.ivCalculatorSelectedTotalValue)
            calculatorMenu = itemView.findViewById(R.id.etCalculatorMenu)
            calculatorMenu.setOnClickListener {
                popupMenu(it)
            }
        }


        private fun popupMenu(v: View) {

            Log.d("CALCULATOR_SIM_ADAPTOR_URL", "${Hostname.BASE_URL}")

            val position = calculatorList[adapterPosition]
            val popupMenu = PopupMenu(c, v)
            popupMenu.inflate(R.menu.calculator_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.iCalculatorEdit->{
                        val v = LayoutInflater.from(c).inflate(R.layout.edit_wallet_item, null)

                        /*  Setup Functions  */
                        var calculatorAmount = v.findViewById<EditText>(R.id.etCalculatorAmount)

                        AlertDialog.Builder(c, R.style.MyDialogStyle)
                            .setView(v)
                            .setPositiveButton("OK"){
                                    dialog,_->
                                position.select = calculatorToken.text as String
                                position.amount = calculatorAmount.text.toString()
                                if (position.amount == ""){
                                    dialog.dismiss()
                                    Toast.makeText(c, "Failed", Toast.LENGTH_SHORT).show()
                                } else {
                                    updateWallet(position.select, position.amount)
                                    notifyDataSetChanged()
                                    dialog.dismiss()
                                }
                            }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    R.id.iCalculatorRemove->{
                        AlertDialog.Builder(c, R.style.MyDialogStyle)
                            .setTitle("Remove")
                            .setIcon(R.drawable.ic_delete)
                            .setMessage("Are you sure to remove this Information")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                position.select = calculatorToken.text as String
                                position.amount = calculatorAmount.text.toString()
                                removeWallet(position.select)
                                calculatorList.removeAt(adapterPosition)
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    else-> true
                }
            }
            popupMenu.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu, true)
        }

        /*  Remove Wallet  */
        private fun removeWallet(select: String) {
            /*  Setup SharedPreference  */
            preferences = c.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
            var usernameSP = preferences.getString("username", "").toString()

            val url = Hostname.BASE_URL + "remove_wallet_token_simulation"
            val requestQueue = Volley.newRequestQueue(c)
            val stringRequest = object : StringRequest(Method.POST,url,
                Response.Listener { response ->

                    Log.d("CALCULATOR_SIM_ADAPTOR_REM", response)

                    val jsonObject = JSONObject(response)
                    if(jsonObject.get("response").equals("Successful")) {
                        Toast.makeText(c, "Remove Complete!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(c,"There was a problem occurred. Please try again later. ",Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener { error ->
                    Toast.makeText(c,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                })
            /*  Sending Post Data  */
            {
                override fun getParams(): HashMap<String,String>{
                    val map = HashMap<String,String>()
                    map["usernameSP"] = usernameSP
                    map["select"] = select
                    map["status"] = "disabled"
                    map["request"] = "Sent"
                    return map
                }
            }
            requestQueue.add(stringRequest)
        }

        /*  Update Wallet  */
        private fun updateWallet(select: String, amount: String) {
            /*  Setup SharedPreference  */
            preferences = c.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
            var usernameSP = preferences.getString("username", "").toString()

            val url = Hostname.BASE_URL + "update_wallet_token_simulation"
            val requestQueue = Volley.newRequestQueue(c)
            val stringRequest = object : StringRequest(Method.POST,url,
                Response.Listener { response ->

                    Log.d("CALCULATOR_SIM_ADAPTOR_UPD", response)

                    val jsonObject = JSONObject(response)
                    if(jsonObject.get("response").equals("Successful")) {
                        Toast.makeText(c, "Edit Complete!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(c,"Request error", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener { error ->
                    Toast.makeText(c,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                })
            /*  Sending Post Data  */
            {
                override fun getParams(): HashMap<String,String>{
                    val map = HashMap<String,String>()
                    map["usernameSP"] = usernameSP
                    map["select"] = select
                    map["amount"] = amount
                    map["request"] = "Sent"
                    return map
                }
            }
            requestQueue.add(stringRequest)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalculatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calculator_token_item, parent, false)
        return CalculatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalculatorViewHolder, position: Int) {
        val item = calculatorList[position]
        var result: Double = 0.00
        var resultsimulate: Double = 0.00
        holder.apply {
            calculatorToken.text = "${item.select}"
            calculatorAmount.text = "Input: ${item.amount}"

            val url = Hostname.BASE_URL + "get_selected_token_simulation"
            val requestQueue = Volley.newRequestQueue(c)
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING

            val stringRequest = object : StringRequest(Method.POST,url,
                Response.Listener { response ->

                    Log.d("CALCULATOR_SIM_ADAPTOR_SEL", response)

                    val jsonObject = JSONObject(response)

                    if(jsonObject.get("response").equals("Approved")) {
                        val sPrice = jsonObject.get("curr_prc").toString()
                        val usd = sPrice.split(' ')[0]
                        calculatorValue.text = "Total: ${df.format(item.amount.toDouble() * usd.toDouble())}"

                    } else if(jsonObject.get("response").equals("Failed")) {
                        Toast.makeText(c, "Please try again!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(c,"Request error",Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener { error ->
                    Toast.makeText(c,"Server is down, Try again later.",Toast.LENGTH_SHORT).show()
                })
            {
                override fun getParams(): HashMap<String, String>
                {
                    val map = HashMap<String,String>()
                    map["request"] = "Sent"
                    map["token_name"] = item.select
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

    override fun getItemCount(): Int {
        return calculatorList.size
    }
}