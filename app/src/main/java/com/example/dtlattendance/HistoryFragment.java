package com.example.dtlattendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    //Declaration
    BarChart barChart;
    int lineColorPurple =  Color.parseColor("#983CFF");
    int lineColorBlue =  Color.parseColor("#2FB4FF");

    RelativeLayout relativeLayoutHistory;

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
        relativeLayoutHistory = view.findViewById(R.id.relativeLayoutHistory);
        barChart = view.findViewById(R.id.sessionChart);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.setGridBackgroundColor(Color.WHITE);
        barChart.setDrawBorders(true);
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend legend = barChart.getLegend();
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

        SharedPreferences pref = getActivity().getSharedPreferences("User",Context.MODE_PRIVATE);
        String storedAdmin =  pref.getString("admin","0");

        if(storedAdmin.equals("0")) {
            FirebaseDatabase.getInstance().getReference("Sessions")
                    .child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            attendanceSessionList.clear();

                            for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                attendanceSessionList.add(attendanceSession);
                                float value = attendanceSession.getTotalTime();
                                value/= 60000;
                                timeValues.add(value);
                            }
                            try {
                                HistoryList adapter = new HistoryList(getActivity(), attendanceSessionList);
                                if (!attendanceSessionList.isEmpty()) {
                                    listViewSession.setAdapter(adapter);
                                    setCharData();
                                }
                                if(attendanceSessionList.size()<2) {
                                    cardViewSession.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        else if(storedAdmin.equals("1")){

            relativeLayoutHistory.setVisibility(View.GONE);

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("history",Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString("uid","0");

            FirebaseDatabase.getInstance().getReference("Sessions")
                    .child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            attendanceSessionList.clear();

                            for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                attendanceSessionList.add(attendanceSession);
                                float value = attendanceSession.getTotalTime();
                                value /= 60;
                                timeValues.add(value);
                            }
                            try {
                                HistoryList adapter = new HistoryList(getActivity(), attendanceSessionList);
                                if (!attendanceSessionList.isEmpty()) {
                                    listViewSession.setAdapter(adapter);
                                    setCharData();
                                }
                                if(attendanceSessionList.size()<2) {
                                    cardViewSession.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        //AttendanceSession attendanceSession = new AttendanceSession("No Session", "Slow connection?");
        //attendanceSessionList.add(attendanceSession);
        HistoryList adapter = new HistoryList(getActivity(), attendanceSessionList);
        listViewSession.setAdapter(adapter);
    }

    private void setCharData(){

        final List<BarEntry> yValues = new ArrayList<>();

        for(int i=0;i<timeValues.size();i++){
            yValues.add(new BarEntry(i+1,timeValues.get(i)));
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(yValues,"Total Time");
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        barDataSet.setGradientColor(lineColorBlue,lineColorPurple);
        //barDataSet.setColors(lineColorBlue,lineColorLightBlue,lineColorPurple,lineColorLightPurple);

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);
        barChart.setData(barData);
        barChart.invalidate();
        cardViewSession.setVisibility(View.VISIBLE);
    }
}
