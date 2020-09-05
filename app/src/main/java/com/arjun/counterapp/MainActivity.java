package com.arjun.counterapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.arjun.counterapp.databinding.ActivityMainBinding;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AudioManager audioManager;
    private Ringtone defaultRingtone;
    private Vibrator vibrator;
    private Boolean isRunning ;
    private static long timeLeftIn = 60000;
    private CountDownTimer timerTask;
    private long toEndTime;
    ActivityMainBinding mainBinding ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        mainBinding = ActivityMainBinding.inflate(inflater);
        setContentView(mainBinding.getRoot());

        mainBinding.end.setOnClickListener(this);
        mainBinding.end.setClickable(false);
        mainBinding.start.setOnClickListener(this);
        isRunning=false;

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), defaultRingtoneUri);


        //Sliding Bar Custom Pointer Text
        mainBinding.setTimer.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return formattedTime((int) value);
            }
        });
        mainBinding.setTimer.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int i = (int) slider.getValue();
                String s = formattedTime((int) (i));
                mainBinding.time.setText(s);
                mainBinding.progressBar.setProgress((int) value / 6);
            }

        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                if(!isRunning) {
                    setButtonProperty(false,0.4f,1f);
                    timeLeftIn = (int) (mainBinding.setTimer.getValue() * 1000);
                    if(mainBinding.setTimer.getValue()==0){
                        Toast.makeText(this, "Can't Start Timer in 0 second", Toast.LENGTH_SHORT).show();
                        setButtonProperty(true,1f,0.4f);
                        return;
                    }
                }
                setTimer();
                return;
            case R.id.end:
                isRunning=false;
                timerTask.cancel();
                setButtonProperty(true,1f,0.4f);
                mainBinding.time.setText(formattedTime((int) mainBinding.setTimer.getValue()));
                mainBinding.progressBar.setProgress((int) mainBinding.setTimer.getValue()/6);

        }

    }

    private void setButtonProperty(boolean b, float forStart,float forEnd) {
        mainBinding.start.setClickable(b);
        mainBinding.start.setAlpha(forStart);
        mainBinding.setTimer.setClickable(b);
        mainBinding.setTimer.setFocusable(b);
        mainBinding.setTimer.setEnabled(b);
        mainBinding.end.setAlpha(forEnd);
        mainBinding.end.setClickable(!b);

    }


    //Formatted String For Slider Material
    private String formattedTime(int i) {
        int min = i / 60;
        int sec = i - min * 60;
        String second = String.valueOf(sec);
        if (second.equals("0")) {
            second = "00";
        } else if (sec <= 9) {
            second = "0" + second;
        }
        return min + ":" + second;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeft", (timeLeftIn));
        outState.putBoolean("isRunning",isRunning);
        outState.putLong("timeEnd", (toEndTime));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftIn =savedInstanceState.getLong("timeLeft");
        boolean isRunning = savedInstanceState.getBoolean("isRunning");
        if(isRunning) {
            setButtonProperty(false, 0.4f, 1f);
            toEndTime = savedInstanceState.getLong("timeEnd");
            timeLeftIn = toEndTime-System.currentTimeMillis();
            setTimer();
        }

    }

    private void setTimer() {
        toEndTime = System.currentTimeMillis()+timeLeftIn;
        timerTask = new CountDownTimer(timeLeftIn, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftIn = l;
                String s = formattedTime((int) (l / 1000));
                mainBinding.time.setText(s);
                mainBinding.progressBar.setProgress((int) (l / 6000));
            }

            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onFinish() {
                isRunning=false;
                setButtonProperty(true, 1f, 0.4f);
                switch (audioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        defaultRingtone.play();
                        final Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            public void run() {
                                defaultRingtone.stop();
                                t.cancel();
                            }
                        }, 8000);
                        defaultRingtone.setLooping(false);
                        return;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        vibrator.vibrate(3000);
                }
            }

        }.start();
        isRunning = true;

    }
}