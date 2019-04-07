/*
* Description: Clear the canvas - this feature allows the user to completely start
 over and clears the canvas so the user can start a new drawing.

* Variables/Methods: The method uses is called clearCanvas(). This method will
 show an alert dialog asking the user if they really want to delete their masterpiece.
 If they select yes then the function clearCanvasHelper() is called and it uses
 bitmaps eraseColor function is called setting the
color to transparent. Then the imageview is set to the cleared bitmap.
 */


/*
* Description: Rotate photo - this feature allows the user to rotate the photo 90
 degrees each time the button is pressed.

* Variables/Methods: The onClickListener is set up with the button so that when it
 is pressed a variable called angle will be updated each time subtracting
 90 from it. Then the imageviews setRotation() method is called passing the
 angle in for the parameter.
 */

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
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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
    private static final int IMAGE1 = 100;
    ImageView imageView, imageFilter;
    EditText editText;
    Button filter_button;
    String currentImage = "";
    int angle;

    Uri source;
    Bitmap bitmapReal;
    Canvas canvas;
    Paint paint;

    int xStep, yStep;

    int defaultColor;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        askForPermission();


        imageView = findViewById(R.id.result);
        imageFilter = findViewById(R.id.filter);
        editText = findViewById(R.id.editText);
        editText.setVisibility(View.INVISIBLE);
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
                savePhoto();
            }

        });

        filter_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                imageFilter.setImageResource(R.drawable.filter2);
                imageView.invalidate();
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

    // asks the user for permissions to use the gallery
    private void askForPermission() {
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
        }
    }

    // creates a screenshot of the bitmap
    private static Bitmap getScreenShot(View view){
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    // allows the user to change the pen color
    // Got from https://github.com/yukuku/ambilwarna
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

        switch (id) {
            case R.id.action_add_photo:
                openGallery();
                break;
            case R.id.action_change_color:
                openColorPicker();
                break;
            case R.id.action_clear_canvas:
                clearCanvas();
                break;
            case R.id.action_change_brush_size:
                changeBrushSize();
                break;
            case R.id.action_draw_rectangle:
                drawRectangle();
                break;
            case R.id.action_add_text:
                addText();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // draws a rectangle on the screen
    private void drawRectangle() {

        if (isPictureLoaded()) {
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLUE);

            Bitmap tBitmap = Bitmap.createBitmap(bitmapReal.getWidth(), bitmapReal.getHeight(), Bitmap.Config.RGB_565);

            Canvas tCanvas = new Canvas(tBitmap);

            tCanvas.drawBitmap(bitmapReal, 0, 0, null);

            tCanvas.drawRect(100, 100, 600, 600, paint);
   
            imageView.setImageDrawable(new BitmapDrawable(getResources(), tBitmap));
            imageView.invalidate();
        }
        else {
            Toast.makeText(this, "Please load a photo first!", Toast.LENGTH_SHORT).show();
        }

    }

    // allows the user to add custom fonts to the photo.
    private void addText() {

        if (isPictureLoaded()) {
            editText.setVisibility(View.VISIBLE);

            Typeface custom_font = Typeface.createFromAsset(getAssets(), "NicRegular.ttf");

            editText.setTypeface(custom_font);
            imageView.invalidate();
        } else {
            Toast.makeText(this, "Please load a photo first!", Toast.LENGTH_SHORT).show();
        }
    }

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
                            clearCanvasHelper();
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

    // helper method for clear canvas
    public void clearCanvasHelper() {
        bitmapReal.eraseColor(Color.TRANSPARENT);
        imageView.setImageBitmap(bitmapReal);
        imageView.invalidate();
        imageFilter.setImageDrawable(null);
        filter_button.setEnabled(false);
        editText.setText("");
        editText.setVisibility(View.INVISIBLE);
        TextView tv = findViewById(R.id.select_photo_tv);
        tv.setText("Select the menu to add a photo to start drawing!");
    }

    // returns true if the picture has not been loaded
    private boolean isPictureLoaded() {
        if (imageView.getDrawable() != null)
            return true;
        else
            return false;
    }

    // saves the photo
    private void savePhoto() {
        if (isPictureLoaded()) {
            View content = findViewById(R.id.lay);
            bitmapReal = getScreenShot(content);
            currentImage = "image" + System.currentTimeMillis() + ".png";
            store(bitmapReal, currentImage);
        }
        else {
            Toast.makeText(this, "Please load a photo first!", Toast.LENGTH_SHORT).show();
        }
    }

    // stores the image in internal storage:
    private void store(Bitmap bm, String fileName){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/FILTEREDIMAGES";
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);
        Log.i("WHERE", file.getAbsolutePath());
        try{
            FileOutputStream fos = new FileOutputStream(file);
            bitmapReal.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "saved!", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}






/*

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
        imageView.invalidate();

    }
 */







