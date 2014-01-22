package altCntrl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public abstract class altCntrlActivity extends Activity implements
		SensorEventListener {

	public SensorManager sManager;
	private boolean on = false;
	private Method[] methods;
	private Object[] objects;
	private View view;
	public double pitch = 0.0, roll = 0.0;
	private static boolean altCntrl = false;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;
	private long lastCheckedTime;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
		Log.i("altCntrl value: ", "" + altCntrl);
		lastCheckedTime = System.currentTimeMillis();

	}

	public void altCntrlSetUp(Method[] methods, Object[] objects, View view) {
		this.methods = methods;
		this.objects = objects;
		this.view = view;
	}

	@SuppressWarnings("deprecation")
	public void onResume() {
		super.onResume();
		sManager.registerListener(this,
				sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void onPause() {
		super.onPause();
		sManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}
		
		if (shakeDetected(event)) {
			altCntrl = !altCntrl;
			lastCheckedTime = System.currentTimeMillis();
			StatusDialog dialog = altCntrl ? new StatusDialog(true) : new StatusDialog(false);
			dialog.show(getFragmentManager(), "Status Dialog");
		}
		
		if (!altCntrl) return;
		recalibrate(event);
		checkRotation(event);

	}

	private void checkRotation(SensorEvent event) {
		roll = event.values[0];
		pitch = event.values[1];
		
		if (pitch > Constants.ROTATION_THRESHOLD[0]) {
			event.values[1] = 0;
			performAction(methods[0], objects[0]);
		} else if (pitch < Constants.ROTATION_THRESHOLD[1]) {
			event.values[1] = 0;
			performAction(methods[1], objects[1]);
		}
	}
	
	
	private void recalibrate(SensorEvent event) {
		if (Math.abs(event.values[1]) < Constants.NOISE) {
			on = true;
		}
	}
	
	
	private boolean shakeDetected(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter

		Log.i("Accel Value: ", ""+mAccel);
		return (Math.abs(mAccel) > Constants.ACCELERATION_THRESHOLD && timeElapsed() > Constants.TIME_DELAY);
	}
	
	// return the timeElapsed in seconds from current time to last checked time
	private int timeElapsed() {
		return (int) (long) (System.currentTimeMillis() - lastCheckedTime) / (1000);
	}

	public void performAction(Method action, Object object) {

		if (action == null)
			return;

		if (on) {
			try {
				action.invoke(object, view);
				on = false;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
