package com.example.android.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;


public class ForecastFragment extends Fragment
                    implements LoaderManager.LoaderCallbacks<Cursor> {

    ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.
                buildWeatherLocationWithStartDate(locationSetting,
                        System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri, null, null, null, sortOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.
                buildWeatherLocationWithStartDate(locationSetting,
                        System.currentTimeMillis());

        Log.i("CREATE_CURSOR_ADAPTER", "Sort Order: " + sortOrder);
        Log.i("CREATE_CURSOR_ADAPTER", "Weather URI: " + weatherForLocationUri);

        Cursor cur = getActivity().getContentResolver()
                .query(weatherForLocationUri,
                        null, null, null, sortOrder);

        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                if(cursor != null){
                    String location = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent( getActivity(), DetailActivity.class);
                    final Uri uri = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithDate(locationSetting, cursor.getLong(1));

                    intent.setData(uri);

                    startActivity(intent);

                }
            }
        });

        return rootView;
    }

}
