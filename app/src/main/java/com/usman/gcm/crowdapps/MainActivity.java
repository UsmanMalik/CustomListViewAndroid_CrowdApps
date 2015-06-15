package com.usman.gcm.crowdapps;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends ActionBarActivity {
    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String BaseUrl = "http://192.168.1.4:3000/";
    private static final String url = BaseUrl+"api/category/complete_data";
    private ProgressDialog pDialog;
    private List<Movie> movieList = new ArrayList<Movie>(); // REMOVE
    private List<Category> categoryList = new ArrayList<Category>();
    private ListView listView;
    private CustomListAdapter adapter;
    //private Bitmap bm;



    ////// GCM

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "nice-mechanism-87213";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "434617070081";

    /**
     * Tag used on log messages.
     */

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;
    ///// Database test /////

    final DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Loading...");
        pDialog.show();

        // Create folder

        File direct = new File(Environment.getExternalStorageDirectory()
                + "/CrowdApps");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        // Register GCM

        context = getApplicationContext();


        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            Log.e("Reg ID: " , regid + "");

            if (regid.isEmpty()) {
                registerInBackground();

            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");

        }

        int countCategory = db.getCategoryCount();


        Log.e("count category: ",countCategory +"");

        if(countCategory == 0){
            Log.e("Count Zero","Fetch data from server");

            getDataFromServer();
            pDialog.dismiss(); // LOCATE its position
        }else{
            categoryList = db.getAllCategories();
            Log.e("Count IS NOT Zero","Get DB");
            for (Category cn : categoryList) {
                String log = " Title: " + cn.getTitle() + " ,Phone: " + cn.getImage_path();
                // Writing Contacts to log
                Log.d("Name: ", log);
                pDialog.dismiss();
            }

        }


        /**
         * CRUD Operations
         * */
        // Inserting Contacts
     /*   Log.d("Insert: ", "Inserting ..");
        db.addContact(new Movie(1,"Ravi", "9100000000"));
        db.addContact(new Movie(2,"Srinivas", "9199999999"));
        db.addContact(new Movie(3,"Tommy", "9522222222"));
        db.addContact(new Movie(4,"Karthik", "9533333333"));

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<Movie> contacts = db.getAllContacts();
        List<Box> boxes = db.getAllBoxes();

        int count = db.getContactsCount();
        Log.e("Count: ", count+"");

        int countCat = db.getCategoryCount();
        Log.e("countCat: ", countCat+"");

        int countBox = db.getBoxesCount();
        Log.e("countBox: ", countBox+"");

        for (Box cn : boxes) {
            String log = " Title: " + cn.getTitle() + " ,Des: " + cn.getDescription();
            // Writing Contacts to log
            Log.d("Name: ", log);
        }

*/

        ///////////////////////////////////////////

        listView = (ListView) findViewById(R.id.list);
        adapter = new CustomListAdapter(this, categoryList);
        listView.setAdapter(adapter);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Category getCategory = categoryList.get(position);
                Toast.makeText(getApplicationContext(), "Hi: " + getCategory.getTitle(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(MainActivity.this, ViewGallery.class);
                i.putExtra("classFrom", MainActivity.class.toString());
                i.putExtra("id", getCategory.getId());
                i.putExtra("title", getCategory.getTitle());
                startActivity(i);
            }
        });


    }

    public void downloadFile(String uRl, String category, String filename) {

        File direct;
        String path ="";
        Log.e("Category downlaod: ", category +"  " +filename);

        if(category.isEmpty()){

            path = File.separator+"CrowdApps";
            direct = new File(Environment.getExternalStorageDirectory()
                    + path);

            Log.e("Category is Empty","Empty");

        }else{
            path = File.separator+"CrowdApps"+File.separator + category;
            direct = new File(Environment.getExternalStorageDirectory()
                    + path);

            Log.e("Category is There",path);

        }



        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(path, filename+".jpg");

        mgr.enqueue(request);

    }

    public void getDataFromServer(){
        Log.e("Get Data from S", " Called");
        // Creating volley request obj
        JsonArrayRequest movieReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        hidePDialog();

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Category c = new Category();
                                c.setId(obj.getInt("id"));
                                c.setTitle(obj.getString("title"));
                                c.setDescription(obj.getString("description"));
                                c.setImage_path( BaseUrl + obj.getString("avatar_url_thumb"));
                                if (db.isCategoryAlreadyExists(obj.getInt("id")) == false){
                                    Log.e("Going to download ", obj.getString("title"));
                                    downloadFile(c.getImage_path(), "", obj.getInt("id")+ "");
                                }else{
                                    Log.e("Catcannot download", obj.getString("title"));
                                }

                                db.addCategory(c);
                                categoryList.add(c);

                                Log.e("image path",c.getImage_path());

                                //Movie movie = new Movie();
                                //movie.setTitle(obj.getString("title"));
                                //movie.setThumbnailUrl(obj.getString("image"));
                                //downloadFile(obj.getString("image"));
                                //movie.setRating(((Number) obj.get("rating"))
                                //        .doubleValue());
                                //movie.setYear(obj.getInt("releaseYear"));

                                // Genre is json array
                                JSONArray boxesArray = obj.getJSONArray("boxes");

                                ArrayList<String> genre = new ArrayList<String>();

                                for (int j = 0; j < boxesArray.length(); j++) {

                                    try {
                                        JSONObject objBox = boxesArray.getJSONObject(j);

                                        Box b = new Box();

                                        b.setId(objBox.getInt("id"));
                                        b.setCategory_id(objBox.getInt("category_id"));
                                        b.setTitle(objBox.getString("title"));
                                        b.setDescription(objBox.getString("description"));
                                        b.setImage_path(BaseUrl + objBox.getString("avatar_url_medium"));
                                        if(db.isBoxAlreadyExists(objBox.getInt("id")) == false) {
                                            downloadFile(b.getImage_path(), c.getTitle(), objBox.getInt("id")+"");
                                        }else{
                                            Log.e("Box img exist", objBox.getString("title"));
                                        }
                                        db.addBox(b);

                                        Log.e("Box title", b.getTitle());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                    //genre.add((String) boxesArray.get(j));
                                }
                                //movie.setGenre(genre);

                                // adding movie to movies array
                                //movieList.add(movie);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {

        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Clheck if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        Log.e("Regid: ", registrationId);
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e("Post Execute", msg);
                //int status = sendRegistrationIdToBackend(); // Register reg on Crowdserver
                Log.e("Before register Key", "Before reg key #########");
                postRegId();
                Log.e("After register Key", "After reg key ********");
                //int status = 200;
                //if(status == 200){
                //    getDataFromServer(); // get data from Server
                //}

            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

      /*  if (view == findViewById(R.id.send)) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    mDisplay.append(msg + "\n");
                }
            }.execute(null, null, null);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
        } */
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private int sendRegistrationIdToBackend() {
        // Your implementation here.

        Log.e("Register Id: " , " Background");
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(BaseUrl+"app_users/register_app_user");

        String reg_id = getRegistrationId(getApplicationContext());

        Log.e("reg_id backend: " , reg_id);

        int code = -1;

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("reg_id", reg_id));
            nameValuePairs.add(new BasicNameValuePair("android_id", "Test android id"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            code = response.getStatusLine().getStatusCode();
            Log.e("Response: 1 " , code + "");


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e("First error: " , e.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("second error: " , e.toString());
        }

        return code;
    }

    public void registerGcmKeyAsync() { // RETIRE this function
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                Log.e("Register Id: " , " Background");
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(BaseUrl+"app_users/register_app_user");

                String reg_id = getRegistrationId(getApplicationContext());

                Log.e("reg_id backend: " , reg_id);

                int code = -1;


                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("reg_id", reg_id));
                    nameValuePairs.add(new BasicNameValuePair("android_id", "Test android id"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    code = response.getStatusLine().getStatusCode();
                    Log.e("Response: 1 " , code + "");

                    response = null;
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    Log.e("First error: " , e.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("second error: " , e.toString());
                }finally {

                    httpclient = null;
                    httppost = null;

                }


                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

        }.execute(null,null,null);
    }

    private void postRegId() {

        RequestQueue rq = Volley.newRequestQueue(this);

        StringRequest postReq = new StringRequest(Request.Method.POST, BaseUrl+"app_users/register_app_user", new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
               // tv.setText(response); // We set the response data in the TextView
                Log.e("Voley response", response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error ["+error+"]");

            }
        })  {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                String reg_id = getRegistrationId(getApplicationContext());

                Map<String, String> params = new HashMap<String, String>();
                params.put("reg_id", reg_id);
                params.put("param1", "Usman Khan");
                params.put("android_id", "Test android id");
                return params;
            }

        };

        rq.add(postReq);

    }



}