// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.kapil.textrecognizermlkit.textrecognition;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.kapil.textrecognizermlkit.FrameMetadata;
import com.example.kapil.textrecognizermlkit.GraphicOverlay;
import com.example.kapil.textrecognizermlkit.LivePreviewActivity;
import com.example.kapil.textrecognizermlkit.VisionProcessorBase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/** Processor for the text recognition demo. */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

  private static final String TAG = "TextRecProc";
  TextToSpeech textToSpeech;
  Context context;
  private final FirebaseVisionTextDetector detector;

  public TextRecognitionProcessor(Context context) {
    this.context = context;
    textToSpeech  = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int i) {
        Log.e(TAG, "onInit: " + "calllleddddd" );
        if(i != TextToSpeech.ERROR) {
          textToSpeech.setLanguage(Locale.UK);
        }
        else Log.e(TAG, "onInit: " + TextToSpeech.ERROR );
      }
    });
    detector = FirebaseVision.getInstance().getVisionTextDetector();
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
    }
  }

  @Override
  protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull FirebaseVisionText results,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();

    List<FirebaseVisionText.Block> blocks = results.getBlocks();

    for (int i = 0; i < blocks.size(); i++) {
      List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
      textToSpeech.speak(blocks.get(i).getText().toString(), TextToSpeech.QUEUE_FLUSH,null);

      for (int j = 0; j < lines.size(); j++) {
        List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
        for (int k = 0; k < elements.size(); k++) {
          //Log.e(TAG, "onSuccess: " + elements.get(k).getText().toString() );
          GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
          graphicOverlay.add(textGraphic);
        }
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Text detection failed." + e);
  }
}
