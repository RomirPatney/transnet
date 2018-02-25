package whitewire.transnetandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

/**
 * Created by Claudio on 25-Feb-18.
 */

public class OfferRideActivity extends AppCompatActivity {

    String fromText, toText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offer_ride);

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                fromText = place.getAddress().toString().split(",")[0];
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: " + status);
            }
        });

        final PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                toText = place.getAddress().toString().split(",")[0];
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: " + status);
            }
        });

        Button submitButton = (Button) findViewById(R.id.submit);

        // Initializing variables to retrieve information from the user
        final Spinner fromMonthSpinner = (Spinner) findViewById(R.id.leavingMonth);
        final Spinner fromDaySpinner = (Spinner) findViewById(R.id.leavingDay);
        final Spinner fromHourSpinner = (Spinner) findViewById(R.id.leavingHour);
        final Spinner toMonthSpinner = (Spinner) findViewById(R.id.arrivingMonth);
        final Spinner toDaySpinner = (Spinner) findViewById(R.id.arrivingDay);
        final Spinner toHourSpinner = (Spinner) findViewById(R.id.arrivingHour);
        ArrayList<Integer> fromMonthArray = new ArrayList<>();
        ArrayList<Integer> fromDayArray = new ArrayList<>();
        ArrayList<Integer> fromHourArray = new ArrayList<>();
        ArrayList<Integer> toMonthArray = new ArrayList<>();
        ArrayList<Integer> toDayArray = new ArrayList<>();
        ArrayList<Integer> toHourArray = new ArrayList<>();

        for (int i=1; i<=12; i++){
            fromMonthArray.add(i);
            toMonthArray.add(i);
        }
        for (int i=1; i<=31; i++){
            fromDayArray.add(i);
            toDayArray.add(i);
        }
        for (int i=1; i<=24; i++){
            fromHourArray.add(i);
            toHourArray.add(i);
        }

        // Creating adapters for spinners to create the layout to be displayed
        ArrayAdapter<Integer> fromMonthAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, fromMonthArray);
        ArrayAdapter<Integer> fromDayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, fromDayArray);
        ArrayAdapter<Integer> fromHourAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, fromHourArray);
        ArrayAdapter<Integer> toMonthAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, toMonthArray);
        ArrayAdapter<Integer> toDayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, toDayArray);
        ArrayAdapter<Integer> toHourAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, toHourArray);

        // Setting dropdown view for spinners
        fromMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromHourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toHourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Setting adapters to spinners
        fromMonthSpinner.setAdapter(fromMonthAdapter);
        fromDaySpinner.setAdapter(fromDayAdapter);
        fromHourSpinner.setAdapter(fromHourAdapter);
        toMonthSpinner.setAdapter(toMonthAdapter);
        toDaySpinner.setAdapter(toDayAdapter);
        toHourSpinner.setAdapter(toHourAdapter);

        // Button to save all the data collected so far
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest(Integer.parseInt(fromMonthSpinner.getSelectedItem().toString()),
                        Integer.parseInt(fromDaySpinner.getSelectedItem().toString()),
                        Integer.parseInt(fromHourSpinner.getSelectedItem().toString()),
                        Integer.parseInt(toMonthSpinner.getSelectedItem().toString()),
                        Integer.parseInt(toDaySpinner.getSelectedItem().toString()),
                        Integer.parseInt(toHourSpinner.getSelectedItem().toString()),
                        fromText, toText);
            }
        });
    }

    public void sendRequest(final int fromMonth, final int fromDay, final int fromHour,
                            final int toMonth, final int toDay, final int toHour,
                            final String fromWhere, final String toWhere) {

        // Initializing request and defining URL
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://behappyapp.herokuapp.com/polls/add/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Given the request is successful, display a message
                        Toast.makeText(getApplicationContext(), "Thank you for submitting your entry",
                                Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Given there is an error with the request, display it and log it
                Toast.makeText(getApplicationContext(), "Error is: " + error.toString(),
                        Toast.LENGTH_LONG).show();
                //Log.e("VOLLEY", error.toString());
            }
        }) {
            // Sending data to API
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("fromMonth", String.valueOf(fromMonth));
                parameters.put("fromDay", String.valueOf(fromDay));
                parameters.put("fromHour", String.valueOf(fromHour));
                parameters.put("toMonth", String.valueOf(toMonth));
                parameters.put("toHour", String.valueOf(toHour));
                parameters.put("toDay", String.valueOf(toDay));
                parameters.put("fromWhere", fromWhere);
                parameters.put("toWhere", toWhere);
                return parameters;
            }

            // Method to finalize request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
