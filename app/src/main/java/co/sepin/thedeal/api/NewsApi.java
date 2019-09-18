package co.sepin.thedeal.api;

import co.sepin.thedeal.model.NewsResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface NewsApi {

    String BASE_URL = "https://newsapi.org/v2/";


    @GET("top-headlines")
    Observable<NewsResult> getNews(@Query("country") String country,
                                   @Query("apiKey") String newsKey);

    @GET("top-headlines")
    Observable<NewsResult> getNewsCategory(@Query("country") String country,
                                           @Query("category") String category,
                                           @Query("apiKey") String newsKey);

    @GET("top-headlines")
    Observable<NewsResult> getNewsSearch(@Query("q") String searchText,
                                         @Query("apiKey") String newsKey);
}
