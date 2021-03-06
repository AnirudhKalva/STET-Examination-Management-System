package com.example.stet



import retrofit2.Call
import retrofit2.http.*


interface RetrofitInterface {

    @POST("/login")         //login
    fun executeLogin(@Body map: HashMap<String?, String?>?): Call<Void?>?
    @POST("/logout")                //check session
    fun executeSession(@Header ("cookieName") cookieName: String): Call<Void?>?
    @POST("/session")                   //logout
    fun executeLogout(@Header ("cookieName") cookieName: String): Call<Void?>?

    @POST("/signup")            //signup
    fun executeSignup(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/details")           //personal details
    fun executeDetail(@Header ("cookieName") cookieName: String,@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/education")             //academics details
    fun executeEducation(@Header ("cookieName") cookieName: String,@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/phone")             //signup details
    fun getDetails(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<Important?>?

    @POST("/check")         //check for email,phone,aadhar.
    fun check(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<Void?>?

    @POST("/getpersonal")       //get personal
    fun getPersonal(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<Personal?>?

    @POST("/getpreferences")
    fun getPreferences(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<PreferenceClass?>?

    @POST("/getEducation")      //get academics details
    fun getEducation(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<Education?>?

    @POST("/updatedetails")
    fun updateDetail(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/timing")                //admit card details to store
    fun timings(@Header ("cookieName") cookieName: String,@Body map: HashMap<String?, String?>?): Call<Void?>?

    @GET("/timeline/{year}")            //timeline
    fun timeline(@Header ("cookieName") cookieName: String,@Path ("year") year:String):Call<Schedule>

    @POST("/resetpassword")     //reset password
    fun resetpassword(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/payment")               //payment
    fun payment(@Header ("cookieName") cookieName: String,@Body map: HashMap<String, String>):Call<Void>

    @GET("/paymentdetails/{phone}")                 //get payment details
    fun getpayment(@Header ("cookieName") cookieName: String,@Path ("phone") phone:String):Call<Payment>

    @GET("/getDocuments/{phone}")                 //get payment details
    fun getDocuemnts(@Header ("cookieName") cookieName: String,@Path ("phone") phone:String):Call<Void>

    @GET("/submitted/{Phone}")              //check for registration
    fun submittedphone(@Header ("cookieName") cookieName: String,@Path("Phone") Phone: String):Call<Void>

    @GET("/users/{username}")                       //get phone number
    fun getPhn(@Path("username") username: String?): Call<Body1?>?

    @POST("/otp/123456789")                         //get otp
    fun getotp(@Body map: HashMap<String, String?>): Call<Void?>?

    @GET("/otp/123456789/{number}")                     //check otp
    fun matchotp(@Path("number") number: String?): Call<Void?>?

    @POST("/token")
    fun sendFirebaseToken(@Body map: HashMap<String, String>): Call<Void>
}