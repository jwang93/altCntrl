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

	private double[] THRESHOLDS = { 30.0, -30.0 };
	private double NOISE = 5.0;
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

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		mAccelLast = mAccelCurrent;
		mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
		float delta = mAccelCurrent - mAccelLast;
		mAccel = mAccel * 0.9f + delta; // perform low-cut filter

		roll = event.values[0];
		pitch = event.values[1];

		Log.i("mAccel: ", ""+mAccel);
		if (Math.abs(mAccel) > 200.0 && timeElapsed() > 2) {
			altCntrl = !altCntrl;
			lastCheckedTime = System.currentTimeMillis();
			StatusDialog dialog = altCntrl ? new StatusDialog(true) : new StatusDialog(false);
			dialog.show(getFragmentManager(), "Status Dialog");
		}

		if (!altCntrl)
			return;

		if (Math.abs(event.values[1]) < NOISE) {
			on = true;
		}

		if (pitch > THRESHOLDS[0]) {
			event.values[1] = 0;
			performAction(methods[0], objects[0]);
		} else if (pitch < THRESHOLDS[1]) {
			event.values[1] = 0;
			performAction(methods[1], objects[1]);
		}

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
