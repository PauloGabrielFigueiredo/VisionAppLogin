package VisitPlann.com;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private String qrCode;
    private Button qrCodeFoundButton;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.activity_main_previewView);
        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
        qrCodeFoundButton.setVisibility(View.INVISIBLE);


        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    /**
     * The bindCameraPreview(…) method will be used to set up and
     * initiate the camera preview to be displayed inside the PreviewView widget.
     *
     * **/
    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        /**
         * set up the PreviewView widget to use the surface view implementation mode using the
         * setPreferredImplementationMode(…) method.
         * **/
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        /**
         * CameraSelector object using the builder using the back lens of the camera.
         * **/
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new VisionImageAnalyzer(this, new VisionListener() {
            @Override
            public void onImageFound(String VisionCla) {
                if(VideoActivityDisplay.isRunning){
                    return ;
                }
                Intent intent = new Intent(MainActivity.this, VideoActivityDisplay.class);
                intent.putExtra(VideoActivityDisplay.ObjetoVision, VisionCla);
                startActivity(intent);
            }

            @Override
            public boolean onActivityAlreadyLaunched() {
                if(VideoActivityDisplay.isRunning){
                    return true;
                }
                else{

                    return false;
                }
            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
    /**
     * Image analysis capabilities available in CameraX along with the
     * ZXing library to scan image frames from the camera to locate QR codes.
    **/


}