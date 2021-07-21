package com.example.stet

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.personal_info_form__activity.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class PersonalInfoFormActivity : AppCompatActivity() {
    //for storing personal details
    private lateinit var gender: String
    private lateinit var husbandorfather: String
    var ch: Int = 0
    var ses=0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personal_info_form__activity)
        loadLocate()
        personal_info_form_activity__pb.progress = 100
        personal_info_form_activity__tv__phone_noedit.text = intent.getStringExtra("phone")
        //to get user_cookie

        val sharedPreferencesx = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        //retrofit builder
        val retrofitx: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        //retrofit instance
        var retrofitInterfacex: RetrofitInterface = retrofitx.create(RetrofitInterface::class.java)
        val cookiex:String?=sharedPreferencesx.getString("user_cookie","")
        //check session callback
        val callx: Call<Void?>? = cookiex?.let { retrofitInterfacex.executeLogout(it) }

        callx!!.enqueue(object : Callback<Void?> {
            override fun onResponse(
                call: Call<Void?>?,
                response: Response<Void?>
            ) {
                if (response.code() == 201) {
                    //session expired
                    val myEditx = sharedPreferencesx.edit()
                    myEditx.putBoolean("login", false).apply()
                    myEditx.putString("phone", "").apply()
                    myEditx.putString("user_cookie", "").apply()
                    Toast.makeText(
                        this@PersonalInfoFormActivity, getString(R.string.logkro),
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this@PersonalInfoFormActivity, MainActivity::class.java)
                    startActivity(i)
                } else if (response.code() == 200) {
                    //session valid
                    ses=1
                } else {
                    Toast.makeText(
                        this@PersonalInfoFormActivity, getString(R.string.toastslowinternet),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

            override fun onFailure(
                call: Call<Void?>?,
                t: Throwable
            ) {
                Toast.makeText(
                    this@PersonalInfoFormActivity, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()

            }

        })
        page_4_back.setOnClickListener {
            val i = Intent(this, Register::class.java)
            i.putExtra("phone", personal_info_form_activity__tv__phone_noedit.text.toString())
            startActivity(i)
        }
        //for date picker dob
        personal_info_form_activity__ib__calander.setOnClickListener {
            val datePickerDialog: DatePickerDialog


            val mYear = 1999

            val mMonth = 5

            val mDay = 8
            datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    personal_info_form_activity__et__dob.setText(
                        dayOfMonth.toString()+"-"+ (monthOfYear + 1) + "-" + year
                    )

                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()

        }
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val map: HashMap<String?, String?> = HashMap()

        map["phone"] = personal_info_form_activity__tv__phone_noedit.text.toString()
        val sharedPreferences = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val cookie:String?=sharedPreferences.getString("user_cookie","")
        // fetching signup details  from db
        val call1: Call<Important?>? = cookie?.let { retrofitInterface.getDetails(it,map) }
        call1!!.enqueue(object : Callback<Important?> {
            override fun onResponse(
                call: Call<Important?>?,
                response: Response<Important?>
            ) {
                if (response.code() == 200) {
                    Log.d("Success", "Data Stored")
                    val result = response.body()

                    if (result != null) {
                        personal_info_form_activity__tv__email_noedit.text = result.email
                    }
                }
            }

            override fun onFailure(
                call1: Call<Important?>?,
                t: Throwable
            ) {
                Log.d("Failure", t.message)
                Toast.makeText(
                    this@PersonalInfoFormActivity, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        val map2: HashMap<String?, String?> = HashMap()
        map2["Phone"] = personal_info_form_activity__tv__phone_noedit.text.toString()
        val progress2 = ProgressDialog(this)
        progress2.setMessage(getString(R.string.loadingpers))
        progress2.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress2.isIndeterminate = true
        progress2.show()
        //for fetching personal info from db
        val call2: Call<Personal?>? = cookie?.let { retrofitInterface.getPersonal(it,map2) }
        call2!!.enqueue(object : Callback<Personal?> {
            override fun onResponse(
                call: Call<Personal?>?,
                response: Response<Personal?>
            ) {
                if (response.code() == 200) {

                    val result = response.body()

                    if (result != null) {
                        personal_info_form_activity__et__cand_fname.setText(result.Fname)
                        personal_info_form_activity__et__cand_mname.setText(result.Mname)
                        personal_info_form_activity__et__cand_lname.setText(result.Lname)
                        personal_info_form_activity__et__fath_fname.setText(result.FHFname)
                        personal_info_form_activity__et__fath_mname.setText(result.FHMname)
                        personal_info_form_activity__et__fath_lname.setText(result.FHLname)
                        if (result.Gender == "Male") {
                            personal_info_form_activity__rd__male.isChecked = true
                        } else if (result.Gender == "Female") {
                            personal_info_form_activity__rd__female.isChecked = true
                        } else if (result.Gender == "Others") {
                            personal_info_form_activity__rb__other.isChecked = true
                        }
                        personal_info_form_activity__et__dob.setText(result.DOB)
                        personal_info_form_activity__sp__community.setSelection(getSpinCommunity(result.Community))
                        personal_info_form__et_aadhar.setText(result.Aadhar)
                        personal_info_form_activity__et__hno.setText(result.Hno)
                        personal_info_form_activity__et__area.setText(result.Area)
                        personal_info_form_activity__et__district.setText(result.District)
                        personal_info_form_activity__sp__state.setSelection(getSpinState(result.State))
                        personal_info_form_activity__et__zip.setText(result.Pincode)
                        personal_info_form_activity__tv__email_noedit.text = result.Email
                        ch = 1
                        personal_info_form__btn_next.text = getString(R.string.update)
                        progress2.dismiss()

                    } else {
                        ch = 0
                        progress2.dismiss()
                    }


                }
            }

            override fun onFailure(
                call1: Call<Personal?>?,
                t: Throwable
            ) {
                Log.d("Failure", t.message)
                Toast.makeText(
                    this@PersonalInfoFormActivity, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        progress2.dismiss()
        personal_info_form__btn_next.setOnClickListener {
            val progress = ProgressDialog(this)
            progress.setMessage(getString(R.string.storingpers))
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progress.isIndeterminate = true
            progress.show()
            if (validName(personal_info_form_activity__et__cand_fname) == 0
                && validName(personal_info_form_activity__et__cand_lname) == 0
                && validName(personal_info_form_activity__et__fath_fname) == 0
                && validName(personal_info_form_activity__et__fath_lname) == 0
                && validNumber(personal_info_form_activity__et__zip, 6) == 0
                && validDOB(personal_info_form_activity__et__dob) == 0
                && validSpinner(personal_info_form_activity__sp__community) == 0
                && validSpinner(personal_info_form_activity__sp__state) == 0
                && validAddress(personal_info_form_activity__et__hno) == 0
                && validAddress(personal_info_form_activity__et__area) == 0
                && validAddress(personal_info_form_activity__et__district) == 0
                && validGender(personal_info_form_activity__rd__male, personal_info_form_activity__rd__female, personal_info_form_activity__rb__other) == 0
                && personal_info_form_activity__cb.isChecked
            ) {

                val Personal: HashMap<String, String> = HashMap()
                Personal["Fname"] = personal_info_form_activity__et__cand_fname.text.toString()
                Personal["Mname"] = personal_info_form_activity__et__cand_mname.text.toString()
                Personal["Lname"] = personal_info_form_activity__et__cand_lname.text.toString()
                Personal["Gender"] = gender
                Personal["FHFname"] = personal_info_form_activity__et__fath_fname.text.toString()
                Personal["FHMname"] = personal_info_form_activity__et__fath_mname.text.toString()
                Personal["FHLname"] = personal_info_form_activity__et__fath_lname.text.toString()
                Personal["DOB"] = personal_info_form_activity__et__dob.text.toString()
                Personal["Community"] = personal_info_form_activity__sp__community.selectedItem.toString()
                Personal["Aadhar"] = personal_info_form__et_aadhar.text.toString()
                Personal["Hno"] = personal_info_form_activity__et__hno.text.toString()
                Personal["Area"] = personal_info_form_activity__et__area.text.toString()
                Personal["District"] = personal_info_form_activity__et__district.text.toString()
                Personal["State"] = personal_info_form_activity__sp__state.selectedItem.toString()
                Personal["Pincode"] = personal_info_form_activity__et__zip.text.toString()
                Personal["Phone"] = personal_info_form_activity__tv__phone_noedit.text.toString()
                Personal["Email"] = personal_info_form_activity__tv__email_noedit.text.toString()
                val sharedPreferences = getSharedPreferences(
                    "Settings",
                    Context.MODE_PRIVATE
                )
                val cookie:String?=sharedPreferences.getString("user_cookie","")
                val call: Call<Void?>? =
                    cookie?.let { it1 -> retrofitInterface.executeDetail(it1,Personal) }
                //for storing personal details in db
                call!!.enqueue(object : Callback<Void?> {
                    override fun onResponse(
                        call: Call<Void?>?,
                        response: Response<Void?>
                    ) {
                        if (response.code() == 200) {
                            Log.d("Success", "Data Stored")
                            Toast.makeText(
                                this@PersonalInfoFormActivity,
                                getString(R.string.datastored), Toast.LENGTH_LONG
                            ).show()

                            val it = Intent(this@PersonalInfoFormActivity, Register::class.java)
                            progress.dismiss()
                            it.putExtra("phone", personal_info_form_activity__tv__phone_noedit.text.toString())
                            startActivity(it)
                        } else {
                            progress.dismiss()
                        }
                    }

                    override fun onFailure(
                        call: Call<Void?>?,
                        t: Throwable
                    ) {
                        Log.d("Failure", t.message)
                        Toast.makeText(
                            this@PersonalInfoFormActivity,  getString(R.string.poorinternet),
                            Toast.LENGTH_LONG
                        ).show()
                        progress.dismiss()
                    }
                })


            } else {
                if (personal_info_form_activity__cb.isChecked) {
                    Toast.makeText(this,  getString(R.string.checkerror), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this,  getString(R.string.accepttc), Toast.LENGTH_SHORT).show()
                }
                progress.dismiss()
            }


        }
    }
    //valid dob or not
    private fun validDOB(editText: EditText): Int {
        var x = 0
        editText.text.toString().validator()
            .nonEmpty()
            .addErrorCallback {
                editText.error = getString(R.string.enterdob)
                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
    }
    //valid spinner
    private fun validSpinner(Spinner1: Spinner): Int {
        var x = 0
        if (Spinner1.selectedItem.toString().trim() == "Select") {
            x = 1
            Toast.makeText(this,  getString(R.string.selectone), Toast.LENGTH_SHORT).show()
        }

        return x
    }
    //valid address
    private fun validAddress(editText: EditText): Int {
        var x = 0
        editText.text.toString().validator()
            .nonEmpty()
            .addErrorCallback {
                editText.error = getString(R.string.validaddress)
                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
    }
    //valid gender
    private fun validGender(
        radioButton1: RadioButton,
        radioButton2: RadioButton,
        radioButton3: RadioButton
    ): Int {
        var x: Int
        if (radioButton1.isChecked || radioButton2.isChecked || radioButton3.isChecked) {
            x = 0
            gender = if (radioButton1.isChecked) {
                "Male"
            } else if (radioButton2.isChecked) {
                "Female"
            } else {
                "Others"
            }
        } else {
            Toast.makeText(this, getString(R.string.selectgender), Toast.LENGTH_LONG).show()
            x = 1
        }
        return x
    }

    private fun validHusbandorFather(radioButton1: RadioButton, radioButton2: RadioButton): Int {
        var x: Int
        if (radioButton1.isChecked || radioButton2.isChecked) {
            x = 0
            husbandorfather = if (radioButton1.isChecked) {
                "Father"
            } else {
                "Husband"
            }
        } else {
            Toast.makeText(this,  getString(R.string.selectone), Toast.LENGTH_LONG).show()
            x = 1
        }
        return x
    }

    private fun validName(editText: EditText): Int {
        var x = 0
        editText.text.toString().validator()
            .nonEmpty()
            .noNumbers()
            .addErrorCallback {
                editText.error =
                    getString(R.string.validname)

                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
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

    private fun validEmail(editText: EditText): Int {
        var x = 0
        editText.text.toString().validator()
            .validEmail()
            .nonEmpty()
            .addErrorCallback {
                editText.error = getString(R.string.validemail)
                x = 1
            }
            .addSuccessCallback {
                x = 0

            }
            .check()
        return x
    }

    private fun validMName(editText: EditText): Int {
        var x = 0
        x = if (validName(editText) == 0 || editText.text.toString() == "") {
            0
        } else {
            1
        }
        return x
    }

    private fun getSpinState(str: String?): Int {

        val list = resources.getStringArray(R.array.states)
        val i = 1
        for (i in 1..28) {
            if (list[i] == str) {
                return i
            }
        }
        return 0
    }

    private fun getSpinCommunity(str: String?): Int {

        val list = resources.getStringArray(R.array.community)
        val i = 1
        for (i in 1..4) {
            if (list[i] == str) {
                return i
            }
        }
        return 0
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
