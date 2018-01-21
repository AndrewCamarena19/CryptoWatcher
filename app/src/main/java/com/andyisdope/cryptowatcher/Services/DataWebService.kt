package com.andyisdope.cryptowatcher.Services

import com.andyisdope.cryptowatch.Currency
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.http.Url

/**
 * Created by Andy on 1/20/2018.
 */
interface DataWebService {

    companion object {
        val Image_Base_URL: String
            get() = "https://www.cryptocompare.com/"
        val Data_Base_URL: String
            get() = "https://api.cryptowat.ch"

        var retrofit
            get() = Retrofit.Builder()
                    .baseUrl(Image_Base_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            set(value) = TODO()
    }

    @GET
    fun dataItems(@Url url: String): Call<Array<Currency>>
}