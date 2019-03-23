package edu.apsu.draw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private static final int IMAGE1 = 100;
    private static final int CAMERA_IMAGE = 101;
    int angle;

    Uri source;
    Bitmap bitmapReal;
    Canvas canvas;
    Paint paint;

    int xStep;
    int yStep;

    int defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.result);

        // set up the paint for intital drawing
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);

        defaultColor = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);


        findViewById(R.id.rotate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = angle - 90;
                imageView.setRotation(angle);
            }
        });

        // gets the touch input from the user
        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int eventAction = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();

                if (eventAction == MotionEvent.ACTION_DOWN) {
                    xStep = x;
                    yStep = y;
                    drawOnBitMap((ImageView) v, bitmapReal, xStep, yStep, x, y);
                } else if (eventAction == MotionEvent.ACTION_MOVE) {
                    drawOnBitMap((ImageView) v, bitmapReal, xStep, yStep, x, y);
                    xStep = x;
                    yStep = y;
                } else if (eventAction == MotionEvent.ACTION_UP) {
                    drawOnBitMap((ImageView) v, bitmapReal, xStep, yStep, x, y);
                }
                return true;
            }
        });
    }

    public void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                paint.setColor(defaultColor);
            }
        });
        colorPicker.show();
    }


    // get position of image so the bitmap can draw on it
    private void drawOnBitMap(ImageView imgV, Bitmap bm, float x1, float y1, float x, float y) {
        if (x < 0 || y < 0 || x > imgV.getWidth() || y > imgV.getHeight()) { // if it is outside of the image
            return;
        } else {

            float width = (float) bm.getWidth() / (float) imgV.getWidth();
            float height = (float) bm.getHeight() / (float) imgV.getHeight();

            canvas.drawLine(x1 * width, y1 * height, x * width, y * height, paint);
            imageView.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;

        if (resultCode == RESULT_OK) {


            if (requestCode == IMAGE1) {
                source = data.getData();
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(source));

                    Bitmap.Config config;
                    if (bitmap.getConfig() != null) {
                        config = bitmap.getConfig();

                    } else {
                        config = Bitmap.Config.ARGB_8888;
                    }

                    bitmapReal = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);

                    canvas = new Canvas(bitmapReal);
                    canvas.drawBitmap(bitmap, 0, 0, null);

                    imageView.setImageBitmap(bitmapReal);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // adds the menu to the activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // selects which menu option is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_photo) {
            openGallery();
        } else if (id == R.id.action_add_photo_from_camera) {
            //openCamera();
        }
         else if (id == R.id.action_change_color) {
             openColorPicker();
        }
        return super.onOptionsItemSelected(item);
    }

    // opens the gallery and allows the user to select a photo
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE1);
        TextView tv = findViewById(R.id.select_photo_tv);
        tv.setText("");
    }

    public void clear() {

    }
}











    /*
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_IMAGE);
        TextView tv = findViewById(R.id.select_photo_tv);
        tv.setText("");
    }
    */
























    /*
    // depending on which button the user presses either the camera will load or the gallery will load
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageURI = data.getData();
            imageView.setImageURI(imageURI);
        } else if (resultCode == RESULT_OK && requestCode == CAMERA_IMAGE) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
    }
    */







