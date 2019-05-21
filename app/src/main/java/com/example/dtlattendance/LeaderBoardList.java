package com.example.dtlattendance;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LeaderBoardList extends ArrayAdapter<User> {

    private Activity context;
    private List<User> leaderBoardList;

    public LeaderBoardList(Activity context,List<User> leaderBoardList){
        super(context,R.layout.list_leaderboard,leaderBoardList);
        this.context = context;
        this.leaderBoardList = leaderBoardList;
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_leaderboard,null,true);

        TextView textViewRank = listViewItem.findViewById(R.id.textViewRank);
        TextView textViewUserName = listViewItem.findViewById(R.id.textViewUserName);
        TextView textViewScore = listViewItem.findViewById(R.id.textViewScore);
        RoundCornerProgressBar progressBarCustom = listViewItem.findViewById(R.id.progressBarCustom);

        User user = leaderBoardList.get(position);
        textViewRank.setText(String.valueOf(position+1));
        textViewUserName.setText(user.getUsername());

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MaxTotal", Context.MODE_PRIVATE);
        Integer max = sharedPreferences.getInt("max",1);

        //Getting total time as integer
        Integer totalTime = Integer.valueOf(user.getTotal());
        Float floatTime = Float.valueOf(totalTime);
        Float floatmax = Float.valueOf(max);
        Float score = floatTime/max*90;
        Integer integerScore = Math.round(score);


        //Custom Progress Bar
        progressBarCustom.setMax(100);

        if(score<35) {
            progressBarCustom.setProgress(40);
            progressBarCustom.setSecondaryProgress(42);
        }
        else{
            progressBarCustom.setProgress(score);
            progressBarCustom.setSecondaryProgress(score+2);
        }
        textViewScore.setText(""+integerScore);
        return listViewItem;
    }
}
