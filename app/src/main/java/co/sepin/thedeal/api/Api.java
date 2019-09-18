package co.sepin.thedeal.api;

import java.util.List;

import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface Api {

    String BASE_URL = "https://apigastestalt.azurewebsites.net/api/";

/*
    @GET ("TestPassword")
    Call<List<Password>> getPassword();

    @POST ("TestPassword")
    Call<Password> setPassword(@Body Password password);*/



    @GET ("register")
    Call<ResponseBody> getPhone(@Query("phone_number") String phone_number);
  //Call<typ_zwracany>

    @GET ("verify")
    Call<User> getVerify(@Query("phone_number") String phone_number, @Query("otp") String otp);



    @POST ("/deal/create")
    Call<Deal> setDeal(@Body Deal deal);

    @GET ("deal/get_list")
    Call<List<Deal>> getDeal(@Query("user_id") User userId);



    @GET ("deal/get_deal_type")
    Call<List<DealType>> getDealType();



    @POST ("/event/create")
    Call<Event> setEvent(@Body Event event);

    @GET ("event/get_list")
    Call<List<Event>> getEvent(@Query("user_id") User userId);
}
