package com.example.dtlattendance;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {

    //Declaration
    LineChart lineChart;
    int linerChartColor =  Color.parseColor("#F9484A");
    int lineColor =  Color.parseColor("#FBDF55");

    ArrayList<Float> timeValues = new ArrayList<>();
    List<AttendanceSession> attendanceSessionList;
    ListView listViewSession;
    CardView cardViewSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        listViewSession = view.findViewById(R.id.listViewSession);
        cardViewSession = view.findViewById(R.id.cardViewSession);

        lineChart = view.findViewById(R.id.sessionChart);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(true);
        lineChart.setGridBackgroundColor(Color.WHITE);
        lineChart.setDrawBorders(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = wmbPreference.edit();
        editor.putBoolean("first",true);
        editor.commit();

        attendanceSessionList = new ArrayList<>();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseDatabase.getInstance().getReference("Sessions")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        attendanceSessionList.clear();

                        for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                            AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                            attendanceSessionList.add(attendanceSession);
                            float value = (float) Double.parseDouble(attendanceSession.getTime());
                            timeValues.add(value);
                        }
                        try {
                            HistoryList adapter = new HistoryList(getActivity(), attendanceSessionList);
                            listViewSession.setAdapter(adapter);
                            setCharData();
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        AttendanceSession attendanceSession = new AttendanceSession("No Session", "Slow connection?", "");
        attendanceSessionList.add(attendanceSession);
        HistoryList adapter = new HistoryList(getActivity(), attendanceSessionList);
        listViewSession.setAdapter(adapter);
    }

    private void setCharData(){

        final ArrayList<Entry> yValues = new ArrayList<>();

        for(int i=0;i<timeValues.size();i++){
            yValues.add(new Entry(i,timeValues.get(i)));
        }

        LineDataSet lineDataSet;
        lineDataSet = new LineDataSet(yValues,"Total Time");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(lineColor);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(1);
        lineDataSet.setFillAlpha(255);
        lineDataSet.setDrawFilled(true);
        Drawable gradientDrawable = ContextCompat.getDrawable(getContext(),R.drawable.background_ui);
        lineDataSet.setFillDrawable(gradientDrawable);

        //lineDataSet.setFillColor(linerChartColor);

        LineData lineData = new LineData(lineDataSet);
        lineData.setDrawValues(false);
        lineChart.setData(lineData);
        lineChart.invalidate();
        cardViewSession.setVisibility(View.VISIBLE);
    }
}
