package com.usman.gcm.crowdapps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewGallery extends Activity {

    ViewPager mViewPager;
    SlideShowPagerAdapter mSlideShowPagerAdapter;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    private List<Box> boxList = new ArrayList<Box>();

    File file;
    final DatabaseHandler db = new DatabaseHandler(this);


    int[] mResources = {
            R.drawable.download,
            R.drawable.jira111x30,
            R.drawable.download,
            R.drawable.jira111x30
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slideshow_main);


        Integer catId;
        String title;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                catId= null;
                title = null;
            } else {
                catId= extras.getInt("id");
                title = extras.getString("title");
            }
        } else {
            catId= (Integer) savedInstanceState.getSerializable("id");
            title= (String) savedInstanceState.getSerializable("title");
        }
        Toast.makeText(this,"Cat id: "+catId + " Title: " + title,Toast.LENGTH_SHORT  ).show();

        boxList = db.getCategoryBoxes(catId); // Handle if null
        Log.e("Boxlist size", boxList.size() + "");

        if(boxList.size() == 0){

            Toast.makeText(this,"No Box found", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);

        }

        for (Box cn : boxList) {
            String log = " ** ** Title: " + cn.getTitle() + " ,List : " + cn.getDescription();
            // Writing Contacts to log
            Log.d("Name: ", log);
        }




        file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "CrowdApps"+ File.separator+title);
        if (file.exists() && file.isDirectory()) {
            Toast.makeText(this, "File exists! " + file.getAbsolutePath(), Toast.LENGTH_SHORT)
                    .show();
        }

 /*       //SD card Available
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Error! No SDCARD Found!", Toast.LENGTH_LONG)
                    .show();
        } else {

            Toast.makeText(this, "Going to create a new dir", Toast.LENGTH_LONG)
                    .show();
            // Locate the image folder in your SD Card
            file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "AnhsirkDasarpFiles");
            // Create a new folder if no folder named SDImageTutorial exist
            file.mkdirs();
        }
*/
        if (file.isDirectory()) {
            listFile = file.listFiles();
            String filePath =  file.getAbsolutePath()+File.separator;

            // Create a String array for FilePathStrings
            FilePathStrings = new String[listFile.length];
            // Create a String array for FileNameStrings
            FileNameStrings = new String[listFile.length];
            Log.e("boxlist size: ", boxList.size()+"");
            for (int i = 0; i < boxList.size(); i++) {
                // Get the path of the image file
                FilePathStrings[i] = filePath+boxList.get(i).getId()+ ""+".jpg";
                Toast.makeText(this, FilePathStrings[i].toString(), Toast.LENGTH_SHORT)
                       .show();                // Get the name image file
                Log.e(" Absolute Path: ", listFile[i].getAbsolutePath().toString());
                Log.e("Iamge Path: ", FilePathStrings[i].toString());

                FileNameStrings[i] = boxList.get(i).getId()+ ""+".jpg";
            }
        }



        // Instantiate a ViewPager and a PagerAdapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSlideShowPagerAdapter = new SlideShowPagerAdapter(this);
        mViewPager.setAdapter(mSlideShowPagerAdapter);
    }


    class SlideShowPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public SlideShowPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
           //int length = FilePathStrings.length;
            //return mResources.length;
           // Log.e("Length getCount: ", length + "");

            return  boxList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_item, container, false);
            Log.e("Position: ", position + " "+ FilePathStrings[position] );
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            TextView titleView = (TextView) itemView.findViewById(R.id.boxTitle);
            TextView descriptionView = (TextView) itemView.findViewById(R.id.description);

            //File imgFile = new  File("/storage/sdcard0/CrowdApps/Android/1.jpg");
            //Log.e("Image File Path: ", imgFile.getAbsolutePath());

            Bitmap bitmap = BitmapFactory.decodeFile(FilePathStrings[position]);
            Log.e("bitmap ++" ,bitmap.getHeight()+"");

            imageView.setImageBitmap(bitmap);

            titleView.setText(boxList.get(position).getTitle());
            descriptionView.setText(boxList.get(position).getDescription());
/*
*
* /storage/sdcard0/AnhsirkDasarpFiles/fileName-23.jpg
05-27 15:41:45.238  28977-28977/com.usman.gcm.crowdapps E/Iamge Path:﹕ /storage/sdcard0/AnhsirkDasarpFiles/fileName-41.jpg
05-27 15:41:45.248  28977-28977/com.usman.gcm.crowdapps E/Iamge Path:﹕ /storage/sdcard0/AnhsirkDasarpFiles/fileName-47.jpg*/
           // imageView.setImageResource(mResources[position]);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }


}
