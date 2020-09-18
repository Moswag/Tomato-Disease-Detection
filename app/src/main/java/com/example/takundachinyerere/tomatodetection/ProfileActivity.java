 package com.example.takundachinyerere.tomatodetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

 public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView profileText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();

        profileText = findViewById(R.id.textView);

        user  = auth.getCurrentUser();

        profileText.setText(user.getEmail());

        button = findViewById(R.id.button5);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectButton();
            }
        });

        button = findViewById(R.id.button6);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timelineButton();
            }
        });

        button = findViewById(R.id.button7);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagesActivity();
            }
        });

        button = findViewById(R.id.button8);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGraphActivity();
            }
        });

        button = findViewById(R.id.button9);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBarGraphActivity();
            }
        });

        button = findViewById(R.id.button10);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSimilarityActivity();
            }
        });

    }

     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.log_out:
                 auth.signOut();
                 finish();
                 Intent i = new Intent(this,LoginActivity.class);
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

     public void onGroupItemClick(MenuItem item) {
         // One of the group items (using the onClick attribute) was clicked
         // The item parameter passed here indicates which item it is
         // All other menu item clicks are handled by <code><a href="/reference/android/app/Activity.html#onOptionsItemSelected(android.view.MenuItem)">onOptionsItemSelected()</a></code>
     }


    public void signOut(View v)
    {
        auth.signOut();
        finish();
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);

    }
    private void detectButton()
    {
        Intent i = new Intent(this, DetectActivity.class);
        startActivity(i);
    }

     private void timelineButton()
     {
         Intent i = new Intent(this, LiveActivity.class);
         startActivity(i);
     }

    private void openImagesActivity()
    {
        Intent i = new Intent(this, ImagesActivity.class);
        startActivity(i);
    }

     private void openGraphActivity()
     {
         Intent i = new Intent(this, GraphActivity.class);
         startActivity(i);
     }

     private void openBarGraphActivity()
     {
         Intent i = new Intent(this, BarActivity.class);
         startActivity(i);
     }

     private void openSimilarityActivity()
     {
         Intent i = new Intent(this, SimilarityActivity.class);
         startActivity(i);
     }
}
