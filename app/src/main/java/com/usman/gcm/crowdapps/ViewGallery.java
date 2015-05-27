package com.usman.gcm.crowdapps;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;
import java.io.File;

public class ViewGallery extends Activity {

    ViewPager mViewPager;
    SlideShowPagerAdapter mSlideShowPagerAdapter;
    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    File file;


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

        file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "AnhsirkDasarpFiles");
        if (file.exists() && file.isDirectory()) {
            Toast.makeText(this, "File exists !!! and is a dir", Toast.LENGTH_LONG)
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
            // Create a String array for FilePathStrings
            FilePathStrings = new String[listFile.length];
            // Create a String array for FileNameStrings
            FileNameStrings = new String[listFile.length];

            for (int i = 0; i < listFile.length; i++) {
                // Get the path of the image file
                FilePathStrings[i] = listFile[i].getAbsolutePath();
                Toast.makeText(this, FilePathStrings[i].toString(), Toast.LENGTH_SHORT)
                        .show();                // Get the name image file

                Log.e("Iamge Path: ", FilePathStrings[i].toString());
                FileNameStrings[i] = listFile[i].getName();
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
            return mResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_item, container, false);
            Log.e("Position: ", position+"");
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);

           Bitmap bitmap = BitmapFactory.decodeFile(FilePathStrings[position]);
            imageView.setImageBitmap(bitmap);
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
            container.removeView((LinearLayout) object);
        }
    }


}
