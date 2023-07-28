package com.example.taptime;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextToSpeech textToSpeech;
    private long timeOfLastShake;
    private int shakeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > 5) {
                long now = System.currentTimeMillis();

                if ((now - timeOfLastShake) > 500) {
                    shakeCount = 0;
                }

                if ((now - timeOfLastShake) < 3000) {
                    shakeCount++;

                    if (shakeCount >= 2) {
                        sayTime();
                        shakeCount = 0;
                    }
                }

                timeOfLastShake = now;
            }
        }
    }

    private void sayTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        String strDate = sdf.format(new Date());
        textToSpeech.speak(" " + strDate, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example.
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onPause();
    }
}
