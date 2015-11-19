package com.eaway.easymonkey;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.eaway.easymonkey.R;

public class StartActivity extends Activity {
    private EditText mEditTextEventCount;
    private EditText mEditTextThrottle;
    private CheckBox mCheckBoxIgnoreCrashes;
    private CheckBox mCheckBoxIgnoreTimeouts;
    private CheckBox mCheckBoxKillProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monkey);

        mEditTextEventCount = (EditText) findViewById(R.id.editTextEventCount);
        mEditTextThrottle = (EditText) findViewById(R.id.editTextThrottle);
        mCheckBoxIgnoreCrashes = (CheckBox) findViewById(R.id.checkBoxIgnoreCrashes);
        mCheckBoxIgnoreTimeouts = (CheckBox) findViewById(R.id.checkBoxIgnoreTimeouts);
        mCheckBoxKillProcess = (CheckBox) findViewById(R.id.checkBoxKillProcess);

    }

    public void onStartButtonClick(View view) {
        // Prepare monkey parameter
        String param = " -v";
        param += " --throttle " + mEditTextThrottle.getText();
        if (mCheckBoxIgnoreCrashes.isChecked()) {
            param += " --ignore-crashes";
        }
        if (mCheckBoxIgnoreTimeouts.isChecked()) {
            param += " --ignore-timeouts";
        }
        if (mCheckBoxKillProcess.isChecked()) {
            param += " --kill-process-after-error";
        }
        param += " " + mEditTextEventCount.getText();
        
        // Start Monkey thread
        MonkeyThread monkey = new MonkeyThread(MainActivity.sSelectedAppList, param);
        Thread thread = new Thread(monkey);
        thread.run();
    }
}
