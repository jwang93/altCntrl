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
	private Activity activity;
	private View view;
	public double pitch;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		pitch = 0.0;
		sManager.registerListener(this,
				sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public void altCntrlSetUp(Method[] methods, Activity activity, View view) {
		this.methods = methods;
		this.activity = activity;
		this.view = view;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// if sensor is unreliable, return void
		
		
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}
		pitch = event.values[1];
	
		if (Math.abs(event.values[1]) < 5.0) {
			on = true;
		}
					
		if (pitch > 30.0) {
			Log.i("Called. Pitch: ", Float.toString(event.values[1]));
			event.values[1] = 0;
			performAction(methods[0]);
		} else if (pitch < -30.0) {
			event.values[1] = 0;
			performAction(methods[1]);
		}

	}
	
	public void performAction(Method action) {
		
		//Case when action was not set to anything 
		if (action == null) return;
		
		if (on) {
			try {
				action.invoke(activity, view);
//				sManager.unregisterListener(this);
				on = false;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
