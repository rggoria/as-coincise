package com.example.alphaacemobile.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.alphaacemobile.Hostname
import com.example.alphaacemobile.R
import com.example.alphaacemobile.databinding.FragmentSearchBinding
import com.example.alphaacemobile.model.HomeTopModel
import org.json.JSONObject

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: ArrayAdapter<String>
    private var list: ArrayList<HomeTopModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        val url = Hostname.BASE_URL + "get_home_top_token"
        val requestQueue = Volley.newRequestQueue(requireActivity())

        val stringRequest = StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                list.clear()
                val jsonObject = JSONObject(response)
                val list_token = mutableListOf<String>()
                if (jsonObject.get("response").equals("Success")){
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0..jsonArray.length()-1) {
                        val jo = jsonArray.getJSONObject(i)
                        //store your variable
                        val token_name = jo.getString("token_name").toString()

                        list_token += token_name
                        setupListView(list_token)
                        setupSearchView(list_token)
                    }

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

        return binding.root
    }

    private fun setupListView(tokenList: List<String>) {
        adapter = ArrayAdapter(requireContext(), R.layout.text_color_layout, tokenList)
        binding.lvSearch.adapter = adapter
        binding.lvSearch.setOnItemClickListener { parent, view, position, id ->
            val element = adapter.getItem(position)

            val bundle = Bundle()
            bundle.putString("name", element)
            Toast.makeText(requireActivity(), "Clicked on token: ${element}", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.tokenDetailFragment, bundle)
        }
    }

    private fun setupSearchView(tokenList: List<String>) {
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val isMatchFound = tokenList.contains(query)
                val msg = if (isMatchFound) "Found: $query" else getString(R.string.no_match)
                Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }
}