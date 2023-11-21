package com.example.getiiabotpapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.getiiabotpapp.databinding.ActivityMainBinding
import com.example.getiiabotpapp.network.RetrofitClientInstance
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), CustomSpinner.OnSpinnerEventsListener {

    var selectedEnv: SpinnerItem? = null

    var selectedMobile: SpinnerItem? = null

    var items : Array<SpinnerItem?> = arrayOf()

    private lateinit var binding: ActivityMainBinding

    fun setEnvPhones() {
        Log.d("selectedEnv", " ${selectedEnv?.username} ${selectedEnv?.mobile}")
        if (selectedEnv == null) {
            items = arrayOf(
                null,
                // this is my users to test
                SpinnerItem("Mobile@23", "962796748648"),
                SpinnerItem("Mobile@42", "962777785412"),
                SpinnerItem("Mobile@44", "962788874521"),
                SpinnerItem("Mobile@55", "962771291528"),
                SpinnerItem("Mobile@70", "962771257450"),
                SpinnerItem("Mobile@11", "962799899573"),
                // this is for others
                //            SpinnerItem("UatTest", "962785811333"),
                ////            SpinnerItem("mobile@1", "962796305030"),
                //            SpinnerItem("mobile@3", "962777798541"),
                //            SpinnerItem("mobile@31", "962796230429"),
                //            SpinnerItem("mobile@36", "49003145"),
                //            SpinnerItem("Mobile@11", "962798829840"),
            )
        } else {
            if (selectedEnv?.mobile == "DEV") {
                items = arrayOf(
                    null,
                    // this is my users to test
                    SpinnerItem("Mobile@5", "962777787677"),
                    SpinnerItem("Mobile@6", "962777352311"),
                )
            } else {
                items = arrayOf(
                    null,
                    // this is my users to test
                    SpinnerItem("Mobile@23", "962796748648"),
                    SpinnerItem("Mobile@42", "962777785412"),
                    SpinnerItem("Mobile@44", "962788874521"),
                    SpinnerItem("Mobile@55", "962771291528"),
                    SpinnerItem("Mobile@70", "962771257450"),
                    SpinnerItem("Mobile@11", "962799899573"),
                    // this is for others
                    //            SpinnerItem("UatTest", "962785811333"),
                    ////            SpinnerItem("mobile@1", "962796305030"),
                    //            SpinnerItem("mobile@3", "962777798541"),
                    //            SpinnerItem("mobile@31", "962796230429"),
                    //            SpinnerItem("mobile@36", "49003145"),
                    //            SpinnerItem("Mobile@11", "962798829840"),
                )
            }
        }
        binding.spinner.adapter = SpinnerAdapter(this, items)
        Log.d("setEnvPhones", "items_count = ${items.size} -  adapter_count = ${binding.spinner.adapter.count}")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemsEnv = arrayOf(
            null,
            // this is my users to test
            SpinnerItem("env", "DEV"),
            SpinnerItem("env", "UAT"),
        )

        setEnvPhones()

        binding.envSpinner.setSpinnerEventsListener(this)
        binding.envSpinner.adapter = SpinnerAdapter(this, itemsEnv)
        binding.envSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (position != 0) {
                    val item: SpinnerItem? = itemsEnv[position]
                    selectedEnv = item
                    setEnvPhones()
                    binding.tvOtp.text = ""
                    binding.tvOtpMessage.text = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinner.setSpinnerEventsListener(this)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (position != 0) {
                    val item: SpinnerItem? = items[position]
                    selectedMobile = item
                    binding.tvOtp.text = ""
                    binding.tvOtpMessage.text = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val getOtpApi = RetrofitClientInstance(application).createService(OtpService::class.java)
        binding.btnGet.setOnClickListener {
            if (selectedMobile == null) return@setOnClickListener
            if (selectedEnv?.mobile == "DEV") {
                RetrofitClientInstance.baseUrl = RetrofitClientInstance.baseDev
            } else {
                RetrofitClientInstance.baseUrl = RetrofitClientInstance.baseUat
            }
            // launching a new coroutine
            GlobalScope.launch {
                try {
                    val response = getOtpApi.getOtp(selectedMobile!!.mobile).execute()
                    val otpObject: OtpModel = response.body()!!

                    this@MainActivity.runOnUiThread {
                        binding.tvOtp.text = if (otpObject.otp != null) "${otpObject.otp}" else "No OTP"
                        binding.tvOtpMessage.text = otpObject.message
                    }
                    Log.d("ayush: ", response.toString())
                } catch (ex: Exception) {
                    Log.d("ayush: ", ex.toString())
                }
            }
        }
    }

    class SpinnerAdapter(
        context: Context,
        items: Array<SpinnerItem?>
    ) : ArrayAdapter<SpinnerItem?>(context, 0, 0, items) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return spinnerItem(convertView, position, true)!!
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return spinnerItem(convertView, position, false)!!
        }

        override fun isEnabled(position: Int): Boolean {
            return position != 0
        }

        private fun spinnerItem(convertView: View?, position: Int, isDropDownView: Boolean): View? {
            val rowItem: SpinnerItem? = getItem(position)
            val holder: ViewHolder
            var rowview = convertView

//            if (rowview == null) {
            holder = ViewHolder()

            val layoutInflater =
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

            if (isDropDownView) {
                if (rowItem != null) {
                    rowview = layoutInflater.inflate(R.layout.spinner_item, null, false)
                    holder.txtName = rowview.findViewById(R.id.tv_name) as TextView
                } else {
                    rowview = layoutInflater.inflate(R.layout.spinner_item_selected, null, false)
                }
            } else {
                rowview = layoutInflater.inflate(R.layout.spinner_item_selected, null, false)
            }
            holder.txtMobile = rowview.findViewById(R.id.tv_mobile) as TextView

            rowview?.tag = holder
//            } else {
////                holder = rowview.tag as ViewHolder
//            }

            if (rowItem == null) {
                holder.txtMobile.text = "Select >>"
            } else {
                holder.txtMobile.text = rowItem.mobile
                if (isDropDownView)
                    holder.txtName.text = rowItem.username
            }

            return rowview
        }

        private class ViewHolder {
            lateinit var txtMobile: TextView
            lateinit var txtName: TextView
        }
    }

    override fun onPopupWindowOpened(spinner: Spinner) {
        spinner.background = AppCompatResources.getDrawable(this, R.drawable.bg_spinner_up)
    }

    override fun onPopupWindowClosed(spinner: Spinner) {
        spinner.background = AppCompatResources.getDrawable(this, R.drawable.bg_spinner_down)
    }

}