package com.example.takundachinyerere.tomatodetection;

import android.content.ContentResolver;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.takundachinyerere.tomatodetection.Model.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


public class DetectActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;

    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";
    private static final String MODEL_FILE = "file:///android_asset/retrained_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/retrained_labels.txt";


    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnTakePhoto, btnImportPhoto;
    private ImageView photoView, imageView, matchView, matchView2;
    private LinearLayout photoLayout, resultLayout, matchLayout;
    Intent CropIntent;
    Uri uri;

    Bitmap photoBitmap;

    public final static int REQUEST_CAMERA = 1;
    public final static int REQUEST_GALLERY = 2;

    private ProgressBar mProgressBar;
    private TextView mTextViewShowUploads;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        photoLayout = findViewById(R.id.linearLayoutPhoto);
        resultLayout = findViewById(R.id.linearLayoutResult);
        matchLayout = findViewById(R.id.linearLayoutMatch);
        photoView = findViewById(R.id.photoView);
        imageView = findViewById(R.id.imageView);
        matchView = findViewById(R.id.matchView);
        matchView2 = findViewById(R.id.matchView2);
        textViewResult = findViewById(R.id.textViewResult);
        btnImportPhoto = findViewById(R.id.btnImportPhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnDetectObject = findViewById(R.id.btnDetectObject);
        auth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progress_bar);
        /*mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);*/
        user = auth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference(user.getUid()+"/"+"uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(user.getUid()+"/"+"uploads");
        setGetPhotoView();

        addEvents();

        initTensorFlowAndLoadModel();
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
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
private void addEvents(){
        btnImportPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPictureFromGallery();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPictureFromCamera();
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                detectObject();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        setGetPhotoView();

        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_CAMERA:
                if(resultCode == Activity.RESULT_OK) {
                    photoBitmap = handlePhotoFromCamera(data);
                }
                break;
            case REQUEST_GALLERY:
                if(resultCode == Activity.RESULT_OK) {
                    uri = data.getData();
                    CropImage();
                    photoBitmap = handlePhotoFromGallery(data);

                }
                break;
        }



        if (photoBitmap != null) {
            photoView.setImageBitmap((photoBitmap));
            try {
                InputStream bitmap=getAssets().open("lb.JPG");
                Bitmap bit=BitmapFactory.decodeStream(bitmap);
                matchView2.setImageBitmap(bit);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.out.println("imageView is set with bitmap of taken photo");
        } else {
            System.out.println("Null Bitmap");
        }
    }

    /*public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] arr=baos.toByteArray();
        String result=Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
    }*/

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void CropImage() {

        try{
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");

            CropIntent.putExtra("crop","true");
            CropIntent.putExtra("outputX",180);
            CropIntent.putExtra("outputY",180);
            CropIntent.putExtra("aspectX",3);
            CropIntent.putExtra("aspectY",4);
            CropIntent.putExtra("scaleUpIfNeeded",true);
            CropIntent.putExtra("return-data",true);

            startActivityForResult(CropIntent,REQUEST_CAMERA);
        }
        catch (ActivityNotFoundException ex)
        {

        }

    }


    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = ImageClassifier.create(getAssets(), MODEL_FILE,
                            LABEL_FILE, INPUT_SIZE, INPUT_NAME, OUTPUT_NAME);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing Model!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getPictureFromGallery(){
        Intent galleryImportIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryImportIntent.setType("image/*");
        startActivityForResult(galleryImportIntent, REQUEST_GALLERY);
    }

    private void getPictureFromCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void detectObject(){

        setResultView();
        setMatchView2();

        try{
            BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
            BitmapDrawable drawable1 = (BitmapDrawable) matchView2.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            Bitmap bitmap1 = drawable1.getBitmap();
            //Bitmap bitmap1 = BitmapFactory.decodeFile(String.valueOf(R.drawable.lb));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
            byte[] data = baos.toByteArray();
            byte[] data1 = baos1.toByteArray();
            final String result=Base64.encodeToString(data, Base64.DEFAULT);
            final String result1=Base64.encodeToString(data1, Base64.DEFAULT);
            String text = "";
            final ArrayList<LabelProb> labelProbs = classifier.recognizeImage(bitmap);
            if (labelProbs == null) {
                text += ": ?\n";
            } else {
                for (int i = 0; i < 1; ++i) {
                    //text += String.format("%s (Similarity: %.2f) \n", labelProbs.get(i).getLabel(), labelProbs.get(i).getProb());
                    text += String.format("%.2f confident that this is %s \n",labelProbs.get(i).getProb(), labelProbs.get(i).getLabel());
                    if(uri != null){
                        StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(uri));

                        mUploadTask = fileReference.putFile(uri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgressBar.setProgress(0);
                                            }
                                        }, 500);

                                        Toast.makeText(DetectActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                                        String text1 = "";
                                        String yValue;
                                        text1 += String.format("%s", labelProbs.get(0).getLabel());
                                        yValue = String.format("%.2f", labelProbs.get(0).getProb());
                                        /*Upload upload = new Upload(text1.toString().trim(),
                                                taskSnapshot.getDownloadUrl().toString());*/
                                        long x=new Date().getTime();
                                        float y = Float.parseFloat(yValue);
                                        String y2Value, y3Value;
                                        y2Value = String.format("%s",labelProbs.get(0).getLabel());
                                        //y3Value = String.format("%s",labelProbs.get(0).getLabel());
                                        int y2 = 0;
                                        if (text1.toString().trim() == "lateblight" ){
                                            Toast.makeText(DetectActivity.this, "Apply a copper based fungicide every 7 days or less.", Toast.LENGTH_LONG).show();
                                        }
                                        String text2=result;
                                        String text3=result1;
                                        Upload upload = new Upload(text1.toString().trim(), taskSnapshot.getDownloadUrl().toString(),x,y,y2,text2.toString().trim(),text3.toString().trim());

                                        String uploadId = mDatabaseRef.push().getKey();
                                        mDatabaseRef.child(uploadId).setValue(upload);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DetectActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                        mProgressBar.setProgress((int) progress);
                                    }
                                });
                    } else {
                        Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            imageView.setImageBitmap((photoBitmap));
            matchView.setImageDrawable(getMatchedImage(labelProbs.get(0).getLabel()));
            textViewResult.setText(text);

        } catch (Exception e) {
            Log.i("TAG", "Some exception " + e);
            e.printStackTrace(System.out);
            textViewResult.setText(R.string.error_message);
        }

    }

    private Bitmap handlePhotoFromCamera(Intent data){
        Bitmap takenPictureData = (Bitmap)data.getExtras().get("data");
        return takenPictureData;
    }

    private Bitmap handlePhotoFromGallery(Intent data){
        Bitmap selectedImage = null;
        try {
            Uri imageUri = data.getData();
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            selectedImage = BitmapFactory.decodeStream(imageStream);
            System.out.println("Photo Imported from Gallery");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Photo Importing Failed");
        }
        return selectedImage;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setGetPhotoView() {
        photoLayout.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);
        textViewResult.setVisibility((View.GONE));
    }



    public void setResultView() {
        photoLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        textViewResult.setVisibility((View.VISIBLE));
    }

    public void setMatchView2() {
        matchView.setVisibility((View.GONE));
    }

    public Drawable getMatchedImage (String label) {
        switch (label) {
            case "healthy":
                return getDrawable(R.drawable.hl);
            case "lateblight":
                return getDrawable(R.drawable.lb);
        }
        return getDrawable(R.drawable.hl);
    }


}

