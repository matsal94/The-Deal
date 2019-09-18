package co.sepin.thedeal.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils3 {

    private static Retrofit retrofitInstance;


    private RetrofitUtils3() {
    }


    public static Retrofit getInstance() {

        if (retrofitInstance == null) {

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(NewsApi.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        return retrofitInstance;
    }
}
