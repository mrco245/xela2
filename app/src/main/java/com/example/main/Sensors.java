package com.example.main;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Sensors extends MainActivity implements SensorEventListener {


    //Function that takes the JSON and sends it to the Web1 Server
    public void sendToServer() {

        RequestAsync test = new RequestAsync();
        test.execute();
    }

    //Function to set all of the data
    public JSONObject setData() {


        try {
            weather.put("temperature", Temp);
            weather.put("humidity", humidity);
            weather.put("pressure", Pressure);


            Accelerometer.put("acceleration", acceleration);
            Accelerometer.put("gyroscope", gyro);
            Accelerometer.put("magnometer", magnometer);

            data.put("WEATHER", weather);
            data.put("ACCELEROMETER", Accelerometer);
            data.put("GPS", gps);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Function that collects all sensor and all GPS location and stores it in a json object.
    public JSONObject sendALL() {
        Time time = new Time();
        String currentDate = null;
        try {
            currentDate = time.currentDateTime().get("Time").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        data = setData();

        try {
            json.put("timestamp", currentDate);
            json.put("data", data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        sendToServer();
        return json;
    }

    //sets the data in the json to be just the sensor data
    public JSONObject setSensorData() {
        try {
            weather.put("temperature", Temp);
            weather.put("humidity", humidity);
            weather.put("pressure", Pressure);

            Accelerometer.put("acceleration", acceleration);
            Accelerometer.put("gyroscope", gyro);
            Accelerometer.put("magnometer", magnometer);

            data.put("WEATHER", weather);
            data.put("ACCELEROMETER", Accelerometer);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }


    //Function that sends the sensor data
    public JSONObject sendSensor() {

        //String currentDate = currentDateTime();

        SensorData = setSensorData();
        //data = setData();
        try {

            //json.put("timestamp", currentDate);
            json.put("data", SensorData);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //sendToServer();
        return json;
    }

    public JSONObject setWeather()
    {
        try {
            weather.put("temperature", Temp);
            weather.put("humidity", humidity);
            weather.put("pressure", Pressure);

            data.put("WEATHER", weather);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONObject sendWeather()
    {
        JSONObject Weather = new JSONObject();
        Weather = setWeather();
        try {

            json.put("data", Weather);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject setAccelermoter()
    {
        try {

            Accelerometer.put("acceleration", acceleration);
            Accelerometer.put("gyroscope", gyro);
            Accelerometer.put("magnometer", magnometer);

            data.put("ACCELEROMETER", Accelerometer);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONObject sendAccelerometer()
    {

        JSONObject Accelerometer = new JSONObject();
        Accelerometer = setAccelermoter();
        try {

            json.put("data", Accelerometer);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    //Starts the accelerometer and sets default values if not supported
    public JSONObject getAcceleration() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(Sensors.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            //Log.d(TAG, "onCreate: Registered accelerometer listener");
        } else {
            try {
                acceleration.put("x", "");
                acceleration.put("y", "");
                acceleration.put("z", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return acceleration;
    }

    //Starts the gyroscope listener and sets default values if not supported
    public JSONObject getGyro() {
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyro != null) {
            sensorManager.registerListener( Sensors.this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
            // Log.d(TAG, "onCreate: Registered gyro listener");
        } else {
            try {
                gyro.put("x", "");
                gyro.put("y", "");
                gyro.put("z", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return gyro;
    }

    //Starts the magnometer listener and sets default values if not supported
    public JSONObject getMagnometer() {

        mMagno = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mMagno != null) {
            sensorManager.registerListener( Sensors.this, mMagno, SensorManager.SENSOR_DELAY_NORMAL);
            //Log.d(TAG, "onCreate: Registered Magnometer listener");
        } else {
            try {
                magnometer.put("x", "");
                magnometer.put("y", "");
                magnometer.put("z", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return magnometer;
    }

    //Starts the pressure listener and sets default values if not supported
    public JSONObject getPressure() {
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mPressure != null) {
            sensorManager.registerListener(Sensors.this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
            //Log.d(TAG, "onCreate: Registered pressure listener");
        } else {
            try {
                Pressure.put("units", "kPa");
                Pressure.put("value", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Pressure;
    }

    //Starts the temperature listener and sets default values if not supported
    public JSONObject getTemp() {
        mTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (mTemp != null) {
            sensorManager.registerListener(Sensors.this, mTemp, SensorManager.SENSOR_DELAY_NORMAL);
            // Log.d(TAG, "onCreate: Registered Temp listener");
        } else {
            try {
                Temp.put("units", "C");
                Temp.put("value", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return Temp;
    }

    //Starts the humidity listener and sets default values if not supported
    public JSONObject getHumidity() {
        mHumi = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (mHumi != null) {
            sensorManager.registerListener( Sensors.this, mHumi, SensorManager.SENSOR_DELAY_NORMAL);
            //Log.d(TAG, "onCreate: Registered humi listener");
        } else {

            try {
                humidity.put("units", "%");
                humidity.put("value", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return humidity;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        if (sensor.getType() == sensor.TYPE_ACCELEROMETER) {
            //Log.d(TAG, "onSensorChanged: X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);

            //xValue.setText("xValue " + event.values[0]);
            //yValue.setText("yValue " + event.values[1]);
            //zValue.setText("zValue " + event.values[2]);

            try {
                acceleration.put("x", event.values[0]);
                acceleration.put("y", event.values[1]);
                acceleration.put("z", event.values[2]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (sensor.getType() == sensor.TYPE_GYROSCOPE) {
            // xGyroValue.setText("xGValue " + event.values[0]);
            //yGyroValue.setText("yGValue " + event.values[1]);
            //zGyroValue.setText("zGValue " + event.values[2]);

            try {
                gyro.put("x", event.values[0]);
                gyro.put("y", event.values[1]);
                gyro.put("z", event.values[2]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (sensor.getType() == sensor.TYPE_MAGNETIC_FIELD) {
            //xMagnoValue.setText("xMValue " + event.values[0]);
            //yMagnoValue.setText("yMValue " + event.values[1]);
            //zMagnoValue.setText("zMValue " + event.values[2]);

            try {
                magnometer.put("x", event.values[0]);
                magnometer.put("y", event.values[1]);
                magnometer.put("z", event.values[2]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (sensor.getType() == sensor.TYPE_PRESSURE) {
            // pressure.setText("pressure " + event.values[0]);

            try {

                Pressure.put("units", "kPa");
                Pressure.put("value", event.values[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else if (sensor.getType() == sensor.TYPE_AMBIENT_TEMPERATURE) {
            //temp.setText("Temp " + event.values[0]);

            try {

                Temp.put("units", "C");
                Temp.put("value", event.values[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (sensor.getType() == sensor.TYPE_RELATIVE_HUMIDITY) {
            // humi.setText("Humi " + event.values[0]);
            try {

                humidity.put("units", "%");
                humidity.put("value", event.values[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}

