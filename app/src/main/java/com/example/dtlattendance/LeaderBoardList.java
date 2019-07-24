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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.common.util.Strings;
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
        ImageView onlineCircle = listViewItem.findViewById(R.id.onlineCircle);


        User user = leaderBoardList.get(position);
        textViewUserName.setText(user.getUsername());

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MaxTotal", Context.MODE_PRIVATE);
        Integer max = sharedPreferences.getInt("max",1);


        //Getting total time as integer
        Integer totalTime = Integer.valueOf(user.getTotal());
        Float floatTime = Float.valueOf(totalTime);

        if(floatTime==-1){
            textViewRank.setText("");
            textViewScore.setText("");
        }else {
            Float floatmax = Float.valueOf(max);
            Float score = floatTime / max * 90+(float)Math.log10(totalTime);
            if (score<0)
                score=(float)0;
            Integer integerScore = Math.round(score);
            textViewRank.setText(String.valueOf(position+1));

            //Custom Progress Bar
            progressBarCustom.setMax(100);

            if(false) {
                progressBarCustom.setProgress(23);
                progressBarCustom.setSecondaryProgress(100);
            }
            else{
                progressBarCustom.setProgress(score+5);
                progressBarCustom.setSecondaryProgress(score+10);
            }
            textViewScore.setText(""+integerScore);
        }

        //Setting user online
        if(user.getOnline().equals("1"))
            onlineCircle.setVisibility(View.VISIBLE);

        return listViewItem;
    }

}
