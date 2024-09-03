package com.example.facedetectionapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView textView;
    ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 124;
    InputImage firebaseVision;
    FaceDetector faceDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        button = findViewById(R.id.cameraBtn);
        textView=findViewById(R.id.text1);
        imageView = findViewById(R.id.imageView);
        FirebaseApp.initializeApp(this);  //links this to firebase, do not have to make a project on firebase since part of  ml-kit
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFile();
            }
        });
        Toast.makeText(this, "App is started!", Toast.LENGTH_SHORT).show();

    }

    private void OpenFile() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(getIntent().resolveActivity(getPackageManager())!=null){
            startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
        }else{
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Bitmap bitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bitmap);
                if (bitmap != null) {
                    FaceDetectionProcess(bitmap);
                    Toast.makeText(this, "Success!!!", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle the case where bitmap is null
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where bundle is null
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where data is null (camera intent didn't return any data)
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void FaceDetectionProcess(Bitmap bitmap) {
        textView.setText("Processing...");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        InputImage image = InputImage.fromBitmap(bitmap,0);
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .enableTracking()
                        .build();
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
        Task<List<Face>> result =detector.process(image);
        result.addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...
                                        if (faces != null) {
                                            if (faces.size() != 0) {
                                                builder.append(faces.size() + "Faces Detected \n\n");
                                                for (Face face : faces) {
                                                    int id = face.getTrackingId();
                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                                                    builder.append("1. Face Tracking id: " + id + "\n");
                                                    builder.append("2. Head Rotation to Right :" + String.format("%.2f", rotY) + "deg. \n");
                                                    builder.append("3. Head tilted Sideways :" + String.format("%.2f", rotZ) + "deg.\n");

                                                    if (face.getSmilingProbability() > 0) {
                                                        float smileProb = face.getSmilingProbability();
                                                        builder.append("4. Smiling Probablity is:" + String.format("%.2f", smileProb) + "\n");

                                                    }
                                                    if (face.getRightEyeOpenProbability() > 0) {
                                                        float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                        builder.append("Probability Right eye open:" + String.format("%.2f", rightEyeOpenProb) + "\n");

                                                    }
                                                    if (face.getLeftEyeOpenProbability() > 0) {
                                                        float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                        builder.append("Probablity Left eye open:" + String.format("%.2f", leftEyeOpenProb) + "\n");
                                                    }
                                                    builder.append("---------------\n");
                                                }
                                            } else {
                                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                            ShowDetection("Face Detection", builder, true);
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        StringBuilder builder1 = new StringBuilder();
                                        builder1.append("Sorry!! There was an error!");
                                        ShowDetection("Face Detection", builder,false);
                                    }
                                });


        }

    private void ShowDetection(String title, StringBuilder builder, boolean success) {
        if(success==true){
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());
            if(builder.length()!=0){
                textView.append(builder);
                if(title.substring(0,title.indexOf(' ')).equalsIgnoreCase("OCR")){
                    textView.append("\n Hold the text to copy it!");
                }else{
                    textView.append("Hold the text to copy it");
                }
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, builder);
                        clipboardManager.setPrimaryClip(clip);
                        return true;
                    }
                });
            }else{
                textView.append(title.substring(0,title.indexOf(' '))+"Failed to find Anything");
            }
        } else if (success == false) {
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.append(builder);
        }

    }



}