package com.example.stet

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.homepage.*
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.android.synthetic.main.login_activity.homepage_btn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.login_activity)
        homepage_btn.setOnClickListener {
            showChangeLang()
        }
        FirebaseMessaging.getInstance().isAutoInitEnabled = false
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val sharedPreferences = getSharedPreferences(
            "Settings",
            MODE_PRIVATE
        )
        if (sharedPreferences.getBoolean("login", false)) {
            val st: String? = sharedPreferences.getString("phone", "")
            val i = Intent(this@MainActivity, ExamRegisterClickActivity::class.java)
            i.putExtra("phone", st)
            startActivity(i)
        }
        val myEdit = sharedPreferences.edit()

        page_1_login.setOnClickListener {
            var c=0
            c= validNumber(page_1_phn_et, 10)+isValidPassword(page_1_Edtpass)
            if (c == 0) {
                /**
                 * @todo remove?
                 */
                val progress = ProgressDialog(this)
                progress.setMessage(getString(R.string.verifyingcredentials))
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progress.isIndeterminate = true
                progress.show()
                val map: HashMap<String?, String?> = HashMap()

                map["phone"] = page_1_phn_et.text.toString()
                map["password"] = page_1_Edtpass.text.toString()
                //callback for login
                val call: Call<Void?>? = retrofitInterface.executeLogin(map)

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
                           /* Toast.makeText(
                                this@MainActivity, cookie,
                                Toast.LENGTH_LONG
                            ).show()*/
                            Toast.makeText(
                                this@MainActivity, getString(R.string.loginsuccessfully),
                                Toast.LENGTH_LONG
                            ).show()
                            progress.dismiss()
                            val i = Intent(this@MainActivity, ExamRegisterClickActivity::class.java)
                            if (remember.isChecked) {
                                myEdit.putBoolean("login", true).apply()
                                myEdit.putString("phone", page_1_phn_et.text.toString()).apply()
                            }
                            i.putExtra("phone", page_1_phn_et.text.toString())
                            startActivity(i)
                        } else if (response.code() == 404) {
                            Toast.makeText(
                                this@MainActivity, getString(R.string.toastwrong),
                                Toast.LENGTH_LONG
                            ).show()
                            progress.dismiss()
                        } else {
                            Toast.makeText(
                                this@MainActivity, getString(R.string.toastslowinternet),
                                Toast.LENGTH_LONG
                            ).show()
                            progress.dismiss()
                        }
                    }

                    override fun onFailure(
                        call: Call<Void?>?,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@MainActivity, getString(R.string.poorinternet),
                            Toast.LENGTH_LONG
                        ).show()
                        progress.dismiss()
                    }

                })

            } else {
                Toast.makeText(this, getString(R.string.checkerror), Toast.LENGTH_LONG).show()
            }
        }
        page_1_signup.setOnClickListener {
            val i = Intent(this, SignupActivity::class.java)
            startActivity(i)
        }
        page_1_forget.setOnClickListener {
            val i = Intent(this, Forget::class.java)
            startActivity(i)
        }
    }
    //on back to home
    override fun onBackPressed() {
       val i=Intent(this,Home1::class.java)
        startActivity(i);
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
                editText.error = "Enter $d digit Phone Number Only"
                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
    }

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
                    "Atleast one Uppercase\none LowerCase\none Special Character\none Number"
                x = 1
            }
            .addSuccessCallback {
                x = 0
            }
            .check()
        return x
    }
    //choose language
    private fun showChangeLang() {

        val listItems = arrayOf("English","हिन्दी")
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", "")
        var t=0
        when (language) {
            "en" -> {
                t=0
            }
            "hi" -> {
                t=1
            }
        }
        val mBuilder = AlertDialog.Builder(this@MainActivity)
        mBuilder.setTitle(getString(R.string.chooselang))
        mBuilder.setSingleChoiceItems(listItems, t) { dialog, which ->
            if (which == 0) {
                setLocate("en")
                recreate()
            } else if (which == 1) {
                setLocate("hi")
                recreate()
            }
            dialog.dismiss()

        }
        val mDialog = mBuilder.create()

        mDialog.show()
    }
    private fun setLocate(Lang: String) {
        val locale = Locale(Lang)
        val config = Configuration()

        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", MODE_PRIVATE).edit()
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
