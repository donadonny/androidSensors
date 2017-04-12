package matus.sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewDebug;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private android.hardware.Sensor sensor;
    private TextView LightText;
    private IconRoundCornerProgressBar LightMeter;
    private TextView RangeInformation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LightText = (TextView)findViewById(R.id.Light);
        LightMeter = (IconRoundCornerProgressBar)findViewById(R.id.LightBar);
        RangeInformation = (TextView)findViewById(R.id.textRangeInformation);
        RangeInformation.setText("Range 0-150 lux");

        //TODO Light Meter use
        LightMeter.setProgressColor(Color.parseColor("#0D47A1"));
        LightMeter.setProgressBackgroundColor(Color.parseColor("#64B5F6"));
        LightMeter.setIconBackgroundColor(Color.parseColor("#64B5F6"));
        //Sensor
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
        if(sensor != null){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        final float maxSensorRange = sensor.getMaximumRange();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LightMeter.getMax() != 150){
                    LightMeter.setMax(150);
                    RangeInformation.setText("Range 0-150 lux");
                }
                else{
                    LightMeter.setMax(maxSensorRange);
                    RangeInformation.setText("Range 0-30 000 lux (max range)");
                }

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        LightMeter.setMax(150);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float light = Math.round(event.values[0]);

        LightText.setText(Float.toString(light)+" lux  ");
        LightMeter.setProgress((int)light);

    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }
}
