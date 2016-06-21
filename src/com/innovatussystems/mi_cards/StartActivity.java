package com.innovatussystems.mi_cards;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartActivity extends Activity implements SensorEventListener {

    private static final String TAG = "MFMSCam";
    public LocationResult locationResult;
    String Remarks = "", InitRemarks, address, cdt;
    Button start;
    EditText rem;
    String[] arr_text;
    String mins;
    String namestart;
    String name;
    String path1;
    //int icon = R.drawable.camicon;
    LocationListener listener;
    LocationManager locationManager;
    Location currentLocation;
    StringBuilder result;
    double latitude_main, longitude_main;
    Thread t;
    String host;
    View v1;
    TextView pastetxt;
    //EditText name_etxt,mobile_etxt,hint_etxt;
    String value, Greetingmessage, companyname, mobileno;
    String cardname, cardmobileno, cardnotes;
    AlertDialog.Builder alertDialog;
    GPSTracker gps;
    /* Camera Call backs */
    ShutterCallback shutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };
    /**
     * Handles data for raw picture
     */
    PictureCallback rawCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };
    Camera.PictureCallback mPictureCallbackRaw = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            // (...)
        }
    };
    Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            camera.takePicture(null, null, jpegCallback);
        }
    };
    private Camera mCamera;
    ErrorCallback mErrorCallback = new ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            Log.d("MiCam", "camera error detected");
            if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
                Log.d("CameraDemo", "attempting to reinstantiate new camera");
                camera.stopPreview();
                //camera.setPreviewCallback(null);
                mCamera.release(); // written in documentation...
                mCamera = null;
                mCamera = Camera.open();

            }
        }
    };
    private Preview mPreview;
    private SensorManager sensorManager = null;
    private int orientation;
    private ExifInterface exif;
    private int deviceHeight;
    private Button ibRetake;
    private Button ibUse;
    private Button ibCapture;
    AutoFocusCallback _pfnAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
//            camera.autoFocus(null);
//            camera.takePicture(shutterCallback, rawCallback,
//            		jpegCallback);
            ibCapture.setEnabled(true);

        }
    };
    private FrameLayout flBtnContainer;
    /**
     * Handles data for jpeg picture
     */
    PictureCallback jpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            flBtnContainer.setVisibility(View.GONE);
            //ibRetake.setVisibility(View.VISIBLE);
            ibUse.setVisibility(View.VISIBLE);
            final Dialog dialog1 = showProgressDialog(null, "Please Wait...\n");
            try {
                t = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        setErrorCallback(mErrorCallback);
                        // getmins();

                        Log.e("FileName", "MiCards_" + cdt + cdt);

                        Calendar c = Calendar.getInstance();
                        Long lDate = c.getTime().getTime();
                        FileOutputStream outStream = null;
                        try {
                            outStream = new FileOutputStream("sdcard/MiCards/" +
                                    "MiCards_" + cdt + ".jpg");

                            outStream.write(data);
                            outStream.flush();
                            outStream.close();
//						 sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

//					saveMediaEntry("/sdcard/MC_BE/" + "BE_"+cdt +".jpg", "BE_"+cdt , "BE_"+cdt , null,
//								lDate, 0, latitude_main, longitude_main);
                            Log.d(TAG, "onPictureTaken - wrote bytes: "
                                    + data.length);
                            //			Inspection.foto="BE_"+cdt + ".jpg";
                            String path = "sdcard/MiCards/" + "MiCards_" + cdt + ".jpg";
                            path1 = path;
                            stopPreviewAndFreeCamera();
                            Log.i("success", "Done");
                            //			sendEmail(flBtnContainer);


                        } catch (Exception e) {
                            Log.e(TAG, "Exception while writing image", e);
                            e.printStackTrace();
                        } finally {
                        }
                        dialog1.dismiss();
                    }
                });
                t.start();
            } catch (Exception ex) {
                camera.stopPreview();
                //camera.setPreviewCallback(null);
                mCamera.release(); // written in documentation...
                mCamera = null;
                mCamera = Camera.open();

            }
        }
    };
    private File sdRoot;
    private String dir;
    private String fileName;
    private ImageView rotatingImage;
    private int degrees = -1;
    private String IMEI;
    private String Model;
    private Mail m;
    private ShareActionProvider mShareActionProvider;
    private PictureCallback mPicture = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // Replacing the button after a photho was taken.
            flBtnContainer.setVisibility(View.GONE);
            //ibRetake.setVisibility(View.VISIBLE);
            ibUse.setVisibility(View.VISIBLE);
            // File name of the image that we just took.
            fileName = "MiCards_" + cdt + ".jpg";
            // Creating the directory where to save the image. Sadly in older
            // version of Android we can not get the Media catalog name
            File mkDir = new File(sdRoot, dir);
            mkDir.mkdirs();
            // Main file where to save the data that we recive from the camera
            File pictureFile = new File(sdRoot, dir + fileName);
            try {
                FileOutputStream purge = new FileOutputStream(pictureFile);
                purge.write(data);
                purge.close();
            } catch (FileNotFoundException e) {
                Log.d("DG_DEBUG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("DG_DEBUG", "Error accessing file: " + e.getMessage());
            }
            // Adding Exif data for the orientation. For some strange reason the
            // ExifInterface class takes a string instead of a file.
            try {
                exif = new ExifInterface("sdcard/" + dir + fileName);
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + orientation);
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("hmmm", e.toString());
            }
        }
    };

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        // returns null if camera is unavailable
        return c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        //name_etxt=(EditText) findViewById(R.id.name_etxt);
        //mobile_etxt=(EditText) findViewById(R.id.mobile_etxt);
        //hint_etxt=(EditText)findViewById(R.id.hint_etxt);
        //scan_btn=(Button) findViewById(R.id.scan_btn);
        Database db = new Database(getBaseContext());
        value = db.getEmail();
        System.out.println("^^^^^^^^^^^^^^^^");
        System.out.println(value);
        System.out.println("#################");
