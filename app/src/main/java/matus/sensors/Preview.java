package matus.sensors;

import android.content.Context;
import android.graphics.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Created by Matus on 5.4.2017.
 */
public class Preview extends ViewGroup implements SurfaceHolder.Callback {
    SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera camera;

//    public ShowCamera(Context context, Camera camera){
//        super(context);
//        mHolder = getHolder();
//        mHolder.
//    }

    public Preview(Context context) {
        super(context);
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}