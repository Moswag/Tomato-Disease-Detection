package com.example.takundachinyerere.tomatodetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.BarGraphSeries;


import java.text.SimpleDateFormat;
import java.util.Date;



public class BarActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;

    SimpleDateFormat sdf = new SimpleDateFormat("MMM");
    GraphView graphView;
    BarGraphSeries series;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar);

        graphView=(GraphView) findViewById(R.id.graphView);

        series = new BarGraphSeries();
        graphView.addSeries(series);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference=database.getReference(user.getUid()+"/"+"timeline");



//        setListeners();

        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);

        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {

                if (isValueX) {
                    return sdf.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }


        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        reference.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                DataPoint[] dp = new DataPoint[(int) dataSnapshot.getChildrenCount()];
                int index = 0;

                for(DataSnapshot myDataSnapshot : dataSnapshot.getChildren())
                {
                    Upload upload = myDataSnapshot.getValue(Upload.class);
                    /*String count = upload.getName();
                    if (count == "healthy"){

                    }*/
                    dp[index]=new DataPoint(upload.getxValue(),upload.getyValue());
                    index++;
                }
                graphView.getViewport().setYAxisBoundsManual(true);
                graphView.getViewport().setMinY(0);
                graphView.getViewport().setMaxY(1);
                graphView.getViewport().setScrollable(true);
                series.resetData(dp);
                graphView.setTitle("Monthly Analysis");
                graphView.getGridLabelRenderer().setVerticalAxisTitle("Total");
                graphView.getGridLabelRenderer().setHorizontalAxisTitle("Date");
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });
    }
}
