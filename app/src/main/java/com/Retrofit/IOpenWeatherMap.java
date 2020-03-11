package com.Retrofit;

import com.example.helmet40.Model.WeatherResult;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {
    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                  @Query("lon") String lon,
                                  @Query("appid") String appid,
                                  @Query("units") String unit);

}
