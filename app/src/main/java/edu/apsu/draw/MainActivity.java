package edu.apsu.draw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private static final int IMAGE1 = 100;
    int angle;

    Uri source;
    Bitmap bitmapReal;
    Canvas canvas;
    Paint paint;

    int xStep;
    int yStep;

    int defaultColor;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.result);

        setUpPaint();
        //defaultColor = ContextCompat.getColor(MainActivity.this, R.color.colorPrimary);


        findViewById(R.id.rotate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle = angle - 90;
                imageView.setRotation(angle);
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
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

    // allows the user to change the pen color
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
        }
         else if (id == R.id.action_change_color) {
             openColorPicker();
        } else if (id == R.id.action_clear_canvas) {
             clearCanvas();
        } else if (id == R.id.action_change_brush_size) {
             changeBrushSize();

        }
        return super.onOptionsItemSelected(item);
    }

    // set up the paint for intital drawing
    public void setUpPaint() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);

    }

    // opens the gallery and allows the user to select a photo
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE1);
        TextView tv = findViewById(R.id.select_photo_tv);
        tv.setText("");
    }

    // allows user to change brush size
    public void changeBrushSize() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        alert.setTitle("Enter the size you want: ");

        alert.setView(edittext);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String youEditTextValue = edittext.getText().toString();
                int result = Integer.parseInt(youEditTextValue);
                paint.setStrokeWidth(result);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    // clears the canvas
    public void clearCanvas() {
        if (imageView.getDrawable() != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure, you want to delete this picture?");
            alertDialogBuilder.setPositiveButton("yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            bitmapReal.eraseColor(Color.TRANSPARENT);
                            imageView.setImageBitmap(bitmapReal);
                            imageView.invalidate();
                            TextView tv = findViewById(R.id.select_photo_tv);
                            tv.setText("Select the menu to add a photo to start drawing!");
                        }
                    });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}








