package altCntrl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

public abstract class altCntrlActivity extends Activity implements
		SensorEventListener {

	private double[] THRESHOLDS = {30.0, -30.0};
	private double NOISE = 5.0;
	public SensorManager sManager;
	private boolean on = false;
	private Method[] methods;
	private Object[] objects;
	private View view;
	public double pitch;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		pitch = 0.0;
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
		
		pitch = event.values[1];
	
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
	
	public void performAction(Method action, Object object) {
		
		if (action == null) return;
		
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
