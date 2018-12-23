package com.extricks.mapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat                  mRgba;
    private Mat                  pRgba;

    private static final String  TAG              = "MainActivity";
    public static final int REQUEST_CAMREA = 1;
    public static final int REQUEST_STORAGE = 2;

    Button mCap;
    private SeekBar sBar1;
    private SeekBar sBar2;
    private TextView tv1;
    private TextView tv2;
    private int s1,s2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
                    .CAMERA};
            if (checkSelfPermission( Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                for(String permission : permissions){
                    if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String [] {permission},REQUEST_CAMREA);
                }

            }else {
                mOpenCvCameraView = findViewById(R.id.color_blob_detection_activity_surface_view);
                mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                mOpenCvCameraView.setCvCameraViewListener(this);
            }
        }


        mCap = findViewById(R.id.cap);
        sBar1 = findViewById(R.id.seekBar);
        sBar2 = findViewById(R.id.seekBar2);
        tv1 = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        s1=75; s2=200;

        sBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                s2=progress;
                tv2.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                s1=progress;
                tv1.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        pRgba = new Mat(height, width, CvType.CV_8UC4);
        //mRectdetector = new RectDetector();
    }

    public void onCameraViewStopped() {
        mRgba.release(); pRgba.release();
    }


    public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnCap(mRgba);
            }
        });
        Imgproc.cvtColor(mRgba,pRgba,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(pRgba,pRgba,new Size(5,5),0);
        Imgproc.Canny(pRgba,pRgba,s1,s2);
        return pRgba;
    }

    private void OnCap(Mat nm){
        Mat rbg = new Mat();
        Imgproc.cvtColor(nm,rbg,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(rbg,rbg,new Size(5,5),0);
        Imgproc.Canny(rbg,rbg,s1,s2);

        List<MatOfPoint> contours =new ArrayList<>();
        Mat mHierarchy = new Mat();
        Imgproc.findContours(rbg,contours,mHierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours,(new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return (int)(Imgproc.contourArea(o2)-Imgproc.contourArea(o1));
            }
        }));
        List<MatOfPoint> cs =new ArrayList<>();
        Point [] pts;
        int i=0;

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for(MatOfPoint c:contours){
            matOfPoint2f.fromList(c.toList());
            Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
            long total = approxCurve.total();
            if(total == 4 ) {
                cs.add(c);
                break;
            }

        }
        pts = approxCurve.toArray();
        //Imgproc.drawContours(nm,cs,-1,new Scalar(0,255,0),2);

        Bitmap bmp = null;
        try {
            Mat src = new MatOfPoint2f(pts[1],pts[0],pts[3],pts[2]);
            Mat dst = new MatOfPoint2f(new Point(0, 0), new Point(rbg.width() - 1, 0), new Point(rbg.width() - 1, rbg.height() - 1), new Point(0, rbg.height() - 1));

            Mat fmat = new Mat();
            Mat transform = Imgproc.getPerspectiveTransform(src, dst);
            Imgproc.warpPerspective(nm, fmat, transform, rbg.size());
            Imgproc.pyrDown(fmat,fmat);
            bmp = Bitmap.createBitmap(fmat.cols(), fmat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(fmat, bmp);
            bmp=Bitmap.createBitmap(bmp, bmp.getWidth()/10,bmp.getHeight()/10,8*bmp.getWidth()/10, 8*bmp.getHeight()/10);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Objects.requireNonNull(bmp).compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }

        Intent intent = new Intent(this, ImageShow.class);
        startActivity(intent);
    }



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CAMREA:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                    mOpenCvCameraView.setCvCameraViewListener(this);
                    copyAssets();
                }
            case REQUEST_STORAGE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    copyAssets();
                }
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File ff = new File(Environment.getExternalStorageDirectory().toString()+"/tessdata/");
                ff.mkdir();
                out = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/tessdata/"+filename);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
