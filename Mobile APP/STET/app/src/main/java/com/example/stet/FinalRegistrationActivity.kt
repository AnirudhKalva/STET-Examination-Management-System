package com.example.stet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

import kotlinx.android.synthetic.main.final_registration.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.TimeUnit

class FinalRegistrationActivity : AppCompatActivity() {
    //this file is for final registration .
    var ses=0
    private var mAuth: FirebaseAuth? = null     //firbase auth
    var codeSent: String? = null
    var P = 0
//    var auth2: FirebaseAuth = FirebaseAuth.getInstance()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_registration)
        loadLocate()
        final_registration__pb.progress = 100
        val phone: String = intent.getStringExtra("phone")
        mAuth = FirebaseAuth.getInstance()
        val map: HashMap<String?, String?> = HashMap()
        val sharedPreferences = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        //shared preference for user_cookie
        val cookie:String?=sharedPreferences.getString("user_cookie","")
        map["venue"] = "KMIT, Narayanaguda, Hyderabad"
        map["eno"]=phone
        //retofit builder
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val sharedPreferencesx = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val retrofitx: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        //retrofit instance
        val retrofitInterfacex: RetrofitInterface = retrofitx.create(RetrofitInterface::class.java)
        val cookiex:String?=sharedPreferencesx.getString("user_cookie","")
        val callx: Call<Void?>? = cookiex?.let { retrofitInterfacex.executeLogout(it) }
        //callback function
        callx!!.enqueue(object : Callback<Void?> {
            //on response
            override fun onResponse(
                call: Call<Void?>?,
                response: Response<Void?>
            ) {
                //code 201 means session expired
                if (response.code() == 201) {

                    val myEditx = sharedPreferencesx.edit()
                    myEditx.putBoolean("login", false).apply()
                    myEditx.putString("phone", "").apply()
                    myEditx.putString("user_cookie", "").apply()
                    Toast.makeText(
                        this@FinalRegistrationActivity, getString(R.string.logkro),
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this@FinalRegistrationActivity, MainActivity::class.java)
                    startActivity(i)
                } else if (response.code() == 200) {

                        ses=1
                } else {
                    Toast.makeText(
                        this@FinalRegistrationActivity, getString(R.string.toastslowinternet),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
            //on failure
            override fun onFailure(
                call: Call<Void?>?,
                t: Throwable
            ) {
                Toast.makeText(
                    this@FinalRegistrationActivity, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()

            }

        })

            val retrofitInterface2: RetrofitInterface =
                retrofit.create(RetrofitInterface::class.java)
            val y: Int = Calendar.getInstance().get(Calendar.YEAR)
            val year: String = y.toString()
    map["exam_date"] = year;
            val call: Call<Schedule>? = cookie?.let { retrofitInterface2.timeline(it, year) }

            call!!.enqueue(object : Callback<Schedule> {
                override fun onResponse(
                    call: Call<Schedule>,
                    response: Response<Schedule>
                ) {
                }

                override fun onFailure(
                    call: Call<Schedule>,
                    t: Throwable
                ) {
                }

            })
            val map1: HashMap<String?, String?> = HashMap()
            map1["Phone"] = phone
            //return response body to get personal details from db
            val call1: Call<Personal?>? = retrofitInterface2.getPersonal(cookie, map1)
            call1!!.enqueue(object : Callback<Personal?> {
                override fun onResponse(
                    call: Call<Personal?>?,
                    response: Response<Personal?>
                ) {
                    if (response.code() == 200) {

                        val result = response.body()
                        //response body to get personal details from db
                        if (result != null) {
                            map["fname"] = result.Fname
                            map["mname"] = result.Mname
                            map["lname"] = result.Lname
                            map["ffname"] = result.FHFname
                            map["fmname"] = result.FHMname
                            map["flname"] = result.FHLname
                            map["dob"] = result.DOB
                            map["gender"] = result.Gender
                            map["aadhar"] = result.Aadhar
                            map["community"] = result.Community
                            map["hno"] = result.Hno
                            map["area"] = result.Area
                            map["district"] = result.District
                            map["state"] = result.State
                            map["pincode"] = result.Pincode
                            map["email"] = result.Email
                            map["phone"] = result.Phone
                        }
                    }
                }

                override fun onFailure(
                    call1: Call<Personal?>?,
                    t: Throwable
                ) {
                    Log.d("Failure", t.message)
                    Toast.makeText(
                        this@FinalRegistrationActivity, t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            val map3: HashMap<String?, String?> = HashMap()
            map3["Phone"] = phone
            //return response body to get academics details from db
            val call2: Call<Education?>? = retrofitInterface2.getEducation(cookie, map3)
            call2!!.enqueue(object : Callback<Education?> {
                override fun onResponse(
                    call: Call<Education?>?,
                    response: Response<Education?>
                ) {
                    if (response.code() == 200) {

                        val result = response.body()

                        if (result != null) {
                            map["exam"] = result.ApplicationCategory

                        }


                    } else {

                        Toast.makeText(
                            this@FinalRegistrationActivity, getString(R.string.toastslowinternet),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                override fun onFailure(
                    call: Call<Education?>?,
                    t: Throwable
                ) {
                    Log.d("Failure", t.message)
                    Toast.makeText(
                        this@FinalRegistrationActivity, t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })


    final_registration_btn__submit.setOnClickListener {
            if (P == 1) {
                Toast.makeText(this, getString(R.string.registrationsuccessfull), Toast.LENGTH_SHORT).show()
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val formatted = current.format(formatter)
                map["Date"] = formatted
                //post request to store admit card info in db
                val retrofitInterface: RetrofitInterface =
                    retrofit.create(RetrofitInterface::class.java)
                val call3: Call<Void?>? = cookie.let { it1 -> retrofitInterface.timings(it1,map) }
                call3!!.enqueue(object : Callback<Void?> {
                    override fun onResponse(
                        call: Call<Void?>?,
                        response: Response<Void?>
                    ) {
                        if (response.code() == 200) {

                            val i = Intent(this@FinalRegistrationActivity, ExamRegisterClickActivity::class.java)
                            i.putExtra("phone", phone)
                            startActivity(i)
                        }
                    }

                    override fun onFailure(
                        call: Call<Void?>?,
                        t: Throwable
                    ) {
                        Log.d("Failure", t.message)
                        Toast.makeText(
                            this@FinalRegistrationActivity, t.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })


            }
        }
        page_8_sendotp.setOnClickListener {
            sendVerificationCode(phone)     //send phone otp
        }
        final_registration_btn__verify.setOnClickListener {
            if (codeSent != null) {
                verifySignInCode()  //verify otp
            }

        }
    }

    private fun verifySignInCode() {
        val code: String = page_8_enter_otp.text.toString()
        val credential = PhoneAuthProvider.getCredential(codeSent!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.verificationsuccessfull),
                        Toast.LENGTH_LONG
                    ).show()
                    final_registration_btn__verify.text = getString(R.string.Verified)
                    P = 1
                    final_registration_btn__verify.background = getDrawable(R.drawable.button_shape2)
                    page_8_enter_otp.visibility = View.INVISIBLE
                    page_8_sendotp.visibility = View.INVISIBLE
                    page_8_otp.visibility = View.INVISIBLE


                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.incorrectcode), Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }
    }

    private fun sendVerificationCode(phn: String) {

        val phone = "+91$phn"
        if (phone.isEmpty()) {
            return
        }
        if (phone.length < 10) {
            return
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone,
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )

    }

    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

            }
            //failure
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("error", e.toString())
            }
            //otp sent
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                codeSent = s
                page_8_sendotp.text = getString(R.string.global__resend_otp)
            }
        }
    //setlocale for language
    private fun setLocate(Lang: String) {
        val locale = Locale(Lang)
        val config = Configuration()

        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", Lang)
        editor.apply()
    }
    //landlocate  for language
    private fun loadLocate() {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", "")
        if (language != null) {
            setLocate(language)
        }
    }
}
