package tech.wendler.wgucompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class NewCourseNotification extends Fragment {

    private SwitchPreference switchPreferenceStartDate;
    private Switch switchStartDate, switchEndDate;

    private boolean notifyOnStartDate, notifyOnEndDate;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Intent alarmIntent;
    private PackageManager packageManager;
    private ComponentName receiver;

    public static final String SHARED_PREFERENCES = "sharedPreferences";
    public static final String NOTIFY_START_DATE = "notifyOnStartDate";
    public static final String NOTIFY_END_DATE = "notifyOnEndDate";
    public static final int START_DATE_NOTIFY_HOUR = 13;
    public static final int START_DATE_NOTIFY_MINUTE = 30;
    public static final int END_DATE_NOTIFY_HOUR = 13;
    public static final int END_DATE_NOTIFY_MINUTE = 30;

    public NewCourseNotification() {

    }

    public static NewCourseNotification newInstance() {
        return new NewCourseNotification();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_course_notification, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switchStartDate = getActivity().findViewById(R.id.switchNewNotifyStartDate);
        switchEndDate = getActivity().findViewById(R.id.switchNewNotifyEndDate);

        switchStartDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                savePreferences();
                loadPreferences();
                handleNotifications();
            }
        });

        loadPreferences();
        updateViewsFromPreferences();

    }

    public void handleNotifications() {
        packageManager = getActivity().getPackageManager();
        receiver = new ComponentName(getActivity(), DeviceBootReceiver.class);
        alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0);

        if (notifyOnStartDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, START_DATE_NOTIFY_HOUR);
            calendar.set(Calendar.MINUTE, START_DATE_NOTIFY_MINUTE);
            calendar.set(Calendar.SECOND, 1);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            if (alarmManager != null) {
                //Creates notification at specific time defined in calendar object
                //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                //Creates notification in 10 seconds
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5 * 1000, pendingIntent);
            }

            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        } else {
            if (PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0) != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }

        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

    public void savePreferences() {
        if (getActivity() != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(NOTIFY_START_DATE, switchStartDate.isChecked());
            editor.putBoolean(NOTIFY_END_DATE, switchEndDate.isChecked());

            editor.apply();
            Toast.makeText(getContext(), "Preferences saved successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        notifyOnStartDate = preferences.getBoolean(NOTIFY_START_DATE, false);
        notifyOnEndDate = preferences.getBoolean(NOTIFY_END_DATE, false);
    }

    public void updateViewsFromPreferences() {
        switchStartDate.setChecked(notifyOnStartDate);
        switchEndDate.setChecked(notifyOnEndDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((ViewCourseNotification) getActivity()).setActionBarTitle("Course Notifications");
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }

    }
}