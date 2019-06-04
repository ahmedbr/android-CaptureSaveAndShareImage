package com.revoseed.app.testapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.revoseed.app.testApp";
    String tempPhotoPath;
    Button captureButton;
    ImageView imageView;
    Bitmap bitmapImage;
    String savedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        imageView = findViewById(R.id.image_view);
        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
    }

    public void requestPermission() {

        // if the permission is NOT granted.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // then request run-time permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
        // else if it's granted
        else {
            // do what you want to do
            // launch camera only when the permission is granted
            launchCamera();
        }
    }

    public void launchCamera() {
        // initialize intent to capture an image
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // ensure the existence of camera activity to handle the intent
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                // create temporary file for the taken picture
                photoFile = createTempImageFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // proceed only if the photo file is not null
            if (photoFile != null) {
                // get the path of the temporary file
                tempPhotoPath = photoFile.getAbsolutePath();

                // get the content Uri for the image file
                Uri photoFileUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // adding the uri to the intent to enable camera to store the image
                takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);

                // launch the camera activity
                startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // if the image was captured successfully
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // do what you want with the image
            /*
                get the bitmap image from temporary captured image after resampling it
                to fit the screen, then set that bitmap image file to an imageView
            */
            bitmapImage = resamplePic(this, tempPhotoPath);
            imageView.setImageBitmap(bitmapImage);

        } else {
            // otherwise, delete the temp image file
            deleteTempImageFile(this, tempPhotoPath);

        }
    }

    public String saveImage(Context context, Bitmap image) {

        String savedPath = null;

        // Create the new file in the external storage
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        String pathName = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/testApp";
        File directory = new File(pathName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File imageFile = new File(directory, imageFileName);
        if (!imageFile.exists()) {
            try {
                imageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        savedPath = imageFile.getAbsolutePath();
        try {
            OutputStream fOut = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EXCEP ", e.getMessage());
        }

        // Show a Toast with the save location
        String savedMessage = context.getString(R.string.saved_message, savedPath);
        Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show();

        return savedPath;
    }

    void shareImage(Context context, String imagePath) {
        // Create the share intent and start the share activity
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        context.startActivity(shareIntent);
    }

    static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    void deleteTempImageFile(Context context, String imagePath) {
        // Get the file
        File imageFile = new File(imagePath);

        // Delete the image
        boolean deleted = imageFile.delete();

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            Toast.makeText(context, "Image deletion failed!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Temp image file deleted successfully", Toast.LENGTH_SHORT).show();
        }

    }

    static Bitmap resamplePic(Context context, String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_image:
                // save the image
                savedImagePath = saveImage(this, bitmapImage);
                Toast.makeText(this, savedImagePath, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("SAVED_PATH", savedImagePath);
                startActivity(intent);

                break;
            case R.id.delete_image:
                deleteTempImageFile(this, tempPhotoPath);
                imageView.setImageBitmap(null);
                break;
            case R.id.share_image:
                shareImage(this, savedImagePath);
                break;
        }
        return true;
    }
}
