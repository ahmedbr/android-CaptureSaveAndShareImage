package com.revoseed.app.testapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class SecondActivity extends AppCompatActivity {
    private static final String FILE_PROVIDER_AUTHORITY = "com.revoseed.app.testApp";
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        String path = intent.getStringExtra("SAVED_PATH");
        Uri photoURI = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, new File(path));
        imageView = findViewById(R.id.image_view2);
        imageView.setImageURI(photoURI);

    }

    public void openImageFolder(Context context, String tempPath) {
        File file = new File(tempPath);
        if (file.exists()) {
            Uri photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, new File(tempPath));
            Log.e("URI: ", photoURI.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(photoURI, "image/*");
            startActivity(intent);
        } else {
            Toast.makeText(context, "file is not exist ahmed", Toast.LENGTH_LONG).show();
        }
    }
}
