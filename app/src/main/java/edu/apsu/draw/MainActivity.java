package edu.apsu.draw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
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

    private static final int MY_PERMISSION_REQUIST = 1;
    ImageView imageView, imageFilter;
    Button filter_button;
    String currentImage = "";
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
        setContentView(R.layout.activity_main2);

        //ask for storage permission:
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUIST);

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUIST);
            }
        } else {
            // do nothing
        }

        imageView = findViewById(R.id.result);
        imageFilter = findViewById(R.id.filter);
        filter_button = findViewById(R.id.apply_button);
        filter_button.setEnabled(false);



        setUpPaint();

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
                View content = findViewById(R.id.lay);
                Bitmap bitmap = getScreenShot(content);
                currentImage = "image" + System.currentTimeMillis() + ".png";
                store(bitmap, currentImage);
            }

        });

        filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                imageFilter.setImageResource(R.drawable.filter);
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

    private static Bitmap getScreenShot(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    // allows the user to change the pen color
    // Gto fromitneem
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
        } else if (id == R.id.action_draw_rectangle) {
             drawRectangle(imageView,bitmapReal,xStep,yStep,xStep,yStep);
        }
        /**else if (id == R.id.action_add_text) {
          addText();
        }**/
        return super.onOptionsItemSelected(item);
    }

    private void drawRectangle(ImageView imgV, Bitmap bm, float x1, float y1, float x, float y) {

        // Load image into(as) bitmap
        paint.setAntiAlias(true);
        // Fill with color
        paint.setStyle(Paint.Style.FILL);
        // Set fill color
        paint.setColor(Color.BLUE);

        // Create Temp bitmap
        Bitmap tBitmap = Bitmap.createBitmap(bitmapReal.getWidth(), bitmapReal.getHeight(), Bitmap.Config.RGB_565);
        // Create a new canvas and add Bitmap into it
        Canvas tCanvas = new Canvas(tBitmap);
        //Draw the image bitmap into the canvas
        tCanvas.drawBitmap(bitmapReal, 0, 0, null);
        // Draw a rectangle over canvas
        tCanvas.drawRoundRect(new RectF(0,0,200,100), 2, 2, paint);
        // Add canvas into ImageView
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tBitmap));

    }

    /** private void addText() {
        EditText et = new EditText(getApplicationContext());

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "NicRegular.ttf");

        et.setTypeface(custom_font);
    }**/

    // set up the paint for initial drawing
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
        filter_button.setEnabled(true);
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
                            imageFilter.setImageDrawable(null);
                            filter_button.setEnabled(false);
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

    // saving the image in internal storage:
    private void store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/FILTEREDIMAGES";
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmapReal.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "saved!", Toast.LENGTH_LONG).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}








