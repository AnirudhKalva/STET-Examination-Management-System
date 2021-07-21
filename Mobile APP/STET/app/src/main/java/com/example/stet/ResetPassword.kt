package com.example.stet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.reset.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

class ResetPassword :AppCompatActivity(){
    //reset password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.reset)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        var retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val phone:String=intent.getStringExtra("Phone")
        reset_back.setOnClickListener {
            val i= Intent(this,MainActivity::class.java)
            startActivity(i)
        }

        reset_password.setOnClickListener {
            if(page_reset_Edtconfirmpass.text.toString()==page_reset_Edtpass.text.toString()) {
                if(isValidPassword(page_reset_Edtpass)==0){

                    val map: HashMap<String, String> = HashMap()
                    map["phone"] = phone
                    map["password"]=page_reset_Edtpass.text.toString()
                    val call: Call<Void?>? = retrofitInterface.resetpassword(map)
                    //set new password
                    call!!.enqueue(object : Callback<Void?> {
                        override fun onResponse(
                            call: Call<Void?>?,
                            response: Response<Void?>?
                        ) {
                            if (response != null) {
                                if (response.code() == 200) {
                                    /**
                                     * @todo verify toast success message is comming or not
                                     */
                                    Toast.makeText(
                                        this@ResetPassword, getString(R.string.passwordreset),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val i = Intent(this@ResetPassword, MainActivity::class.java)
                                    i.putExtra("phone", phone)
                                    startActivity(i)
                                } else if (response.code() == 404) {
                                    Toast.makeText(
                                        this@ResetPassword, getString(R.string.phonenotfound),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<Void?>?,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@ResetPassword,  getString(R.string.poorinternet),
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    })

                }
            }
            else
            {
                Toast.makeText(
                    this@ResetPassword, getString(R.string.mustbesame),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun validNumber(editText: EditText, d: Int): Int {
        var x = 0
        editText.text.toString().validator()
            .nonEmpty()
            .validNumber()
            .onlyNumbers()
            .maxLength(d)
            .minLength(d)
            .addErrorCallback {
                editText.error = getString(R.string.enterupto) + d + getString(R.string.digitonlu)
                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
    }
    //valid password
    private fun isValidPassword(editText: EditText): Int {
        var x = 0
        editText.text.toString().validator()
            .minLength(6)
            .nonEmpty()
            .atleastOneSpecialCharacters()
            .atleastOneNumber()
            .atleastOneLowerCase()
            .atleastOneUpperCase()
            .addErrorCallback {
                editText.error =
                    getString(R.string.validpass)
                x = 1
            }
            .addSuccessCallback {
                x = 0
            }
            .check()
        return x
    }
    private fun setLocate(Lang: String) {
        val locale = Locale(Lang)
        val config = Configuration()

        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", Lang)
        editor.apply()
    }
    private fun loadLocate() {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", "")
        if (language != null) {
            setLocate(language)
        }
    }
}
