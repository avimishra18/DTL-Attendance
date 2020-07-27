package com.example.dtlattendance.Adapter;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dtlattendance.Model.AttendanceSession;
import com.example.dtlattendance.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryList extends ArrayAdapter<AttendanceSession> {

    private Activity context;
    private List<AttendanceSession> historyList;

    public HistoryList(Activity context,List<AttendanceSession> historyList){
        super(context, R.layout.list_session,historyList);
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_session,null,true);

        TextView textViewDate = listViewItem.findViewById(R.id.textViewDate);
        TextView textViewInTime = listViewItem.findViewById(R.id.textViewInTime);
        TextView textViewTotalTime = listViewItem.findViewById(R.id.textViewTotalTime);
        ProgressBar circularProgressBar = listViewItem.findViewById(R.id.circularProgressBar);

        String inTime="",outTime="";

        AttendanceSession attendanceSession = historyList.get(position);
        if(attendanceSession.getStartTimeStamp()>1000000) {
            String date = getDate(attendanceSession.getStartTimeStamp());
            inTime = getTime(attendanceSession.getStartTimeStamp());
            outTime = getTime(attendanceSession.getEndTimeStamp());
            textViewDate.setText(date);
            textViewInTime.setText("From " + inTime + " to " + outTime);
        }

        if(!outTime.isEmpty() || !inTime.isEmpty()) {
            Integer minutes = Math.round(attendanceSession.getTotalTime()/60000);
            textViewTotalTime.setText("" + minutes);
            circularProgressBar.setProgress(100);
        }
        else{
            textViewInTime.setText(String.valueOf(attendanceSession.getStartTimeStamp()));
            textViewDate.setText(String.valueOf(attendanceSession.getEndTimeStamp()));
            circularProgressBar.setVisibility(View.GONE);
            textViewTotalTime.setText("");
        }

        //For Online

        return listViewItem;
    }

    //TimeStamp to Date & Time converter
    private String getDate(Long input) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(input);
        String date = DateFormat.format("EEE, dd-MM-yyyy", cal).toString();
        return date;
    }

    private String getTime(Long input) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(input);
        String date = DateFormat.format("hh:mm:ss", cal).toString();
        return date;
    }
}
