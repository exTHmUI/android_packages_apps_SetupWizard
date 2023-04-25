package net.hearnsoft.setupwizard.views;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.FragmentDatetimeViewBinding;

import java.util.Calendar;
import java.util.Locale;

public class DateTimeFragment extends Fragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private FragmentDatetimeViewBinding binding;
    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";
    private Calendar calendar;
    private boolean isAutoTimeEnabled, is24HourFormat;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeAndDateDisplay();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentDatetimeViewBinding.inflate(getLayoutInflater());
        calendar = Calendar.getInstance();
        setNtpServer(getResources().getStringArray(R.array.ntp_server)[0]);
        isAutoTimeEnabled = isAutoTimeEnabled();
        is24HourFormat = is24HourLocale(Locale.getDefault());
        updateTimeAndDateDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        requireContext().registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(mIntentReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.time.setOnClickListener(v -> showTimePickerDialog());
        binding.date.setOnClickListener(v -> showDatePickerDialog());
        binding.autoTimeSwitch.setChecked(isAutoTimeEnabled);
        binding.autoTimeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setAutoTime(isChecked);
            isAutoTimeEnabled = isAutoTimeEnabled();
            timeUpdated(requireContext(), isAutoTimeEnabled);
        });
        binding.formatSwitch.setChecked(is24HourFormat);
        binding.formatSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            set24HHour(isChecked);
            timeUpdated(requireContext(), isChecked);
        });
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setNtpServer(getResources().getStringArray(R.array.ntp_server)[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    //显示时间选择器
    private void showTimePickerDialog() {
        TimePickerFragment fragment = TimePickerFragment.newInstance();
        fragment.show(getChildFragmentManager(), "timePicker");
    }

    //显示日期选择器
    private void showDatePickerDialog() {
        DatePickerFragment fragment = DatePickerFragment.newInstance();
        fragment.show(getChildFragmentManager(), "datePicker");
    }

    //是否是24小时制
    private boolean is24HourLocale(Locale locale) {
        return DateFormat.is24HourLocale(locale);
    }

    //是否是自动更新时间
    private boolean isAutoTimeEnabled() {
        return Settings.Global.getInt(requireContext().getContentResolver(),
                Settings.Global.AUTO_TIME, 0) > 0;
    }

    //设置自动更新时间
    private void setAutoTime(boolean isChecked) {
        Settings.Global.putInt(requireContext().getContentResolver(),
                Settings.Global.AUTO_TIME, isChecked ? 1 : 0);
        updateTimeAndDateDisplay();
    }

    //设置是否是24小时制
    private void set24HHour(boolean isChecked){
        binding.formatSwitch.setChecked(isChecked);
        String value = isChecked ? HOURS_24 : HOURS_12;
        Settings.System.putString(requireContext().getContentResolver(),
                Settings.System.TIME_12_24, value);
    }

    //更新时间和日期显示
    @SuppressLint("WrongConstant")
    static void timeUpdated(Context context, Boolean is24Hour) {
        try {
            Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
            timeChanged.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            int timeFormatPreference;
            if (is24Hour == null) {
                timeFormatPreference = Intent.EXTRA_TIME_PREF_VALUE_USE_LOCALE_DEFAULT;
            } else {
                timeFormatPreference = is24Hour ? Intent.EXTRA_TIME_PREF_VALUE_USE_24_HOUR
                        : Intent.EXTRA_TIME_PREF_VALUE_USE_12_HOUR;
            }
            timeChanged.putExtra(Intent.EXTRA_TIME_PREF_24_HOUR_FORMAT, timeFormatPreference);
            context.sendBroadcast(timeChanged);
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    //设置系统时间
    private void setTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    //设置系统日期
    private void setDate(int year, int month, int day) {
        Log.d("setDate", "year: " + year + " month: " + month + " day: " + day);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = calendar.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    //显示时间选择器
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public static TimePickerFragment newInstance() {
            return new TimePickerFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(
                    requireContext(),
                    this,
                    hour,
                    minute,
                    DateFormat.is24HourFormat(requireContext())
            );
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            ((DateTimeFragment) getParentFragment()).setTime(hourOfDay, minute);
        }
    }

    //显示日期选择器
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public static DatePickerFragment newInstance() {
            return new DatePickerFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(
                    requireContext(),
                    this,
                    year,
                    month,
                    day
            );
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            ((DateTimeFragment) getParentFragment()).setDate(year, month, dayOfMonth);
        }
    }

    //设置NTP服务器
    private void setNtpServer(String serverUrl){
        Settings.Global.putString(
                requireContext().getContentResolver(),
                Settings.Global.NTP_SERVER,
                serverUrl
        );
        Toast.makeText(requireContext(), getString(R.string.set_ntp_toast,serverUrl) , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        updateTimeAndDateDisplay();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setTime(hourOfDay, minute);
        updateTimeAndDateDisplay();
    }

    private void updateTimeAndDateDisplay() {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(requireContext());
        final Calendar now = Calendar.getInstance();
        binding.time.setText(DateFormat.getTimeFormat(requireActivity()).format(now.getTime()));
        binding.date.setText(shortDateFormat.format(now.getTime()));
    }

}
