/*
 * Copyright (C) 2013 Tieto Poland Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.System.CORNERSTONE_APP_COUNT;
import static android.provider.Settings.System.CORNERSTONE_START_ON_BOOT;
import static android.provider.Settings.System.CORNERSTONE_APP_0;
import static android.provider.Settings.System.CORNERSTONE_APP_1;
import static android.provider.Settings.System.CORNERSTONE_APP_2;
import static android.provider.Settings.System.CORNERSTONE_APP_3;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class MultiwindowSettings extends SettingsPreferenceFragment implements
Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "MultiwindowSettings";

    private PreferenceCategory applicationSettingScreen;
    private ListPreference applicationCountEditor;
    private CheckBoxPreference cornerstoneStartOnBoot;
    private List<String> applicationNames = new ArrayList<String>();
    private List<String> applicationPackageNames = new ArrayList<String>();
    private PackageManager packageManger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        packageManger =  getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = packageManger.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
        for (ResolveInfo rInfo : list) {
            CharSequence app = rInfo.activityInfo.loadLabel(packageManger);
            if(!app.toString().equals("Multiwindow")){
                applicationNames.add((String) rInfo.activityInfo.loadLabel(packageManger));
                applicationPackageNames.add(rInfo.activityInfo.applicationInfo.packageName);
            }
        }

        // Extract application name from full class path
        String defaultApplicationClassName = getString(R.string.defaultApp);
        int lastDotOccurrence = defaultApplicationClassName.lastIndexOf(".") + 1;
        String defaultApplicationName = "";
        if(lastDotOccurrence < defaultApplicationClassName.length())
            defaultApplicationName = defaultApplicationClassName.substring(lastDotOccurrence);
        else
            Log.v(TAG, "Incorrect default app class name provided in default configuration");

        applicationNames.add(defaultApplicationName);
        applicationPackageNames.add(defaultApplicationClassName);

        addPreferencesFromResource(R.xml.cs_preferences);

        applicationSettingScreen = (PreferenceCategory) getPreferenceScreen().findPreference(getString(R.string.key_application_preference_screen));
        applicationCountEditor = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.key_app_name_count));
        applicationCountEditor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                Log.v(TAG, "Value changed to: " + value);
                Settings.System.putInt(getContentResolver(), CORNERSTONE_APP_COUNT, value);
                inflateApplicationSettingScreen(value);
                return true;
            }
        });
        cornerstoneStartOnBoot = (CheckBoxPreference) getPreferenceScreen().findPreference(getString(R.string.key_start_on_boot));
        cornerstoneStartOnBoot.setChecked(Settings.System.getInt(getContentResolver(), CORNERSTONE_START_ON_BOOT, 0) == 1);
        cornerstoneStartOnBoot.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = (Boolean) newValue ? 1 : 0;
                Settings.System.putInt(getContentResolver(), CORNERSTONE_START_ON_BOOT, value);
                return true;
            }
        });

        inflateApplicationSettingScreen(Settings.System.getInt(getContentResolver(), CORNERSTONE_APP_COUNT, 1));
    }

    @Override
    public void onResume() {
        super.onResume();

        // updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        return null;
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        Log.e(TAG," Pref change objValue= " + objValue);
        if(preference != null && preference instanceof ListPreference) {
            int i = applicationPackageNames.indexOf(objValue);
            Log.e(TAG," Pref change number = " + i);
            if (i >= 0){
                int index = -1;
                try {
                    index = Integer.parseInt(preference.getKey().substring(preference.getKey().lastIndexOf("_") + 1));
                } catch (NumberFormatException e) {
                    return false;
                }
                switch (index) {
                case 0:
                    Settings.System.putString( getContentResolver(), CORNERSTONE_APP_0, (String) objValue);
                    break;
                case 1:
                    Settings.System.putString( getContentResolver(), CORNERSTONE_APP_1, (String) objValue);
                    break;
                case 2:
                    Settings.System.putString( getContentResolver(), CORNERSTONE_APP_2, (String) objValue);
                    break;
                case 3:
                    Settings.System.putString( getContentResolver(), CORNERSTONE_APP_3, (String) objValue);
                    break;
                default:
                    return false;
                }
                preference.setTitle(applicationNames.get(i));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public void inflateApplicationSettingScreen(int neededSize) {

        applicationSettingScreen.removeAll();
        int elementsLeft = applicationSettingScreen.getPreferenceCount();
        String[] appNamesArray = applicationNames.toArray(new String[applicationNames.size()]);
        String[] pkgNamesArray = applicationPackageNames.toArray(new String[applicationPackageNames.size()]);
        for(int i=elementsLeft;i<neededSize;i++) {
            Log.e(TAG,i+" | Adding new list pref: " + ""+i);
            String key = getString(R.string.key_app_name)+"_"+i;
            String value;
            switch (i) {
            case 0:
                value = Settings.System.getString( getContentResolver(), CORNERSTONE_APP_0);
                break;
            case 1:
                value = Settings.System.getString( getContentResolver(), CORNERSTONE_APP_1);
                break;
            case 2:
                value = Settings.System.getString( getContentResolver(), CORNERSTONE_APP_2);
                break;
            case 3:
                value = Settings.System.getString( getContentResolver(), CORNERSTONE_APP_3);
                break;
            default:
                return;
            }
            if (value == null)
                value = getPreferenceManager().getSharedPreferences().getString(key, getString(R.string.defaultApp));
            int indexOfEntry = applicationPackageNames.indexOf(value);
            if (indexOfEntry < 0)
                indexOfEntry = 0;
            ListPreference listPref = new ListPreference(getActivity());
            listPref.setKey(key); //Refer to get the pref value
            listPref.setEntries(appNamesArray);
            listPref.setEntryValues(pkgNamesArray);
            listPref.setDialogTitle(getString(R.string.pref_application_name_title) + " " + (i + 1)); // Name panels from 1 not 0
            listPref.setTitle(applicationNames.get(indexOfEntry));
            listPref.setSummary(getString(R.string.pref_application_name_summary));
            listPref.setOnPreferenceChangeListener(this);
            listPref.setOnPreferenceClickListener(this);
            applicationSettingScreen.addItemFromInflater(listPref);
        }
    }

}
