package co.sepin.thedeal.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitUtils2 {

    private static Retrofit retrofitInstance;


    private RetrofitUtils2() {
    }


    public static Retrofit getInstance() {

        if (retrofitInstance == null) {

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(WeatherApi.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        return retrofitInstance;
    }
}