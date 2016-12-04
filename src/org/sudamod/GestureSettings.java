/*
 * Copyright (C) 2015 The SudaMod project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.sudamod;


import android.content.ContentResolver;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import static android.provider.Settings.Secure.WAKE_GESTURE_ENABLED;
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;
import java.util.ArrayList;
import java.util.List;


import org.cyanogenmod.cmparts.R;
import org.cyanogenmod.cmparts.SettingsPreferenceFragment;
//import com.android.settings.Utils;

import cyanogenmod.providers.CMSettings;

public class GestureSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "GestureSettings";

    private static final String CATEGORY_GESTURES = "category_gestures";
    private static final String CATEGORY_DIRECT_CONTROL = "direct_control";

    private static final String KEY_GESTURES = "device_specific_gesture_settings";
    private static final String KEY_TAP_TO_WAKE = "double_tap_wake_gesture";
    private static final String KEY_PROXIMITY_WAKE = "proximity_on_wake";

    private SwitchPreference mTapToWake;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();

        addPreferencesFromResource(R.xml.gesture_settings);

        // update or remove gesture activity
//        Utils.updatePreferenceToSpecificActivityFromMetaDataOrRemove(getActivity(),
//                getPreferenceScreen(), KEY_GESTURES);

        final PreferenceCategory category_gesture = (PreferenceCategory) findPreference(CATEGORY_GESTURES);
        final PreferenceCategory category_direct_control = (PreferenceCategory) findPreference(CATEGORY_DIRECT_CONTROL);

        mTapToWake = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);

        if (mTapToWake != null && isTapToWakeAvailable(activity.getResources())) {
            mTapToWake.setOnPreferenceChangeListener(this);
        } else {
            if (category_gesture != null && mTapToWake != null) {
                category_gesture.removePreference(mTapToWake);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mTapToWake) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(resolver, DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }

        return true;
    }

    private void updateState() {

        ContentResolver resolver = getActivity().getContentResolver();

        // Update tap-to-wake if it is available.
        if (mTapToWake != null) {
            int value = Settings.Secure.getInt(resolver, DOUBLE_TAP_TO_WAKE, 0);
            mTapToWake.setChecked(value != 0);
        }

    }

    private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }

    private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sensors != null && sensors.getDefaultSensor(Sensor.TYPE_WAKE_GESTURE) != null;
    }

}
