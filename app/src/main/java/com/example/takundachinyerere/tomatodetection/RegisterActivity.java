package com.example.takundachinyerere.tomatodetection;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class RegisterActivity extends AppCompatActivity {

    EditText e1,e2;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        e1 = (EditText)findViewById(R.id.editText);
        e2 = (EditText)findViewById(R.id.editText2);

        auth = FirebaseAuth.getInstance();
    }


    public void createUser(View v)
    {
        if(e1.getText().toString().equals("") && e2.getText().toString().equals(""))
        {
            Toast.makeText(getApplicationContext(),"Blanks Not Allowed",Toast.LENGTH_SHORT).show();
        }
         else
        {

            String email = e1.getText().toString();
            String password = e2.getText().toString();

           auth.createUserWithEmailAndPassword(email,password)
               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                                  if(task.isSuccessful())
                                  {
                                      Toast.makeText(getApplicationContext(),"User Created Successfully",Toast.LENGTH_SHORT).show();
                                      finish();
                                      Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
                                      startActivity(i);

                                  }
                                  else
                                  {
                                      Toast.makeText(getApplicationContext(),"User Could Not Be Created",Toast.LENGTH_SHORT).show();
                                  }
            }
        });
        }
    }
}
