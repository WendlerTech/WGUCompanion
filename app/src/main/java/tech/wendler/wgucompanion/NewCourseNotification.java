package tech.wendler.wgucompanion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class NewCourseNotification extends Fragment {

    private boolean notifyOnStartDate = false, notifyOnEndDate = false,
            startNotificationSet = false, endNotificationSet = false;
    private int calStartMonth, calStartYear, calEndMonth, calEndYear;
    private Course selectedCourse;
    private AlarmManager startAlarmManager, endAlarmManager;
    private PendingIntent startPendingIntent, endPendingIntent;
    private Intent alarmIntent;
    private PackageManager packageManager;
    private ComponentName receiver;
    private Button btnStartDateNotify, btnEndDateNotify;

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

        Bundle courseBundle = this.getArguments();
        if (courseBundle != null) {
            selectedCourse = (Course) courseBundle.getSerializable("selectedCourse");
        }

        TextView lblCourseTitle = getActivity().findViewById(R.id.lblNewNotifyCourseTitle);
        TextView lblStartDate = getActivity().findViewById(R.id.lblNewNotifyStartDate);
        TextView lblEndDate = getActivity().findViewById(R.id.lblNewNotifyEndDate);
        btnStartDateNotify = getActivity().findViewById(R.id.btnNotifyCourseStart);
        btnEndDateNotify = getActivity().findViewById(R.id.btnNotifyCourseEnd);
        Button btnCancelNotify = getActivity().findViewById(R.id.btnCancelCourseNotify);

        //Splits date strings into month/year
        String startDate = selectedCourse.getStartDate();
        String endDate = selectedCourse.getEndDate();
        String startMonth, startYear, endMonth, endYear;
        if (startDate.length() == 6) {
            startMonth = startDate.substring(0, 1);
            startYear = startDate.substring(2);
        } else {
            startMonth = startDate.substring(0, 2);
            startYear = startDate.substring(3);
        }

        if (endDate.length() == 6) {
            endMonth = endDate.substring(0, 1);
            endYear = endDate.substring(2);
        } else {
            endMonth = endDate.substring(0, 2);
            endYear = endDate.substring(3);
        }

        try {
            //Converts month/year into integers
            calStartMonth = Integer.parseInt(startMonth);
            calStartYear = Integer.parseInt(startYear);
            calEndMonth = Integer.parseInt(endMonth);
            calEndYear = Integer.parseInt(endYear);
        } catch (NumberFormatException ignored) {

        }

        lblCourseTitle.setText(selectedCourse.getCourseTitle());
        lblStartDate.setText("Start date: " + selectedCourse.getStartDate());
        lblEndDate.setText("End date: " + selectedCourse.getEndDate());

        packageManager = getActivity().getPackageManager();
        receiver = new ComponentName(getActivity(), DeviceBootReceiver.class);
        alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        startAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        endAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        btnStartDateNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseStartNotificationContent();
                notifyOnStartDate = true;
                //Avoids issues with using multiple pending intent values
                alarmIntent.setAction(Long.toString(System.currentTimeMillis()));
                handleNotifications();
                //Changes button color to signify notification has been turned on
                btnStartDateNotify.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.greenBtn));
                btnStartDateNotify.setTextColor(ContextCompat.getColor(getContext(), R.color.offWhite));
            }
        });

        btnEndDateNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                courseEndNotificationContent();
                notifyOnEndDate = true;
                alarmIntent.setAction(Long.toString(System.currentTimeMillis()));
                handleNotifications();
                btnEndDateNotify.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.greenBtn));
                btnEndDateNotify.setTextColor(ContextCompat.getColor(getContext(), R.color.offWhite));
            }
        });

        btnCancelNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyOnStartDate = false;
                notifyOnEndDate = false;
                handleNotifications();
                btnStartDateNotify.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultBtnText));
                btnStartDateNotify.setBackgroundColor((ContextCompat.getColor(getContext(), R.color.defaultBtnBg)));
                btnEndDateNotify.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBtnBg));
                btnEndDateNotify.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultBtnText));
            }
        });
    }

    public void handleNotifications() {
        startPendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        endPendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Checks to make sure at least 1 button was pressed
        if (notifyOnStartDate || notifyOnEndDate) {
            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(System.currentTimeMillis());
            endCalendar.setTimeInMillis(System.currentTimeMillis());

            //Sets calendars to course start & end dates
            if (notifyOnStartDate) {
                //First day of course start month at 9:30AM
                startCalendar.set(Calendar.MONTH, calStartMonth - 1);
                startCalendar.set(Calendar.YEAR, calStartYear);
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                startCalendar.set(Calendar.HOUR_OF_DAY, 9);
                startCalendar.set(Calendar.MINUTE, 30);
                startCalendar.set(Calendar.SECOND, 1);
            }
            if (notifyOnEndDate) {
                //Last day of course end month at 9:30AM
                endCalendar.set(Calendar.MONTH, calEndMonth - 1);
                endCalendar.set(Calendar.YEAR, calEndYear);
                endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCalendar.set(Calendar.HOUR_OF_DAY, 9);
                endCalendar.set(Calendar.MINUTE, 30);
                endCalendar.set(Calendar.SECOND, 1);
            }

            if (startAlarmManager != null) {
                if (!startNotificationSet && notifyOnStartDate) {
                    //Creates notification at specific time defined in calendar object
                    startAlarmManager.set(AlarmManager.RTC_WAKEUP, startCalendar.getTimeInMillis(), startPendingIntent);
                    startNotificationSet = true;
                    Toast.makeText(getContext(), "You'll receive a notification on: "
                            + startCalendar.getTime().toString(), Toast.LENGTH_LONG).show();
                }
            }
            if (endAlarmManager != null) {
                if (!endNotificationSet && notifyOnEndDate) {
                    //Creates notification at specific time defined in calendar object
                    endAlarmManager.set(AlarmManager.RTC_WAKEUP, endCalendar.getTimeInMillis(), endPendingIntent);
                    endNotificationSet = true;
                    Toast.makeText(getContext(), "You'll receive a notification on: "
                            + endCalendar.getTime().toString(), Toast.LENGTH_LONG).show();
                }
            }
            packageManager.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        } else {
            //Cancels any pending notifications
            if (PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0) != null
                    && startAlarmManager != null && endAlarmManager != null) {
                startAlarmManager.cancel(startPendingIntent);
                endAlarmManager.cancel(endPendingIntent);
                startNotificationSet = false;
                endNotificationSet = false;
                Toast.makeText(getContext(), "All pending notifications have been cancelled.",
                        Toast.LENGTH_LONG).show();
            }
        }
        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    //Sets notification text to course start
    private void courseStartNotificationContent() {
        alarmIntent.putExtra("title", "Course Reminder");
        alarmIntent.putExtra("content", "Your course, " + selectedCourse.getCourseTitle()
                + ", is starting soon!");
    }

    //Sets notification text to course end
    private void courseEndNotificationContent() {
        alarmIntent.putExtra("title", "Course Reminder");
        alarmIntent.putExtra("content", "Your course, " + selectedCourse.getCourseTitle()
                + ", is ending soon!");
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
