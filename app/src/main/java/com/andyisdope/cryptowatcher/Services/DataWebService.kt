package com.andyisdope.cryptowatcher.Services

import com.andyisdope.cryptowatch.Currency
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Url

/**
 * Created by Andy on 1/20/2018.
 */
interface DataWebService {

    companion object {
        val Data_Base_URL: String
            get() = "https://api.cryptowat.ch"

        const val Init_URL = "https://coinmarketcap.com/all/views/all/"

        var retrofit
            get() = Retrofit.Builder()
                    .baseUrl(Init_URL)
                    //GsonConverterFactory.create()
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            set(value) = TODO()
    }

    @GET
    fun dataItems(@Url url: String): Call<Array<Currency>>

    @GET
    fun getInitial(@Url url: String): Call<String>
}