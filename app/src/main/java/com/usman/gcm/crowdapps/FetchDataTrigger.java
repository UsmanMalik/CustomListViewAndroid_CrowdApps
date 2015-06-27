package com.usman.gcm.crowdapps;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by usman on 6/15/15.
 */
public class FetchDataTrigger extends IntentService {
    public static final String url = "imsg";
    public static final String description = "omsg";
    private static final String BaseUrl = "http://192.168.1.4:3000/";
    final DatabaseHandler db = new DatabaseHandler(this);



    public FetchDataTrigger() {
        super("FetchDataTrigger");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String msg = intent.getStringExtra(url);
        String id = intent.getStringExtra(description);

        Log.e("Message Fetch data", msg);;
        getDataFromServer(msg,id);

    }

    public void getDataFromServer(String url, String id){

        url = BaseUrl+"/api/category/"+url+"/"+id;

        Log.e("New data intent service", url);


        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub
                Log.e("JsonObj Response", response.toString());
                String title;
                // Parsing json
                    try {


                        Category c = new Category();
                        c.setId(response.getInt("id"));
                        c.setTitle(response.getString("title"));
                        title = response.getString("title");
                        Log.e("Titlee", title);
                        c.setDescription(response.getString("description"));
                        c.setImage_path( BaseUrl + response.getString("avatar_url_original"));
                        if (db.isCategoryAlreadyExists(response.getInt("id")) == false){
                            Log.e("Going to download ", response.getString("title"));
                            downloadFile(c.getImage_path(), "", response.getInt("id")+ "");
                        }else{
                            Log.e("Catcannot download", response.getString("title"));
                        }

                        db.addCategory(c);
                        //categoryList.add(c);

                        Log.e("image path",c.getImage_path());

                        // Genre is json array
                        JSONArray boxesArray = response.getJSONArray("boxes");
                        JSONObject id = (JSONObject)boxesArray.get(0);

                        Log.e("boxes Length: ", ""+boxesArray.length());

                        ArrayList<String> genre = new ArrayList<String>();

                        for (int j = 0; j < boxesArray.length(); j++) {

                            try {
                                JSONObject objBox = boxesArray.getJSONObject(j);

                                Box b = new Box();

                                b.setId(objBox.getInt("id"));
                                b.setCategory_id(objBox.getInt("category_id"));
                                b.setTitle(objBox.getString("title"));
                                b.setDescription(objBox.getString("description"));
                                b.setImage_path(BaseUrl + objBox.getString("avatar_url_original"));
                                if(db.isBoxAlreadyExists(objBox.getInt("id")) == false) {
                                    Log.e("Title folder", title);
                                    downloadFile(b.getImage_path(), title, objBox.getInt("id") + "");
                                }else{
                                    Log.e("Box img exist", objBox.getString("title"));
                                }
                                db.addBox(b);

                                Log.e("Box title", b.getTitle());
                            } catch (JSONException e) {
                                Log.e("Error try ", e.toString());
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
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });
        queue.add(jsObjRequest);



    }

    public void downloadFile(String uRl, String category, String filename) {

        File direct;
        String path ="";
        Log.e("Category downlaod: ", category +"  " +filename + " Url: " + uRl);

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



}
