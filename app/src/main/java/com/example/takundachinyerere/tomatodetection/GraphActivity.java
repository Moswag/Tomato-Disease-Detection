package com.example.takundachinyerere.tomatodetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {
    /*EditText yValue;
    Button btn_insert;*/
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;

    SimpleDateFormat sdf = new SimpleDateFormat("d:MMM:yy");
    GraphView graphView;
    LineGraphSeries series;
    PointsGraphSeries series1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        /*yValue=(EditText) findViewById(R.id.y_value);
        btn_insert=(Button) findViewById(R.id.btn_insert);*/

        graphView=(GraphView) findViewById(R.id.graphView);

        series = new LineGraphSeries();
        series1 = new PointsGraphSeries();
        graphView.addSeries(series);
        graphView.addSeries(series1);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                auth.signOut();
                finish();
                Intent i = new Intent(this,MainActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*private void setListeners(){
        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = reference.push().getKey();
                long x=new Date().getTime();
                int y = Integer.parseInt(yValue.getText().toString());

                PointValue pointValue = new PointValue(x,y);
                reference.child(id).setValue(pointValue);
            }
        });
    }*/

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
                    dp[index]=new DataPoint(upload.getxValue(),upload.gety2Value());
                    index++;
                }

                graphView.getViewport().setYAxisBoundsManual(true);
                graphView.getViewport().setMinY(-2);
                graphView.getViewport().setMaxY(2);

                // enable scrolling
                graphView.getViewport().setScrollable(true);
                series.resetData(dp);
                series1.resetData(dp);
                graphView.setTitle("Prediction");
                graphView.getGridLabelRenderer().setVerticalAxisTitle("Probability Axis");
                graphView.getGridLabelRenderer().setHorizontalAxisTitle("Date Axis");
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });
    }
}
