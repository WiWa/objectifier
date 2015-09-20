package com.wiwa.objectifier;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendModelBtn = (Button) findViewById(R.id.sendmodelbtn);
        Button viewModelBtn = (Button) findViewById(R.id.viewmodelbtn);
        sendModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

                File f = new File(downloads + "/fordaryl1");

                Log.d("Files", "Path: " + f.toString());
                File file[] = f.listFiles();
                Log.d("Files", "Size: "+ file.length);
                String filename = null;

                JSONObject rest_json = new JSONObject();
                for (int i=0; i < file.length; i++)
                {
                    String name = file[i].getName();
                    Log.d("Files", "FileName:" + name);
                    filename = name;
                    if(filename.endsWith(".jpg") || filename.endsWith(".png")){
                        File img = file[i];
                        byte[] data = new byte[(int) img.length()];
                        try {
                            new FileInputStream(img).read(data);
                            String encoded = Base64.encodeToString(data, Base64.DEFAULT);
                            rest_json.put(img.getName(), encoded);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try{
                    sendJson(new URI("http://45.55.46.216:9000"), rest_json);
                }
                catch (Exception e){

                }
                if(filename != null){
                    Toast.makeText(MainActivity.this, "Doing the thing: " + filename, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Nopies.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    getThings(new URI("http://45.55.46.216:9000"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                String downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

                File f = new File(downloads + "/fordaryl1");

                Log.d("Files", "Path: " + f.toString());
                File file[] = f.listFiles();
                Log.d("Files", "Size: "+ file.length);
                String filename = null;
                for (int i=0; i < file.length; i++)
                {
                    String name = file[i].getName();
                    if(name.endsWith(".obj")){
                        Log.d("Files", "FileName:" + name);
                        filename = name;
                        break;
                    }
                }
                if(filename != null){
                    Toast.makeText(MainActivity.this, "Doing the thing: " + filename, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Nopies.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private class DownloadFilesTask extends AsyncTask<Object, Void, Void>{
        @Override
        protected Void doInBackground(Object... objects) {
            URI uri = (URI) objects[0];
            JSONObject json = (JSONObject) objects[1];
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
            HttpResponse response;

            try {

                HttpPost post = new HttpPost(uri);
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);

                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                Iterator<String> iter = json.keys();
                String k;
                while(iter.hasNext()){
                    k = iter.next();
                    entity.addPart(k, new StringBody((String) json.get(k)));
                }
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setEntity(entity);
                response = client.execute(httpPost);
                HttpEntity result = response.getEntity();

                /*Checking response */
                if(response!=null){
                    InputStream in = response.getEntity().getContent(); //Get the data in the entity
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    protected void sendJson(final URI uri, final JSONObject json) {

        // to start it
        new DownloadFilesTask().execute(uri, json);

    }
    protected void getThings(final URI uri) {

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
        HttpResponse response;
        JSONObject json = new JSONObject();

        try {

            HttpGet get = new HttpGet(uri);
            StringEntity se = new StringEntity( json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            response = client.execute(get);

                    //Checking response
            if(response!=null){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                byte[] buffer = new byte[in.available()];
                in.read(buffer);

                File targetFile = new File("/Android/data/com.winwang.objectify/files/teapots.obj");
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.winwang.objectify");
                startActivity(launchIntent);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
