package com.example.stet

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.admitcard_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*



class DownloadAdmitcardActivity : AppCompatActivity() {

    // download admit card

    var ses=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.admitcard_info)
        val phone=intent.getStringExtra("phone")
        val sharedPreferencesx = getSharedPreferences(
            "Settings",
            Context.MODE_PRIVATE
        )
        val retrofitx: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var retrofitInterfacex: RetrofitInterface = retrofitx.create(RetrofitInterface::class.java)
        val cookiex:String?=sharedPreferencesx.getString("user_cookie","")
        //check for session
        val callx: Call<Void?>? = cookiex?.let { retrofitInterfacex.executeLogout(it) }

        callx!!.enqueue(object : Callback<Void?> {
            override fun onResponse(
                call: Call<Void?>?,
                response: Response<Void?>
            ) {
                if (response.code() == 201) {

                    val myEditx = sharedPreferencesx.edit()
                    myEditx.putBoolean("login", false).apply()
                    myEditx.putString("phone", "").apply()
                    myEditx.putString("user_cookie", "").apply()
                    Toast.makeText(
                        this@DownloadAdmitcardActivity, getString(R.string.logkro),
                        Toast.LENGTH_LONG
                    ).show()
                    val i = Intent(this@DownloadAdmitcardActivity, MainActivity::class.java)
                    startActivity(i)
                } else if (response.code() == 200) {

                    ses=1
                } else {
                    Toast.makeText(
                        this@DownloadAdmitcardActivity, getString(R.string.toastslowinternet),
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

            override fun onFailure(
                call: Call<Void?>?,
                t: Throwable
            ) {
                Toast.makeText(
                    this@DownloadAdmitcardActivity, getString(R.string.poorinternet),
                    Toast.LENGTH_LONG
                ).show()

            }

        })

            download.setOnClickListener {
                if (phone != "0") {
                    image(phone, "admit", "fs")
                } else {
                    Toast.makeText(this, getString(R.string.logininagain), Toast.LENGTH_SHORT)
                        .show()
                }
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
    //download admit function
    fun image(phone:String,str: String,coll: String){
        val retrofit1: Retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.api_url))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val progress2 = ProgressDialog(this)
        progress2.setMessage(getString(R.string.downloadadmit))
        progress2.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress2.isIndeterminate = true
        progress2.show()
        var retrofitInterface1: UploadRetro? = retrofit1.create(UploadRetro::class.java)
        val call: Call<String?>? = retrofitInterface1?.downloadfile(phone+"_"+str+".pdf",coll)
        call!!.enqueue(object : Callback<String?> {
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Toast.makeText(this@DownloadAdmitcardActivity,t.message, Toast.LENGTH_SHORT).show()
                progress2.dismiss()
            }
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.code()==200)
                {
                    val imageURL: String? =response.body()
                    if(imageURL!=null)
                    {
                        val st: String =imageURL
                        val pureBase64Encoded:String  = st.substring(st.indexOf(",")  + 1);
                        val decodedString: ByteArray = android.util.Base64.decode(pureBase64Encoded, android.util.Base64.DEFAULT)
                        val folderLocation = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "AdmitCard"
                        )
                        folderLocation.mkdirs()
                        val filename: String =
                            folderLocation.path + File.separator + System.currentTimeMillis().toString() + "admitcard.pdf"
                        try {
                            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x512) // https://stackoverflow.com/a/51314932/9160306
                            val fileOutPutStream = FileOutputStream(filename)
                            fileOutPutStream.write(decodedString)
                            fileOutPutStream.close()
                            Toast.makeText(this@DownloadAdmitcardActivity,getString(R.string.admitsaved), Toast.LENGTH_LONG).show()
                            download.background=getDrawable(R.drawable.button_shape2)
                        } catch (e: IOException) {
                            progress2.dismiss()
                            Toast.makeText(this@DownloadAdmitcardActivity,getString(R.string.notgenerated), Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }

                    }
                }
                else
                {
                    progress2.dismiss()
                    Toast.makeText(this@DownloadAdmitcardActivity,getString(R.string.notgenerated), Toast.LENGTH_SHORT).show()

                }
                progress2.dismiss()
            }
        })
    }
}