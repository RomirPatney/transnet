package whitewire.transnetandroid;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.wallet.OfferWalletObject;

import static whitewire.transnetandroid.R.id.btnCamera;

/**
 * Created by Claudio on 25-Feb-18.
 */

public class MainActivity extends AppCompatActivity{
    public void checkPreferences () {
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        // If the user is logging in for the first time, show them the starting menu
        if (isFirstRun) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_selection);
        checkPreferences();

        final Button requireRide = (Button) findViewById(R.id.requireRide);
        final Button offerRide = (Button) findViewById(R.id.offerRide);
        requireRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

        offerRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), OfferRideActivity.class));
            }
        });
    }
}
