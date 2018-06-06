package com.example.kapil.textrecognizermlkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnSnap,btnDetectText,btnLabelImage,btnRecognizeFace;
    ImageView imageView;
    TextView tvTextRecognised;
    Bitmap imageBitmap;
    LinearLayout layoutButtons;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        btnDetectText = findViewById(R.id.btnDetectText);
        btnLabelImage = findViewById(R.id.btnLabelImage);
        btnRecognizeFace = findViewById(R.id.btnRecognizeFace);
        btnSnap = findViewById(R.id.btnSnap);
        layoutButtons = findViewById(R.id.layoutButtons);
        tvTextRecognised = findViewById(R.id.tvImageData);
        btnSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btnDetectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTxt();
            }
        });
        btnLabelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelImage();
            }
        });
        btnRecognizeFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeFace();
            }
        });
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            layoutButtons.setVisibility(View.VISIBLE);
        }
    }

    private void recognizeFace(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .setTrackingEnabled(true)
                        .build();
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        processFace(faces);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
    }
    private void processFace(List<FirebaseVisionFace> firebaseVisionFaces){
        List<FirebaseVisionFace> faces = firebaseVisionFaces;
        for (FirebaseVisionFace face : faces) {
            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
            }

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
                tvTextRecognised.setText("Smile probability = " + smileProb);
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
            }

            // If face tracking was enabled:
            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                int id = face.getTrackingId();
            }
        }
    }

    private void labelImage(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector();
        Task<List<FirebaseVisionLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
                                        processLabel(labels);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
    }
    private void processLabel(List<FirebaseVisionLabel> firebaseVisionLabel){
        List<FirebaseVisionLabel> labels = firebaseVisionLabel;
        for (FirebaseVisionLabel label: labels) {
            String text = label.getLabel();
            String entityId = label.getEntityId();
            float confidence = label.getConfidence();
            tvTextRecognised.setText(text + "\n" + confidence );
        }
    }

    private void detectTxt() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        Task<FirebaseVisionText> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                processText(firebaseVisionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
    }

    private void processText(FirebaseVisionText firebaseVisionText){
        List<FirebaseVisionText.Block> blocks = firebaseVisionText.getBlocks();
        if(blocks.size() == 0){
            Toast.makeText(this, "No text detected!", Toast.LENGTH_SHORT).show();
            return;
        }
        for(FirebaseVisionText.Block  block : firebaseVisionText.getBlocks()){
            //String text = block.getText();
            for(FirebaseVisionText.Line line : block.getLines()){
                String text = line.getText();
                tvTextRecognised.setText(text);

            }
        }
    }
}
