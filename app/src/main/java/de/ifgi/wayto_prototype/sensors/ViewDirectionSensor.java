package de.ifgi.wayto_prototype.sensors;

import de.ifgi.wayto_prototype.activities.MainActivity;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.CameraPosition;

import de.ifgi.wayto_prototype.activities.MainActivity;

/**
 * Created by helo on 9/12/15.
 */
public class ViewDirectionSensor extends Activity implements SensorEventListener {

    private CameraPosition currentCameraPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
