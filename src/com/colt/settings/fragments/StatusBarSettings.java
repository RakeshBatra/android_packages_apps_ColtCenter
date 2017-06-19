/*
 * Copyright (C) 2017 ColtOS Project
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

package com.colt.settings.fragments;

import com.colt.settings.preferences.CustomSeekBarPreference;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.provider.Settings;
import android.os.UserHandle;
import android.view.View;
import android.widget.EditText;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import cyanogenmod.preference.CMSystemSettingListPreference;

import java.util.Date;

public class StatusBarSettings extends SettingsPreferenceFragment
	implements Preference.OnPreferenceChangeListener {

	private static final String STATUS_BAR_CLOCK_POSITION = "status_bar_clock";
	private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
	private static final String STATUS_BAR_DATE = "status_bar_date";
	private static final String STATUS_BAR_DATE_STYLE = "status_bar_date_style";
	private static final String STATUS_BAR_DATE_FORMAT = "status_bar_date_format";
	private static final String PREF_FONT_STYLE = "statusbar_clock_font_style";
        private static final String PREF_STATUS_BAR_CLOCK_FONT_SIZE  = "statusbar_clock_font_size";
        private static final String PREF_CLOCK_DATE_POSITION = "statusbar_clock_date_position";
	private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
        private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
	private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";

	private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 4;
	private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 6;

	public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
	public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
	private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

	private CMSystemSettingListPreference mStatusBarClock;
        private CMSystemSettingListPreference mStatusBarAmPm;
	private CMSystemSettingListPreference mStatusBarDate;
	private CMSystemSettingListPreference mStatusBarDateStyle;
	private CMSystemSettingListPreference mStatusBarDateFormat;
	private CMSystemSettingListPreference mFontStyle;
        private CustomSeekBarPreference mStatusBarClockFontSize;
        private CMSystemSettingListPreference mClockDatePosition;
	private CMSystemSettingListPreference mStatusBarBattery;
	private CMSystemSettingListPreference mStatusBarBatteryShowPercent;
        private CMSystemSettingListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

	mStatusBarClock = (CMSystemSettingListPreference) findPreference(STATUS_BAR_CLOCK_POSITION);
	mStatusBarBatteryShowPercent =
		(CMSystemSettingListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

	mStatusBarAmPm = (CMSystemSettingListPreference) findPreference(STATUS_BAR_AM_PM);
	if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        mStatusBarDate = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE);
	int showDate = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE, 0);
        mStatusBarDate.setValue(String.valueOf(showDate));
        mStatusBarDate.setSummary(mStatusBarDate.getEntry());
        mStatusBarDate.setOnPreferenceChangeListener(this);

        int dateStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_DATE_STYLE, 0);
	mStatusBarDateStyle = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE_STYLE);
        mStatusBarDateStyle.setValue(String.valueOf(dateStyle));
        mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntry());
        mStatusBarDateStyle.setOnPreferenceChangeListener(this);

	mStatusBarDateFormat = (CMSystemSettingListPreference) findPreference(STATUS_BAR_DATE_FORMAT);
        String dateFormat = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = "EEE";
	}
	mStatusBarDateFormat.setValue(dateFormat);
	mStatusBarDateFormat.setOnPreferenceChangeListener(this);
	mStatusBarDateFormat.setSummary(DateFormat.format(dateFormat, new Date()));

	parseClockDateFormats();

	mFontStyle = (CMSystemSettingListPreference) findPreference(PREF_FONT_STYLE);
	int fontStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_FONT_STYLE, 0);
        mFontStyle.setValue(String.valueOf(fontStyle));
        mFontStyle.setSummary(mFontStyle.getEntry());
        mFontStyle.setOnPreferenceChangeListener(this);

	mStatusBarClockFontSize = (CustomSeekBarPreference) findPreference(PREF_STATUS_BAR_CLOCK_FONT_SIZE);
        mStatusBarClockFontSize.setValue(Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_FONT_SIZE, 14));
        mStatusBarClockFontSize.setOnPreferenceChangeListener(this);

	mClockDatePosition = (CMSystemSettingListPreference) findPreference(PREF_CLOCK_DATE_POSITION);
        int clockdatePosition = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_POSITION, 0);
        mClockDatePosition.setValue(String.valueOf(clockdatePosition));
        mClockDatePosition.setSummary(mClockDatePosition.getEntry());
        mClockDatePosition.setOnPreferenceChangeListener(this);

	mStatusBarBattery = (CMSystemSettingListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(0));
	mStatusBarBattery.setOnPreferenceChangeListener(this);

	mQuickPulldown = (CMSystemSettingListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
	updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));
	mQuickPulldown.setOnPreferenceChangeListener(this);

	setStatusBarDateDependencies();
    }

     @Override
     public void onResume() {
        super.onResume();
	// Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);

        }
    }

    @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mStatusBarBattery) {
	   int value = Integer.valueOf((String) newValue);
	   enableStatusBarBatteryDependents(value);
	return true;
	} else if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateQuickPulldownSummary(value);
            return true;
        } else if (preference == mStatusBarDate) {
            int statusBarDate = Integer.parseInt((String) newValue);
            int index = mStatusBarDate.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_DATE, statusBarDate);
            mStatusBarDate.setSummary(mStatusBarDate.getEntries()[index]);
            setStatusBarDateDependencies();
            return true;
        } else if (preference == mStatusBarDateStyle) {
            int statusBarDateStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, STATUS_BAR_DATE_STYLE, statusBarDateStyle);
            mStatusBarDateStyle.setSummary(mStatusBarDateStyle.getEntries()[index]);
            return true;
        } else if (preference ==  mStatusBarDateFormat) {
            int index = mStatusBarDateFormat.findIndexOfValue((String) newValue);
            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.status_bar_date_string_edittext_title);
                alert.setMessage(R.string.status_bar_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(
                    resolver,
                    Settings.System.STATUS_BAR_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(resolver,
                            Settings.System.STATUS_BAR_DATE_FORMAT, value);

                        mStatusBarDateFormat.setSummary(DateFormat.format(value, new Date()));

                        return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_DATE_FORMAT, (String) newValue);
                    mStatusBarDateFormat.setSummary(
                            DateFormat.format((String) newValue, new Date()));
                }
            }
            return true;
  	} else if (preference == mFontStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_FONT_STYLE, val);
            mFontStyle.setSummary(mFontStyle.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarClockFontSize) {
            int size = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_FONT_SIZE, size);
            return true;
        } else if (preference == mClockDatePosition) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockDatePosition.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
            mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
            parseClockDateFormats();
            return true;
         }

        return false;
   }

	private void enableStatusBarBatteryDependents(int batteryIconStyle) {
	 mStatusBarBatteryShowPercent.setEnabled(
                batteryIconStyle != STATUS_BAR_BATTERY_STYLE_HIDDEN
                && batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);

 }

	private void setStatusBarDateDependencies() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int showDate = Settings.System.getInt(getActivity()
                        .getContentResolver(), Settings.System.STATUS_BAR_DATE, 0);
		mStatusBarDateStyle.setEnabled(showDate != 0);
		mStatusBarDateFormat.setEnabled(showDate != 0);
		mClockDatePosition.setEnabled(showDate != 0);

            }
        });
    }

    private void parseClockDateFormats() {
        // Parse and repopulate mClockDateFormats's entries based on current date.
        String[] dateEntries = getResources().getStringArray(R.array.status_bar_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }

                parsedDateEntries[i] = newDate;
            }
        }
        mStatusBarDateFormat.setEntries(parsedDateEntries);
     }

	private void updateQuickPulldownSummary(int value) {
        mQuickPulldown.setSummary(value == 0
                ? R.string.status_bar_quick_qs_pulldown_off
                : R.string.status_bar_quick_qs_pulldown_summary);

    }

   @Override
    protected int getMetricsCategory() {
        return MetricsEvent.COLT;
    }

}
