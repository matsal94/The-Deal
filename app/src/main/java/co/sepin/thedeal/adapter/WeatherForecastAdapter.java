package co.sepin.thedeal.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.pwittchen.weathericonview.WeatherIconView;

import co.sepin.thedeal.R;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.model.WeatherForecastResult;

import static android.content.Context.MODE_PRIVATE;


public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.ViewHolder> {

    private Context context;
    private WeatherForecastResult weatherForecastResult;
    private String unit;
    private int sunriseHour, sunsetHour;


    public WeatherForecastAdapter() {
        super();
    }


    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult, String unit) {

        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
        this.unit = unit;
    }


    @NonNull
    @Override
    public WeatherForecastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_list, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull WeatherForecastAdapter.ViewHolder holder, int position) {

        YoYo.with(Techniques.Pulse).duration(600).playOn(holder.cardView); //animacja cardView podczas przewijania

        holder.iconView.setIconResource(context.getString(setWeatherForecastIcon(position)));
        holder.dateTimeTV.setText(new StringBuilder(ModeClass.convertUnixToDate2(context, weatherForecastResult.list.get(position).getDt())));

        try {

            int descriptionId = context.getResources().getIdentifier(weatherForecastResult.list.get(position).weather.get(0).getDescription()
                    .replaceAll(" ", "_")
                    .replaceAll("-", "_")
                    .replaceAll("/", "_")
                    .replaceAll("%", "")
                    .replaceAll(":", ""), "string", context.getPackageName());
            holder.descriptionTV.setText(context.getString(descriptionId));
        } catch (Exception e) {
            holder.descriptionTV.setText(new StringBuilder(weatherForecastResult.list.get(position).weather.get(0).getDescription()));
        }

        switch (unit) {

            case "Metric":
                holder.temperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherForecastResult.list.get(position).getMain().getTemp()))).append("°C").toString());
                break;

            case "Imperial":
                holder.temperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherForecastResult.list.get(position).getMain().getTemp()))).append("°F").toString());
                break;

            default:
                holder.temperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherForecastResult.list.get(position).getMain().getTemp()))).append("K").toString());
        }
    }


    private int setWeatherForecastIcon(int position) {

        String iconName;
        int selectHour = ModeClass.convertUnixToHour(weatherForecastResult.list.get(position).getDt());

        if ((sunriseHour <= selectHour) && (selectHour <= sunsetHour))
            iconName = new StringBuilder("wi_owm_day_").append(String.valueOf(weatherForecastResult.list.get(position).getWeather().get(0).getId())).toString();
        else
            iconName = new StringBuilder("wi_owm_night_").append(String.valueOf(weatherForecastResult.list.get(position).getWeather().get(0).getId())).toString();

        int iconId = context.getResources().getIdentifier(iconName, "string", context.getPackageName());
        return iconId;
    }


    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        WeatherIconView iconView;
        TextView dateTimeTV, descriptionTV, temperatureTV;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.item_weatherCV);
            iconView = (WeatherIconView) itemView.findViewById(R.id.item_weatherWIV);
            dateTimeTV = (TextView) itemView.findViewById(R.id.item_weather_dateTimeTV);
            descriptionTV = (TextView) itemView.findViewById(R.id.item_weather_descriptionTV);
            temperatureTV = (TextView) itemView.findViewById(R.id.item_weather_temperatureTV);

            SharedPreferences prefsWeather = context.getSharedPreferences("Weather", MODE_PRIVATE);
            sunriseHour = prefsWeather.getInt("sunriseHour", 6);
            sunsetHour = prefsWeather.getInt("sunsetHour", 18);
        }
    }
}