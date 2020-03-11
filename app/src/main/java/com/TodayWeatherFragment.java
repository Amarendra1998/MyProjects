package com;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Common.Common;
import com.Retrofit.IOpenWeatherMap;
import com.Retrofit.RetrofitClient;
import com.example.helmet40.Model.WeatherResult;
import com.example.helmet40.R;
import com.squareup.picasso.Picasso;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class TodayWeatherFragment extends Fragment {

    ImageView img_weather;
    TextView txt_city_name,txt_humidity,txt_sunrise,_txt_sunset,txt_pressure,txt_temprature,txt_description,txt_date_time,txt_wind,txt_geo_coords;
    LinearLayout weather_panel;
    ProgressBar loading;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    static TodayWeatherFragment instance;

  public static TodayWeatherFragment getInstance(){
      if (instance == null)
          instance = new TodayWeatherFragment();
          return instance;

  }

    public TodayWeatherFragment() {
       compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemview = inflater.inflate(R.layout.fragment_today_weather, container, false);
        img_weather = (ImageView)itemview.findViewById(R.id.img_weather);
        txt_city_name = (TextView) itemview.findViewById(R.id.text_city_name);
        txt_humidity = (TextView) itemview.findViewById(R.id.text_humidity);
        txt_sunrise = (TextView) itemview.findViewById(R.id.text_sunrise);
        _txt_sunset = (TextView) itemview.findViewById(R.id.text_sunset);
        txt_pressure = (TextView) itemview.findViewById(R.id.text_pressure);
        txt_temprature = (TextView) itemview.findViewById(R.id.text_temperature);
        txt_description = (TextView) itemview.findViewById(R.id.text_description);
        txt_date_time = (TextView) itemview.findViewById(R.id.text_date_time);
        txt_wind = (TextView) itemview.findViewById(R.id.text_wind);
        txt_geo_coords = (TextView) itemview.findViewById(R.id.text_geo_cords);

        weather_panel = (LinearLayout) itemview.findViewById(R.id.weather_panel);
        loading = (ProgressBar) itemview.findViewById(R.id.loading);

        getWeatherInformation();
        return itemview;
    }

    private void getWeatherInformation() {

      compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(Common.current_location.getLatitude()),
              String.valueOf(Common.current_location.getLongitude()),
              Common.API_KEY,
              "metric")
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(new Consumer<WeatherResult>() {
          @Override
          public void accept(WeatherResult weatherResult) throws Exception {
              Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/").append(weatherResult.getWeather().get(0).getIcon()).append(".png").toString()).into(img_weather);
              txt_city_name.setText(weatherResult.getName());
              txt_description.setText(new StringBuilder("Weather in").append(weatherResult.getName().toString()));
              txt_temprature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
              txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
              txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append("hpa").toString());
              txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%").toString());
              txt_sunrise.setText(Common.convertUnixToHour((long) weatherResult.getSys().getSunrise()));
              _txt_sunset.setText(Common.convertUnixToHour((long) weatherResult.getSys().getSunset()));
              txt_geo_coords.setText(new StringBuilder("[").append(weatherResult.getCoord().toString()).append("]").toString());

              weather_panel.setVisibility(View.VISIBLE);
              loading.setVisibility(View.GONE);


          }
      }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
              Toast.makeText(getActivity(),""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
          }
      })
      );
    }

}
