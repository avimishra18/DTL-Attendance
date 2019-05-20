package com.example.dtlattendance;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryList extends ArrayAdapter<AttendanceSession> {

    private Activity context;
    private List<AttendanceSession> historyList;

    public HistoryList(Activity context,List<AttendanceSession> historyList){
        super(context,R.layout.list_layout,historyList);
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_layout,null,true);

        TextView textViewInTime = listViewItem.findViewById(R.id.textViewInTime);
        TextView textViewOutTime = listViewItem.findViewById(R.id.textViewOutTime);
        TextView textViewTotalTime = listViewItem.findViewById(R.id.textViewTotalTime);

        AttendanceSession attendanceSession = historyList.get(position);
        textViewInTime.setText("In:"+attendanceSession.getStartDate());
        textViewOutTime.setText("Out:"+attendanceSession.getEndDate());
        textViewTotalTime.setText(attendanceSession.getTime());

        return listViewItem;
    }
}
