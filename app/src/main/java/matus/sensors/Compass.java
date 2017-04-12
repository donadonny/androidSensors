package matus.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class Compass extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor compass;
    private TextView compassAngle;
    private float currentDegree = 0f;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //compass
        image=(ImageView)findViewById(R.id.imageViewCompass);
        compassAngle = (TextView)findViewById(R.id.angle);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //sensor
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(compass != null){
            sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
        }


    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree=Math.round(event.values[0]);
        compassAngle.setText("Rotation: "+Float.toString(degree)+" degrees");
        RotateAnimation ra=new RotateAnimation(currentDegree,-degree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(120);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree=-degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
