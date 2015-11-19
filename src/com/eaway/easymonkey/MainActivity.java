package com.eaway.easymonkey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.eaway.easymonkey.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    public static final String TAG = "EasyMonkey.Main";

    private static final String APP_NAME = "name";
    private static final String APP_PKG = "pkg";
    private static final String APP_ICON = "icon";

    private static final String PKG_PREFIX_ANDROID = "com.android";
    private static final String PKG_PREFIX_GOOGLE = "com.google";

    // private static final String KEY_LABEL = "label";
    // private static final String KEY_NAME = "name";
    // private static final String KEY_PKG = "pkg";

    private ListView mListView;
    private RadioButton mRadioBtnPackage;
    private RadioButton mRadioBtnName;
    private CheckBox mCheckBoxHideAndroid;
    private CheckBox mCheckBoxHideGoogle;

    private PackageManager mPkgMgr;
    private PackageInfo mPkgInfo;

    public static List<PackageInfo> sPkgInfoList;
    public static List<MonkeyApp> sSelectedAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPkgMgr = getPackageManager();
        try {
            mPkgInfo = mPkgMgr.getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mListView = (ListView) findViewById(R.id.appList);
        mRadioBtnPackage = (RadioButton) findViewById(R.id.radioPackage);
        mRadioBtnName = (RadioButton) findViewById(R.id.radioName);
        mCheckBoxHideAndroid = (CheckBox) findViewById(R.id.checkBoxHideAndroid);
        mCheckBoxHideGoogle = (CheckBox) findViewById(R.id.checkBoxHideGoogle);

        refreshAppListView();
    }

    public void onSelectAllButtonClick(View view) {
        for (int i = 0; i < mListView.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) mListView.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.checkBox);
            cb.setChecked(true);
        }
    }

    public void onClearAllButtonClick(View view) {
        

        for (int i = 0; i < mListView.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) mListView.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.checkBox);
            cb.setChecked(false);
        }
    }

    public void onHideButtonClick(View view) {
        refreshAppListView();
    }

    public void onSortButtonClick(View view) {
        if (sPkgInfoList != null) {
            if (mRadioBtnPackage.isChecked()) {
                Collections.sort(sPkgInfoList, new Comparator<PackageInfo>() {
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        return p1.packageName.compareTo(p2.packageName);
                    }
                });
            } else {
                Collections.sort(sPkgInfoList, new Comparator<PackageInfo>() {
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        String n1 = (String) p1.applicationInfo.loadLabel(mPkgMgr);
                        String n2 = (String) p2.applicationInfo.loadLabel(mPkgMgr);
                        return n1.compareTo(n2);
                    }
                });
            }
            refreshAppListView();
        }
    }

    public void onStartButtonClick(View view) {
        
        // Get selected app list
        sSelectedAppList = new ArrayList<MonkeyApp>();
        for (int i = 0; i < mListView.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) mListView.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.checkBox);
            if (cb.isChecked()) {
                TextView pkg = (TextView) itemLayout.findViewById(R.id.appPackage);
                TextView name = (TextView) itemLayout.findViewById(R.id.appName);
                MonkeyApp app = new MonkeyApp((String) name.getText(), (String) pkg.getText());
                sSelectedAppList.add(app);
            }
        }

        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                String version = "Version: " + mPkgInfo.versionName + "\n";
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setTitle(getApplicationInfo().loadLabel(mPkgMgr));
                dlgAlert.setMessage(version + getString(R.string.author));
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean refreshAppListView() {

        // Get installed packages
        ArrayList<HashMap<String, Object>> appList = new ArrayList<HashMap<String, Object>>();
        if (sPkgInfoList == null) {
            sPkgInfoList = mPkgMgr.getInstalledPackages(0);
        }

        for (PackageInfo pkg : sPkgInfoList) {

            // Skip Ourself
            if (pkg.packageName.equalsIgnoreCase(getPackageName()))
                continue;

            // Skip Android packages
            if (mCheckBoxHideAndroid.isChecked()) {
                if (pkg.packageName.contains(PKG_PREFIX_ANDROID))
                    continue;
            }

            // Skip Google packages
            if (mCheckBoxHideGoogle.isChecked()) {
                if (pkg.packageName.contains(PKG_PREFIX_GOOGLE))
                    continue;
            }

            HashMap<String, Object> mapApp = new HashMap<String, Object>();
            mapApp.put(APP_PKG, pkg.packageName);
            mapApp.put(APP_NAME, pkg.applicationInfo.loadLabel(mPkgMgr));
            mapApp.put(APP_ICON, pkg.applicationInfo.loadIcon(mPkgMgr));
            appList.add(mapApp);
        }

        // Bind ListView with content adapter
        SimpleAdapter appAdapter = new SimpleAdapter(this, appList, R.layout.app_list_item,
                new String[] {
                        APP_NAME, APP_PKG, APP_ICON
                },
                new int[] {
                        R.id.appName, R.id.appPackage, R.id.appIcon
                });

        appAdapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                }
                else
                    return false;
            }
        });

        mListView.setAdapter(appAdapter);

        return true;
    }

}
