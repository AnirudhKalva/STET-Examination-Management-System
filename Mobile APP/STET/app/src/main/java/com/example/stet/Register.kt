package com.example.stet

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Register : AppCompatActivity() {
    //for register page
    var a=0
    var b=0
    var c=0
    var d=0
    var ses=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.register)
        val progress = ProgressDialog(this@Register)
        progress.setMessage(getString(R.string.loading))
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.isIndeterminate = true
        progress.dismiss()
        val phone: String = intent.getStringExtra("phone")
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)

        val map: HashMap<String?, String?> = HashMap()
        map["Phone"] = phone
        val sharedPreferences = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val cookie:String?=sharedPreferences.getString("user_cookie","")
        val retrofitx: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        //check session
        var retrofitInterfacex: RetrofitInterface = retrofitx.create(RetrofitInterface::class.java)
        val callx: Call<Void?>? = cookie?.let { retrofitInterfacex.executeLogout(it) }

        callx!!.enqueue(object : Callback<Void?> {
            override fun onResponse(
                call: Call<Void?>?,
                response: Response<Void?>
            ) {
                if (response.code() == 201) {

                    val myEditx = sharedPreferences.edit()
                    myEditx.putBoolean("login", false).apply()
                    myEditx.putString("phone", "").apply()
                    myEditx.putString("user_cookie", "").apply()
                    Toast.makeText(
                        this@Register, getString(R.string.logkro),
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this@Register, MainActivity::class.java)
                    startActivity(i)
                } else if (response.code() == 200) {

                    ses=1
                } else {
                    Toast.makeText(
                        this@Register, getString(R.string.toastslowinternet),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

            override fun onFailure(
                call: Call<Void?>?,
                t: Throwable
            ) {
                Toast.makeText(
                    this@Register, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()

            }

        })

            val getPersonalDetailsCall: Call<Personal?>? = cookie?.let { retrofitInterface.getPersonal(it, map) }
            getPersonalDetailsCall!!.enqueue(object : Callback<Personal?> {
                override fun onResponse(
                    call: Call<Personal?>?,
                    response: Response<Personal?>
                ) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            register_personal.background = getDrawable(R.drawable.button_shape2)
                            a = 1
                        } else {
                            progress.dismiss()
                        }
                    }
                }

                override fun onFailure(
                    call: Call<Personal?>?,
                    t: Throwable
                ) {
                    Log.d("Failure", t.message)
                    Toast.makeText(
                        this@Register, t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

            val map2: HashMap<String?, String?> = HashMap()
            map2["Phone"] = phone
            //get education
                val call2: Call<Education?>? = retrofitInterface.getEducation(cookie, map2)
            call2!!.enqueue(object : Callback<Education?> {
                override fun onResponse(
                    call: Call<Education?>?,
                    response: Response<Education?>
                ) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            register_professional.background = getDrawable(R.drawable.button_shape2)
                            b = 1
                            progress.dismiss()
                        }
                    } else {

                    }
                }

                override fun onFailure(
                    call: Call<Education?>?,
                    t: Throwable
                ) {
                    Log.d("Failure", t.message)
                    Toast.makeText(
                        this@Register, t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            //get payment
            val call3: Call<Payment> = retrofitInterface.getpayment(cookie, phone)
            call3!!.enqueue(object : Callback<Payment> {
                override fun onResponse(
                    call: Call<Payment>,
                    response: Response<Payment>
                ) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            register_payment.background = getDrawable(R.drawable.button_shape2)
                            c = 1

                        }
                    } else {

                    }
                }

                override fun onFailure(
                    call: Call<Payment>,
                    t: Throwable
                ) {
                    Log.d("Failure", t.message)
                    Toast.makeText(
                        this@Register, t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

        //get payment
        val call4: Call<Void> = retrofitInterface.getDocuemnts(cookie, phone)
        call4!!.enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.code() == 200) {
                        register_documents.background = getDrawable(R.drawable.button_shape2)
                        d = 1
                } else {

                }
            }

            override fun onFailure(
                call: Call<Void>,
                t: Throwable
            ) {
                Log.d("Failure", t.message)
                Toast.makeText(
                    this@Register, t.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })


//            if (sharedPreferences.getBoolean("documents", false)) {
//                register_documents.background = getDrawable(R.drawable.button_shape2)
//                d = 1
//            }
            register_personal.setOnClickListener {
                val i = Intent(this, PersonalInfoFormActivity::class.java)
                i.putExtra("phone", phone)
                startActivity(i)
            }
            register_professional.setOnClickListener {
                val i = Intent(this, ProfessionalInfoFormActivity::class.java)
                i.putExtra("phone", phone)
                startActivity(i)
            }
            register_payment.setOnClickListener {
                val i = Intent(this, PaymentActivity::class.java)
                i.putExtra("phone", phone)
                startActivity(i)
            }
            register_documents.setOnClickListener {
                val i = Intent(this, FIleUploadActivity::class.java)
                i.putExtra("phone", phone)
                startActivity(i)
            }
            register_submit.setOnClickListener {
                if (a == 1 && b == 1 && d == 1) {
                    val i = Intent(this, FinalRegistrationActivity::class.java)
                    i.putExtra("phone", phone)
                    startActivity(i)
                } else {
                    Toast.makeText(this@Register, getString(R.string.allfour), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            register_back.setOnClickListener {
                val i = Intent(this, ExamRegisterClickActivity::class.java)
                i.putExtra("phone", phone)
                startActivity(i)
            }

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