//		String to=db.getMail();
//		if(to.equals("")){

        m = new Mail("micards@innovatussystems.com", "India12#");

        //Getting setting values
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        Model = android.os.Build.MODEL;
        Log.e("Phone Model", Model);
//		host = "111.118.180.108";
/*		latitude_main=this.getIntent().getDoubleExtra("lat", latitude_main);
        longitude_main=this.getIntent().getDoubleExtra("lon", longitude_main);
		address=this.getIntent().getStringExtra("add");
	*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        cdt = currentDateandTime;
        // Setting all the path for the image
        //sdRoot = Environment.getExternalStorageDirectory();
        //sdRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //dir = "/sdcard/MFMSCam/";
        //dir = "/DCIM/Camera/";
        //Getting all the needed elements from the layout

        rotatingImage = (ImageView) findViewById(R.id.imageView1);
        //ibRetake = (Button) findViewById(R.id.ibRetake);
        ibUse = (Button) findViewById(R.id.ibUse);
        ibCapture = (Button) findViewById(R.id.ibCapture);
        flBtnContainer = (FrameLayout) findViewById(R.id.flBtnContainer);
        // Getting the sensor service.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Selecting the resolution of the Android device so we can create a
        // proportional preview
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        deviceHeight = display.getHeight();
        isExternalStoragePresent();
        // Add a listener to the Capture button
        display2();
        ibCapture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                takeFocusedPicture();

            }
        });

        // Add a listener to the Use button
        ibUse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(StartActivity.this);
                builder1.setMessage("Confirm Share ?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Share",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                reports();

                            }

                        });
                builder1.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //path1="";
                                //restartActivity();
                                //dialog.cancel();
                                restartActivity();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }

        });
        //StartActivity.this.finish();
    }

    private void sharing() {
        final Dialog dialog1 = showProgressDialog(null, "Loading Please wait...\n");
        try {
            t = new Thread(new Runnable() {

                @SuppressLint("NewApi")
                @Override
                public void run() {
                    try {

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.setType("image/*");
                        String shareBody = "A new business card has arrived as a scanned image from MiCards.\nOpen the attachment, get the details and add them to your Contacts database.\n \nNo more worries about misplaced business cards.\n \nMiCards is from Innovatus Systems (www.innovatussystems.com).\nFor more details about MiCards, please contact Innovatus Systems at \n info@innovatussystems.com";
                        display();
                        System.out.println("$$$$$$$$$$$$$");
                        System.out.println(value);
                        System.out.println("@@@@@@@@@@@@@");
                        String aEmailList1 = value;
                        System.out.println("!!!!!!!!!!!!!");
                        System.out.println(aEmailList1);
                        sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList1);
                        String card = "MiCards-Build your contacts the SMART way.";
                        String MiCards = "MiCards-";
                        display2();
                        if (cardname.equals("") && cardmobileno.equals("") && cardnotes.equals("")) {
                            System.out.println("nothing entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, card);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + shareBody + "\n");
                        } else if (cardname.equals("") && cardmobileno.equals("")) {
                            System.out.println("notes entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + " " + cardnotes);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + " " + " " + cardnotes + "\n \n" + shareBody + "\n");
                        } else if (cardmobileno.equals("") && cardnotes.equals("")) {
                            System.out.println("name entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardname);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardname + "\n \n" + shareBody + "\n");
                        } else if (cardname.equals("") && cardnotes.equals("")) {
                            System.out.println("phoneno entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardmobileno);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardmobileno + "\n \n" + shareBody + "\n");
                        } else if (cardnotes.equals("")) {
                            System.out.println("phoneno,name entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardname + "-" + cardmobileno);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardname + "-" + cardmobileno + "\n \n" + shareBody + "\n");
                        } else if (cardname.equals("")) {
                            System.out.println("phoneno,notes entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardmobileno + "-" + cardnotes);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardmobileno + "-" + cardnotes + "\n \n" + shareBody + "\n");
                        } else if (cardmobileno.equals("")) {
                            System.out.println("notes,name entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardname + "-" + cardnotes);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardname + "-" + cardnotes + "\n \n" + shareBody + "\n");
                        } else {
                            System.out.println("all entered");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MiCards + " " + cardname + "-" + cardmobileno + "-" + cardnotes);
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, card + "\n" + MiCards + " " + cardname + "-" + cardmobileno + "-" + cardnotes + "\n \n" + shareBody + "\n");
                        }
                        //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, myname+ " "+"business card" +" "+"Mobile number" +" "+phoneno +" "+"Identity" +" "+notes);
                        //sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/" + path1));
                        try {
                            startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(StartActivity.this, "No email client installed.",
                                    Toast.LENGTH_LONG).show();
                        }
                        //StartActivity.this.finish();
                        //Toast.makeText(StartActivity.this, "MiCards sent", Toast.LENGTH_LONG).show();
                        //Intent intent=new Intent(getApplicationContext(),StartActivity.class);

                    } catch (Exception e) {
                        Log.e(TAG, "Exception while uploading image", e);
                        e.printStackTrace();
                    } finally {
                    }
                    dialog1.dismiss();
                }
            });
            t.start();
        } catch (Exception ex) {

        }


//			sendEmail(v1);
//			//Intent intent=new Intent(getApplicationContext(),StartActivity.class);
        //Toast.makeText(StartActivity.this, "MiCards sent", Toast.LENGTH_LONG).show();
        //StartActivity.this.finish();
//			//startActivity(intent);

        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
        startActivity(intent);
        StartActivity.this.finish();

    }

    //	@SuppressLint("NewApi")
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    getMenuInflater().inflate(R.menu.action_bar_share_menu, menu);
//	    MenuItem item = menu.findItem(R.id.menu_item_share);
//
//	    ShareActionProvider myShareActionProvider = (ShareActionProvider) item.getActionProvider();
//
//	    Intent myIntent = new Intent();
//	    myIntent.setAction(Intent.ACTION_SEND);
//	    myIntent.putExtra(Intent.EXTRA_TEXT, "Whatever message you want to share");
//	    myIntent.setType("text/plain");
//
//	    myShareActionProvider.setShareIntent(myIntent);
//
//	    return true;
//	}
    public void gpsaction_offline() {
        gps = new GPSTracker(StartActivity.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude_main = gps.getLatitude();
            longitude_main = gps.getLongitude();
            //makeAlertDialog("Lat="+latitude_main+"lon="+longitude_main);
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                address = getAddress(latitude_main, longitude_main);
                //System.out.println("Current Address is"+address);
            }
            locationResult = new LocationResult() {
                public void gotLocation(Location location) {
                    if (location != null) {
                        getSharedPreferences("LatLong", Context.MODE_PRIVATE)
                                .edit().putString("Lat", location.getLatitude() + "").putString(
                                "Long", location.getLongitude() + "");
                        latitude_main = location.getLatitude();
                        longitude_main = location.getLongitude();
                        Log.i("Address gathered", latitude_main + " : "
                                + longitude_main);
                        //address = getAddress(latitude_main, longitude_main);

                    }
                }
            };

            MyLocation myLocation = new MyLocation();
            myLocation.getLocation(StartActivity.this, locationResult);
            System.out.println("Current Address is" + address);
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude_main + "\nLong: " + longitude_main, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void createCamera() {
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Setting the right parameters in the camera
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width > size.width)
                size = sizes.get(i);
        }

        params.setPictureSize(size.width, size.height);
        //params.setPictureSize(1280, 960);
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setPictureFormat(PixelFormat.JPEG);
        params.setFlashMode(Parameters.FLASH_MODE_AUTO);
        params.setJpegQuality(100);
        mCamera.setParameters(params);

//		PackageManager pm =getPackageManager();
//		if(pm.hasSystemFeature
//				(PackageManager.FEATURE_CAMERA
//				) && pm.hasSystemFeature
//				(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
//				       // True means the camera has autofocus mode on.Do what ever you want to do
//				}
        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        // Calculating the width of the preview so it is proportional.
        float widthFloat = (float) (deviceHeight) * 4 / 3;
        int width = Math.round(widthFloat);

        // Resizing the LinearLayout so we can make a proportional preview. This
        // approach is not 100% perfect because on devices with a really small
        // screen the the image will still be distorted - there is place for
        // improvement.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, deviceHeight);
        preview.setLayoutParams(layoutParams);

        // Adding the camera preview after the FrameLayout and before the button
        // as a separated element.
        preview.addView(mPreview, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Test if there is a camera on the device and if the SD card is
        // mounted.
/*		if (!checkCameraHardware(this)) {
            Intent i = new Intent(this, NoCamera.class);
			startActivity(i);
			finish();
	}*/
        // Creating the camera
        createCamera();

        // Register this class as a listener for the accelerometer sensor
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // release the camera immediately on pause event
        releaseCamera();

        // removing the inserted view - so when we come back to the app we
        // won't have the views on top of each other.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeViewAt(0);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private boolean checkSDCard() {
        boolean state = false;
        String sd = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sd)) {
            state = true;
        }
        return state;
    }

    /**
     * Putting in place a listener so we can get the sensor data only when
     * something changes.
     */
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                RotateAnimation animation = null;
                if (event.values[0] < 4 && event.values[0] > -4) {
                    if (event.values[1] > 0 && orientation != ExifInterface.ORIENTATION_ROTATE_90) {
                        // UP
                        orientation = ExifInterface.ORIENTATION_ROTATE_90;
                        animation = getRotateAnimation(270);
                        degrees = 270;
                    } else if (event.values[1] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
                        // UP SIDE DOWN
                        orientation = ExifInterface.ORIENTATION_ROTATE_270;
                        animation = getRotateAnimation(90);
                        degrees = 90;
                    }
                } else if (event.values[1] < 4 && event.values[1] > -4) {
                    if (event.values[0] > 0 && orientation != ExifInterface.ORIENTATION_NORMAL) {
                        // LEFT
                        orientation = ExifInterface.ORIENTATION_NORMAL;
                        animation = getRotateAnimation(0);
                        degrees = 0;
                    } else if (event.values[0] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_180) {
                        // RIGHT
                        orientation = ExifInterface.ORIENTATION_ROTATE_180;
                        animation = getRotateAnimation(180);
                        degrees = 180;
                    }
                }
                if (animation != null) {
                    rotatingImage.startAnimation(animation);
                }
            }
        }
    }

    /**
     * Calculating the degrees needed to rotate the image imposed on the button
     * so it is always facing the user in the right direction
     *
     * @param toDegrees
     * @return
     */
    private RotateAnimation getRotateAnimation(float toDegrees) {
        float compensation = 0;

        if (Math.abs(degrees - toDegrees) > 180) {
            compensation = 360;
        }
        // When the device is being held on the left side (default position for
        // a camera) we need to add, not subtract from the toDegrees.
        if (toDegrees == 0) {
            compensation = -compensation;
        }
        // Creating the animation and the RELATIVE_TO_SELF means that he image
        // will rotate on it center instead of a corner.
        RotateAnimation animation = new RotateAnimation(degrees, toDegrees - compensation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // Adding the time needed to rotate the image
        animation.setDuration(250);
        // Set the animation to stop after reaching the desired position. With
        // out this it would return to the original state.
        animation.setFillAfter(true);

        return animation;
    }

    /**
     * STUFF THAT WE DON'T NEED BUT MUST BE HEAR FOR THE COMPILER TO BE HAPPY.
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void showAlertDialog(final CharSequence message) {
        StartActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        StartActivity.this);
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                    }
                                });
                builder.create().show();
            }
        });
    }

    public Dialog showProgressDialog(String title, String message) {
        final ProgressDialog dialog = ProgressDialog.show(this, title, message);
        return dialog;
    }

    public final void setErrorCallback(ErrorCallback cb) {
        mErrorCallback = cb;
    }

    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    Uri saveMediaEntry(String imagePath, String displayName, String title,
                       String description, long dateTaken, int orientation, double lat,
                       double lon) {
        ContentValues v = new ContentValues();
        v.put(MediaColumns.TITLE, title);
        v.put(MediaColumns.DISPLAY_NAME, displayName);
        v.put(ImageColumns.DESCRIPTION, description);
        v.put(MediaColumns.DATE_ADDED, dateTaken);
        v.put(ImageColumns.DATE_TAKEN, dateTaken);
        v.put(MediaColumns.DATE_MODIFIED, dateTaken);
        v.put(MediaColumns.MIME_TYPE, "image/jpeg");
        v.put(ImageColumns.ORIENTATION, orientation);

        File f = new File(imagePath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
        v.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
        v.put(MediaColumns.SIZE, f.length());
        v.put(ImageColumns.LATITUDE, lat);
        v.put(ImageColumns.LONGITUDE, lon);
        f = null;

        v.put("_data", imagePath);
        ContentResolver c = getContentResolver();
        return c.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
    }

    private void isExternalStoragePresent() {

        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();
        String path = Environment.getExternalStorageDirectory().getPath()
                .toString();
        Log.e("SD Path", path);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            File wallpaperDirectory = new File("sdcard/MiCards/");
            // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if (!((mExternalStorageAvailable) && (mExternalStorageWriteable))) {
            Toast.makeText(getBaseContext(),
                    "SD card not present.Service Stopped", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void takePicsPeriodically_offline(final double lat, final double lon) {
        //preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//		Toast.makeText(
//				getApplicationContext(),
//				"Your Current Location is - \nLat: " + latitude_main
//						+ "\nLong: " + longitude_main, Toast.LENGTH_LONG)
//				.show();
        //insertJob(90, Remarks, lat, lon, address);
    }

    public String getPic() {
        SharedPreferences sp = getSharedPreferences("PICNAME", Context.MODE_PRIVATE);
        String s = sp.getString("PICNAME", "");
        return s;
    }

    public void sendEmail(View view) {

        String to = "sriram@innovatussystems.com";
        Database db = new Database(getBaseContext());
        display();
        String too = value;
        String[] toArr = {too}; // This is an array, you can add more emails, just separate them with a coma
        m.setTo(toArr); // load array to setTo function
        m.setFrom("micards@innovatussystems.com"); // who is sending the email
        m.setSubject("MiCards-A new business card");
        m.setBody("A new business card has arrived as a scanned image from MiCards.\n Open the attachment, get the details and add them to your Contacts database.\n \n No more worries about misplaced business cards.\n \n MiCards is from Innovatus Systems (www.innovatussystems.com).\n For more details about MiCards, please contact Innovatus Systems at \n info@innovatussystems.com");
        try {

            System.out.println("DONE HERE");
            m.addAttachment(path1);
            System.out.println(path1);// path to file you want to attach
            if (m.send()) {
                // success
                System.out.println("++++++++++++++++");

                Toast.makeText(StartActivity.this, "Micards Image Sent", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(intent);
                finish();
                //StartActivity.this.finish();

            } else {
                // failure
                Toast.makeText(StartActivity.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                finish();

            }
        } catch (Exception e) {
            // some other problem
            Log.e(TAG, "Mail error", e);
            System.out.println("EXCEPTION CAUGHT");
            Toast.makeText(StartActivity.this, "Check Your Internet Connectivity", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent);
            finish();
            //StartActivity.this.finish();
        }

    }

    public void display() {

        try {
//
            Database db = new Database(StartActivity.this);

            Cursor c = db.getMail();
            if (c.getCount() > 0) {
                for (int i = 0; i < c.getCount(); i++) {
                    for (int j = 0; j < c.getColumnCount(); j++) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                String _id = c.getString(0);
                                System.out.println("id" + _id);
                                value = c.getString(1);
                                System.out.println("email id" + value);
                                Greetingmessage = c.getString(2);
                                System.out.println("Greeting Message is" + Greetingmessage);
                                companyname = c.getString(3);
                                System.out.println("Company name is" + companyname);
                                mobileno = c.getString(4);
                                System.out.println("Mobile no is" + mobileno);
                            }
                        }
                    }
                }
            }
            db.close();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("bye");
        }
    }

    public void takeFocusedPicture() {
        mCamera.autoFocus(mAutoFocusCallback);
    }

    private String getAddress(double lat, double lon) {
        Log.e("Address Called", "Address Called");
        try {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<android.location.Address> addresses = gcd.getFromLocation(lat,
                    lon, 1);
            if (addresses.size() > 0) {
                android.location.Address address1 = addresses.get(0);

                StringBuilder result = new StringBuilder();
                for (int i = 0; i < addresses.size(); i++) {
                    android.location.Address address = addresses.get(i);

                    int maxIndex = address.getMaxAddressLineIndex();
                    for (int x = 0; x <= maxIndex; x++) {
                        result.append(address.getAddressLine(x));
                        if (x <= maxIndex - 1)
                            result.append(",");
                        else
                            result.append(".");
                    }
                    return result.append("\n\n").toString();
                }

            } else {
                // makeToast("Con't collect address" + "Con't collect");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // makeToast("Exception occured " + ex.getMessage());
        }
        return "-";
    }

    private void restartActivity() {
        Intent i = new Intent(getBaseContext(), StartActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    //	public void insertcontactdata()
//	{
//		Database dbhelper=new Database(StartActivity.this);
//		dbhelper.value_insert_contacttable(cardname, cardmobileno, cardnotes);
//		System.out.println("One Record inserted into contacttable");
//		System.out.println("^^^^^^^^^^^^^^^");
//		System.out.println(cardname);
//		System.out.println(cardmobileno);
//		System.out.println(cardnotes);
////		Intent sendIntent = new Intent(Intent.ACTION_SEND);
////		String smail1= "myname \t phoneno \t \t notes";
////		String smail2= new String();
////		for (int number = 0; number < smail2.length(); number++) { 
////			
////			smail2=cardname+"\t"+cardmobileno+"\t"+cardnotes;
////	    }
////        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "MiCards-Contact Database"); 
////        sendIntent.putExtra(Intent.EXTRA_TEXT,smail1+"\n"+smail2); 
////        Uri uri = Uri.fromFile(getDatabasePath("mycards"));
////        //uri = file:///data/data/com.gmailspike/databases/TEST_DB
////        //sendIntent.putExtra(Intent.EXTRA_STREAM, uri); 
////        sendIntent.setType("application/octet-stream");
////
////        startActivity(Intent.createChooser(sendIntent,"Email:"));
//	}
    public void reports() {
        LayoutInflater factory1 = LayoutInflater.from(this);
        final View textEntryView1 = factory1.inflate(R.layout.paste,
                null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartActivity.this)
                .setIcon(R.drawable.ic_menu_compass)
                .setTitle("Message")
                .setView(textEntryView1)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                //Email_setting.this.finish();
                                sharing();
                            }
                        });
        alertDialog.show();
    }

    public void display2() {

        try {
//				
            Database db = new Database(StartActivity.this);

            Cursor c = db.getContact();
            if (c.getCount() > 0) {
                for (int i = 0; i < c.getCount(); i++) {
                    for (int j = 0; j < c.getColumnCount(); j++) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                cardname = c.getString(0);
                                System.out.println("cardname" + cardname);
                                cardmobileno = c.getString(1);
                                System.out.println("cardmobileno is" + cardmobileno);
                                cardnotes = c.getString(2);
                                System.out.println("cardnotes is" + cardnotes);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("bye");
        }
    }
}
