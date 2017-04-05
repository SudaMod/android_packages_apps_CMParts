/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017 The LineageOS Project
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
package org.cyanogenmod.cmparts.statusbar;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;

import cyanogenmod.preference.CMSystemSettingListPreference;

import org.cyanogenmod.cmparts.R;
import org.cyanogenmod.cmparts.SettingsPreferenceFragment;

import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Spannable;
import android.content.Intent;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String CARRIER_SIZE_STYLE = "carrier_size_style";
    private static final String STATUS_BAR_NETWORK_TRAFFIC_STYLE = "status_bar_network_traffic_style";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;
    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
    private static final String PREF_COLUMNS = "qs_columns";

    private CMSystemSettingListPreference mQuickPulldown;
    private CMSystemSettingListPreference mStatusBarBattery;
    private CMSystemSettingListPreference mStatusBarBatteryShowPercent;

    private SwitchPreference mStatusBarCarrier;
    private PreferenceScreen mCustomCarrierLabel;
    private ListPreference mCarrierSize;
    private String mCustomCarrierLabelText;

    private ListPreference mStatusBarNetworkTraffic;

    private ListPreference mRowsPortrait;
    private ListPreference mQsColumns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

        mStatusBarBatteryShowPercent =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);


        mStatusBarBattery =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(2));

        mQuickPulldown =
                (CMSystemSettingListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        ContentResolver resolver = getActivity().getContentResolver();

        mCarrierSize = (ListPreference) findPreference(CARRIER_SIZE_STYLE);
        int CarrierSize = Settings.System.getInt(resolver,
                Settings.System.CARRIER_SIZE, 5);
        mCarrierSize.setValue(String.valueOf(CarrierSize));
        mCarrierSize.setSummary(mCarrierSize.getEntry());
        mCarrierSize.setOnPreferenceChangeListener(this);

        PreferenceScreen prefSet = getPreferenceScreen();
        mStatusBarCarrier = (SwitchPreference) prefSet
                .findPreference(STATUS_BAR_CARRIER);
        mStatusBarCarrier.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CARRIER, 0) == 1));
        mStatusBarCarrier.setOnPreferenceChangeListener(this);
        mCustomCarrierLabel = (PreferenceScreen) prefSet
                .findPreference(CUSTOM_CARRIER_LABEL);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            prefSet.removePreference(mStatusBarCarrier);
            prefSet.removePreference(mCustomCarrierLabel);
        } else {
            updateCustomLabelTextSummary();
        }

        mStatusBarNetworkTraffic = (ListPreference) findPreference(STATUS_BAR_NETWORK_TRAFFIC_STYLE);
        int networkTrafficStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_STYLE, 3);
        mStatusBarNetworkTraffic.setValue(String.valueOf(networkTrafficStyle));
        mStatusBarNetworkTraffic
                .setSummary(mStatusBarNetworkTraffic.getEntry());
        mStatusBarNetworkTraffic.setOnPreferenceChangeListener(this);


        mRowsPortrait = (ListPreference) findPreference(PREF_ROWS_PORTRAIT);
        int rowsPortrait = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_ROWS_PORTRAIT, 3);
        mRowsPortrait.setValue(String.valueOf(rowsPortrait));
        mRowsPortrait.setSummary(mRowsPortrait.getEntry());
        mRowsPortrait.setOnPreferenceChangeListener(this);

        mQsColumns = (ListPreference) findPreference(PREF_COLUMNS);
        int columnsQs = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.QS_COLUMNS, 3);
        mQsColumns.setValue(String.valueOf(columnsQs));
        mQsColumns.setSummary(mQsColumns.getEntry());
        mQsColumns.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) objValue);
            enableStatusBarBatteryDependents(batteryStyle);
        } else if (preference == mQuickPulldown) {
		    int quickpulldown = Integer.valueOf((String) objValue);
            updateQuickPulldownSummary(quickpulldown);
		} else if (preference == mStatusBarCarrier) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER, value ? 1 : 0);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
            getActivity().sendBroadcast(i);
        } else if (preference == mCarrierSize) {
            String newValue = (String) objValue;
            int CarrierSize = Integer.valueOf((String) newValue);
            int index = mCarrierSize.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.CARRIER_SIZE,
                    CarrierSize);
            mCarrierSize.setSummary(mCarrierSize.getEntries()[index]);
            Intent i = new Intent();
            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
            getActivity().sendBroadcast(i);
        } else if (preference == mStatusBarNetworkTraffic) {
            String newValue = (String) objValue;
            int networkTrafficStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarNetworkTraffic
                    .findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_TRAFFIC_STYLE,
                    networkTrafficStyle);
            mStatusBarNetworkTraffic.setSummary(mStatusBarNetworkTraffic
                    .getEntries()[index]);
        } else if (preference == mRowsPortrait) {
            int intValue = Integer.valueOf((String) objValue);
            int index = mRowsPortrait.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_ROWS_PORTRAIT, intValue);
            preference.setSummary(mRowsPortrait.getEntries()[index]);
        } else if (preference == mQsColumns) {
            int intValue = Integer.valueOf((String) objValue);
            int index = mQsColumns.findIndexOfValue((String) objValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.QS_COLUMNS, intValue);
            preference.setSummary(mQsColumns.getEntries()[index]);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);
            LinearLayout parent = new LinearLayout(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            parent.setLayoutParams(params);
            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? ""
                    : mCustomCarrierLabelText);
            input.setSelection(input.getText().length());
            params.setMargins(60, 0, 60, 0);
            input.setLayoutParams(params);
            parent.addView(input);
            alert.setView(parent);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            String value = ((Spannable) input.getText())
                                    .toString().trim();
                            Settings.System
                                    .putString(
                                            resolver,
                                            Settings.System.CUSTOM_CARRIER_LABEL,
                                            value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(getActivity()
                .getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);

        if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            mCustomCarrierLabel
                    .setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
        }
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(
                batteryIconStyle != STATUS_BAR_BATTERY_STYLE_HIDDEN
                && batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }
}
