package co.sepin.thedeal.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {

    private static Retrofit retrofitInstance;


    private RetrofitUtils() {
    }


    public static Retrofit getInstance() {

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        if (retrofitInstance == null) {

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(Api.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofitInstance;
    }
}
