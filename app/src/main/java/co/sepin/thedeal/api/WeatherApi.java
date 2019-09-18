package co.sepin.thedeal.api;

import co.sepin.thedeal.model.WeatherForecastResult;
import co.sepin.thedeal.model.WeatherResult;
import co.sepin.thedeal.model.WeatherUvi;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface WeatherApi {

    String BASE_URL = "https://api.openweathermap.org/data/2.5/";
/*
    @GET("weather")
    Call<WeatherResult> getCallWeatherByLatLng(@Query("lat") String lat,
                                               @Query("lon") String lng,
                                               @Query("appid") String appId,
                                               @Query("units") String unit);*/

    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                                 @Query("lon") String lng,
                                                 @Query("appid") String appId,
                                                 @Query("units") String unit);

    @GET("weather")
    Observable<WeatherResult> getWeatherByCityName(@Query("q") String cityName,
                                                   @Query("appid") String appId,
                                                   @Query("units") String unit);

    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByLatLng(@Query("lat") String lat,
                                                                 @Query("lon") String lng,
                                                                 @Query("appid") String appId,
                                                                 @Query("units") String unit);

    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByCityName(@Query("q") String cityName,
                                                                   @Query("appid") String appId,
                                                                   @Query("units") String unit);

    @GET("uvi")
    Observable<WeatherUvi> getUviWeatherByLatLng(@Query("appid") String appId,
                                                 @Query("lat") String lat,
                                                 @Query("lon") String lng);

    @GET("uvi")
    Observable<WeatherUvi> getUviWeatherByCityName(@Query("appid") String appId,
                                                   @Query("q") String cityName);
}