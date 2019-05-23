package com.example.dtlattendance;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class HistoryList extends ArrayAdapter<AttendanceSession> {

    private Activity context;
    private List<AttendanceSession> historyList;

    public HistoryList(Activity context,List<AttendanceSession> historyList){
        super(context,R.layout.list_session,historyList);
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

        AttendanceSession attendanceSession = historyList.get(position);
        if(attendanceSession.getStartDate().length()>12) {
            String date = attendanceSession.startDate.substring(0, 12);
            String inTime = attendanceSession.startDate.substring(13);
            String outTime = attendanceSession.endDate.substring(13);
            textViewDate.setText(date);
            textViewInTime.setText("From " + inTime + " to " + outTime);
        }
        //textViewInTime.setText("In: " + attendanceSession.getStartDate());
        //textViewDate.setText("Out: " + attendanceSession.getEndDate());

        if(!attendanceSession.getTime().isEmpty()) {
            Float seconds = Float.valueOf(attendanceSession.getTime());
            Integer minutes = Math.round(seconds / 60);
            textViewTotalTime.setText("" + minutes);
            circularProgressBar.setProgress(100);
        }
        else{
            textViewInTime.setText(attendanceSession.getStartDate());
            textViewDate.setText(attendanceSession.getEndDate());
            circularProgressBar.setVisibility(View.GONE);
            textViewTotalTime.setText("");
        }

        return listViewItem;
    }
}
