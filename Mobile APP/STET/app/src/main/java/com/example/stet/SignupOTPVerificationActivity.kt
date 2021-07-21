package com.example.stet

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.android.synthetic.main.signup_otp_verification_activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class SignupOTPVerificationActivity : AppCompatActivity() {

    //verification of email,phn,aadhar and signup

    private var mAuth: FirebaseAuth? = null
    var codeSent: String? = null
    lateinit var Email: String
    var Password: String ="Abc!123"
    var E: Int = 0
    var P: Int = 0
    var check = 0
    var ses=0

    var auth2: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.signup_otp_verification_activity)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)

        mAuth = FirebaseAuth.getInstance()
        var name: String = intent.getStringExtra("Name")
        var email: String = intent.getStringExtra("Email")
        var phone: String = intent.getStringExtra("Phone")
        var pass: String = intent.getStringExtra("Password")
        Email = email
        Password = pass
        page_2_email.text = "Email ID : $email"
        page_2_get_phn.text = "Phone Number : $phone"
        page_2_phn_verify.visibility = View.INVISIBLE
        page_2_email_verify.visibility = View.INVISIBLE
        val sharedPreferences = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val myEdit = sharedPreferences.edit()
        page_2_next.setOnClickListener {

            when {
                P != 1 -> {
                    Toast.makeText(this, getString(R.string.phoneverify), Toast.LENGTH_LONG).show()
                }
                E != 1 -> {
                    Toast.makeText(this, getString(R.string.emailverify), Toast.LENGTH_LONG).show()
                }
                else -> {
                    val progress = ProgressDialog(this)
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progress.isIndeterminate = true
                    progress.setMessage(getString(R.string.registering))
                    progress.show()

                    val map: HashMap<String, String> = HashMap()

                    map["name"] = name
                    map["email"] = email
                    map["password"] = pass
                    map["phone"] = phone
                    //execute signup
                    val call: Call<Void?>? = retrofitInterface.executeSignup(map)
                    call!!.enqueue(object : Callback<Void?> {
                        override fun onResponse(
                            call: Call<Void?>?,
                            response: Response<Void?>
                        ) {
                            if (response.code() == 200) {
                                val cookie = response.headers()["Set-Cookie"]
                                if (cookie != null) {
                                    myEdit.putString("user_cookie", cookie.split(";")[0]).apply()
                                }
                                Toast.makeText(
                                    this@SignupOTPVerificationActivity,
                                    getString(R.string.signupsuccess), Toast.LENGTH_LONG
                                ).show()
                                progress.dismiss()
                                val i = Intent(this@SignupOTPVerificationActivity, ExamRegisterClickActivity::class.java)
                                i.putExtra("phone", phone)


                                startActivity(i)
                            } else if (response.code() == 400) {
                                Toast.makeText(
                                    this@SignupOTPVerificationActivity,
                                    getString(R.string.alreadyregist), Toast.LENGTH_LONG
                                ).show()
                                progress.dismiss()
                            }
                        }

                        override fun onFailure(
                            call: Call<Void?>?,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@SignupOTPVerificationActivity,  getString(R.string.poorinternet),
                                Toast.LENGTH_LONG
                            ).show()
                            progress.dismiss()
                        }
                    })

                }
            }
        }

        page_2_back.setOnClickListener {
            val i = Intent(this, SignupActivity::class.java)
            startActivity(i)
        }


        page_2_send_phn_otp.setOnClickListener {

            //progress.setMessage("Sending OTP :) ")
            //progress.show()
            sendVerificationCode(phone)


        }

        page_2_phn_verify.setOnClickListener {
            // progress3.show()
            verifySignInCode()

        }



        page_2_send_email_link.setOnClickListener {


            emailVal(email, "Abc!123")

        }

    }
    //send email
    private fun emailVal(eml: String, pass: String) {
        //  progress4.show()
        auth2.createUserWithEmailAndPassword(eml, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {


                    Log.d("TAG", "createUserWithEmail:success")
                    val user = auth2.currentUser

                    user?.sendEmailVerification()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "Email sent.")
                            Toast.makeText(this, getString(R.string.emailsent), Toast.LENGTH_LONG)
                                .show()
                            //progress4.dismiss()
                            page_2_email_check.text=getString(R.string.emailsent)
                            page_2_email_verify.visibility = View.VISIBLE
                            page_2_send_email_link.visibility = View.INVISIBLE
                        } else {
                            Log.d("TAG", "Email NOT sent.")
                            Toast.makeText(this, getString(R.string.emailnot), Toast.LENGTH_LONG)
                                .show()
                            //  progress4.dismiss()
                            page_2_email_verify.visibility = View.INVISIBLE
                            page_2_send_email_link.visibility = View.VISIBLE
                        }

                    }

                } else {


                    Log.w("TAG", "createUserWithEmail:failure", task.exception)
                    check = 1
                    Toast.makeText(
                        baseContext, getString(R.string.alreadyexist),
                        Toast.LENGTH_SHORT
                    ).show()
                    // progress4.dismiss()

                }


            }
        //email verify
        page_2_email_verify.setOnClickListener {
            //  progress5.show()
            auth2.signInWithEmailAndPassword(Email,"Abc!123")
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {


                        val user = auth2.currentUser
                        if (user != null && user.isEmailVerified) {
                            Log.d("TAG", "Email Verification :success")
                            Toast.makeText(this, getString(R.string.emailverified), Toast.LENGTH_LONG).show()
                            page_2_email_check.text = getString(R.string.emailverified)
                            page_2_email_verify.text = getString(R.string.verified)
                            page_2_email_verify.background=getDrawable(R.drawable.button_shape2)
                            E = 1
                            //  progress5.dismiss()
                            page_2_send_email_link.visibility = View.INVISIBLE


                        } else if(user!=null) {
                            Toast.makeText(this, getString(R.string.emailnotverified), Toast.LENGTH_LONG)
                                .show()
                            Log.d("TAG", "Verify:failed")
                            page_2_send_email_link.visibility = View.VISIBLE
                            page_2_email_verify.visibility = View.INVISIBLE
                            //   progress5.dismiss()
                        }


                    } else {

                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, getString(R.string.authfailed),
                            Toast.LENGTH_SHORT
                        ).show()
                        // progress5.dismiss()
                        page_2_send_email_link.visibility = View.VISIBLE

                    }


                }
        }
    }



    //check otp
    private fun verifySignInCode() {
        val code: String = page_2_otp_phn.text.toString()
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
                    // progress3.dismiss()
                    page_2_phn.text = getString(R.string.phonesuccess)
                    page_2_phn_verify.text = getString(R.string.verified)
                    page_2_enter_otp_phn.visibility = View.INVISIBLE
                    page_2_otp_phn.visibility = View.INVISIBLE
                    page_2_send_phn_otp.visibility = View.INVISIBLE
                    page_2_phn_verify.background=getDrawable(R.drawable.button_shape2)
                    P = 1

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.incorrectcode), Toast.LENGTH_LONG
                        ).show()
                        page_2_send_phn_otp.visibility = View.VISIBLE
                        page_2_phn_verify.visibility = View.INVISIBLE
                        page_2_send_phn_otp.text = getString(R.string.global__resend_otp)
                        // progress3.dismiss()
                    }
                }
            }
    }
    //send phn otp
    private fun sendVerificationCode(phn: String) {

        val phone: String = "+91$phn"
        if (phone.isEmpty()) {
            page_2_get_phn.requestFocus()
            page_2_get_phn.error = getString(R.string.cantbeempty)
            return
        }
        if (phone.length < 10) {
            page_2_get_phn.requestFocus()
            page_2_get_phn.error = getString(R.string.completephonenumber)
            return
        }
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)       // Phone number to verify
            .setTimeout(60, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            phone,
//            60,
//            TimeUnit.SECONDS,
//            this,

    //            mCallbacks
//        )

    }

    private var mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
             /*   page_2_phn.text = "Phone Verifciation Success"
                page_2_phn_verify.text = "Verified"
                page_2_enter_otp_phn.visibility = View.INVISIBLE
                page_2_otp_phn.visibility = View.INVISIBLE
                page_2_send_phn_otp.visibility = View.INVISIBLE*/
                //progress3.dismiss()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                page_2_phn.text = getString(R.string.failed)
                Log.d("error", e.toString())
                page_2_phn_verify.visibility = View.INVISIBLE
                page_2_send_phn_otp.visibility = View.VISIBLE
                // progress.dismiss()
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                page_2_phn.text = getString(R.string.otpsent)
                page_2_phn_verify.visibility = View.VISIBLE
                page_2_send_phn_otp.visibility = View.INVISIBLE
                codeSent = s
                //progress.dismiss()


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
