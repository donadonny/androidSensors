package matus.sensors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.support.v4.app.NavUtils;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.*;
import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Handler.Callback, SensorEventListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    //camera

    static final String TAG = "CamTest";
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 1242;
    private static final int MSG_CAMERA_OPENED = 1;
    private static final int MSG_SURFACE_READY = 2;
    private final Handler mHandler = new Handler(this);
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    CameraManager mCameraManager;
    String[] mCameraIDsList;
    CameraDevice.StateCallback mCameraStateCB;
    CameraDevice mCameraDevice;
    CameraCaptureSession mCaptureSession;
    boolean mSurfaceCreated = true;
    boolean mIsCameraConfigured = false;
    private Surface mCameraSurface = null;
    //tmp
    private TextView maxAccRange;

    //Sensors
    private SensorManager sensorManager;

    //Barometer
    private android.hardware.Sensor barometerSensor;
    private TextView barPressure;
    private IconRoundCornerProgressBar pressureBar;
    //Compass
    private ImageView compassImage;
    private android.hardware.Sensor compass;
    private float currentDegree = 0f;

    //Light
    private android.hardware.Sensor lightSensor;
    private IconRoundCornerProgressBar LightMeter;

    //accelerometer
    private android.hardware.Sensor accelerometerSensor;
    private IconRoundCornerProgressBar accelerometer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        //Compass
        compassImage = (ImageView)findViewById(R.id.compasImage);

        //Barometer
        pressureBar = (IconRoundCornerProgressBar) findViewById(R.id.pressureBar);
        pressureBar.setProgressBackgroundColor(Color.parseColor("#8BCED2"));
        pressureBar.setProgressColor(Color.parseColor("#0B687D"));
        pressureBar.setIconBackgroundColor(Color.parseColor("#8BCED2"));
        pressureBar.setMax(1100);

        barPressure = (TextView)findViewById(R.id.pressureValue);

        //Light
        LightMeter = (IconRoundCornerProgressBar)findViewById(R.id.LightBarInCamera);

        //Accelerometer
        accelerometer = (IconRoundCornerProgressBar)findViewById(R.id.accelerometer);
        accelerometer.setIconBackgroundColor(R.color.trans);
        accelerometer.setProgressBackgroundColor(R.color.trans);

        accelerometer.setProgressColor(Color.parseColor("#D44330"));
        accelerometer.setSecondaryProgressColor(Color.parseColor("#D44330"));

        LightMeter.setProgressColor(Color.parseColor("#0D47A1"));
        LightMeter.setProgressBackgroundColor(Color.parseColor("#64B5F6"));
        LightMeter.setIconBackgroundColor(Color.parseColor("#64B5F6"));
        LightMeter.setMax(150);
        //Sensor
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        if(lightSensor != null){
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(compass != null){
            sensorManager.registerListener(this, compass , SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometerSensor != null){
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (barometerSensor != null){
            sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }




        //Camera
        //SwitchCameraButton = (Button) findViewById(R.id.dummy_button);
        //camera preview


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


        this.mSurfaceView = (SurfaceView) findViewById(R.id.SurfaceViewPreview);
        this.mSurfaceHolder = this.mSurfaceView.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);


        try {
            mCameraIDsList = this.mCameraManager.getCameraIdList();
            for (String id : mCameraIDsList) {
                Log.v(TAG, "CameraID: " + id);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mCameraStateCB = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
               // Toast.makeText(getApplicationContext(), "onOpened", Toast.LENGTH_SHORT).show();


                mCameraDevice = camera;
                mHandler.sendEmptyMessage(MSG_CAMERA_OPENED);
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
              //  Toast.makeText(getApplicationContext(), "onDisconnected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(CameraDevice camera, int error) {
              // Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
            }
        };

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            float light = Math.round(event.values[0]);
            LightMeter.setProgress((int)light);
        }
        else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float degree=Math.round(event.values[0]);
            RotateAnimation ra=new RotateAnimation(currentDegree,-degree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            ra.setDuration(120);
            ra.setFillAfter(true);
            compassImage.startAnimation(ra);
            currentDegree=-degree;
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float xvalue = Math.round(event.values[0]+event.values[1]+event.values[2]);

            accelerometer.setMax(120);
            accelerometer.setProgress(xvalue);
        }
        else if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            float pressureValue = Math.round(event.values[0]);
            pressureBar.setProgress((int)pressureValue);
            //pressureBar.setProgress(997);
            barPressure.setText(Float.toString(pressureValue));
            barPressure.setTextColor(Color.WHITE);
        }





    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();


        //requesting permission
        final int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                //  Toast.makeText(getApplicationContext(), "request permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            //  Toast.makeText(getApplicationContext(), "PERMISSION_ALREADY_GRANTED", Toast.LENGTH_SHORT).show();
            try {
                mCameraManager.openCamera(mCameraIDsList[0], mCameraStateCB, new Handler());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


    }
    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mCaptureSession != null) {
                mCaptureSession.stopRepeating();
                mCaptureSession.close();
                mCaptureSession = null;
            }

            mIsCameraConfigured = false;
        } catch (final CameraAccessException e) {
            // Doesn't matter, cloising device anyway
            e.printStackTrace();
        } catch (final IllegalStateException e2) {
            // Doesn't matter, cloising device anyway
            e2.printStackTrace();
        } finally {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
                mCaptureSession = null;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CAMERA_OPENED:
            case MSG_SURFACE_READY:
                // if both surface is created and camera device is opened
                // - ready to set up preview and other things
                if (mSurfaceCreated && (mCameraDevice != null)
                        && !mIsCameraConfigured) {
                    configureCamera();
                }
                break;
        }

        return true;
    }

    private void configureCamera() {
        // prepare list of surfaces to be used in capture requests
        List<Surface> sfl = new ArrayList<Surface>();

        sfl.add(mCameraSurface); // surface for viewfinder preview

        // configure camera with all the surfaces to be ever used
        try {
            mCameraDevice.createCaptureSession(sfl,
                    new CaptureSessionListener(), null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mIsCameraConfigured = true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    try {
                        mCameraManager.openCamera(mCameraIDsList[1], mCameraStateCB, new Handler());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCameraSurface = holder.getSurface();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCameraSurface = holder.getSurface();
        mSurfaceCreated = true;
        mHandler.sendEmptyMessage(MSG_SURFACE_READY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
    }

    private class CaptureSessionListener extends
            CameraCaptureSession.StateCallback {
        @Override
        public void onConfigureFailed(final CameraCaptureSession session) {
            Log.d(TAG, "CaptureSessionConfigure failed");
        }

        @Override
        public void onConfigured(final CameraCaptureSession session) {
            Log.d(TAG, "CaptureSessionConfigure onConfigured");
            mCaptureSession = session;

            try {
                CaptureRequest.Builder previewRequestBuilder = mCameraDevice
                        .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(mCameraSurface);
                mCaptureSession.setRepeatingRequest(previewRequestBuilder.build(),
                        null, null);
            } catch (CameraAccessException e) {
                Log.d(TAG, "setting up preview failed");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        LightMeter.setVisibility(View.INVISIBLE);
        accelerometer.setVisibility(View.INVISIBLE);
        compassImage.setVisibility(View.INVISIBLE);
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        LightMeter.setVisibility(View.VISIBLE);
        accelerometer.setVisibility(View.VISIBLE);
        compassImage.setVisibility(View.VISIBLE);
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }



}
