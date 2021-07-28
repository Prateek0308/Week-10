package com.cst2335.ahla0004;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This class is the first page of the application
 *
 * @author 16139
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity {



    String stringUrl="https://api.openweathermap.org/data/2.5/weather?q=TORONTO&appid=7e943c97096a9784391a981c4d878b22&Units=Metric";

    TextView txttemp;
    ImageView imgIcon;
    TextView txtmaxtemp;
    TextView txtmintemp;
    TextView txthumidity;
    TextView txtdescription;
    EditText cityTextField;
    float oldSize=14f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);






        txttemp = findViewById(R.id.temp);
        txtmaxtemp = findViewById(R.id.maxtemp);
        txtmintemp = findViewById(R.id.mintemp);
        txthumidity = findViewById(R.id.humidity);
        txtdescription = findViewById(R.id.description);

        imgIcon = findViewById(R.id.icon);

        cityTextField = findViewById(R.id.cityTextField);
        Button forecastbutton = findViewById(R.id.forecastbutton);
        NavigationView navigationView=findViewById(R.id.popout_menu);


        DrawerLayout drawer=findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, myToolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.setNavigationItemSelectedListener(item -> {

            onOptionsItemSelected(item);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        });

        forecastbutton.setOnClickListener(v ->
        {
            String cityName=cityTextField.getText().toString();
            myToolbar.getMenu().add( 0, 5, 0, cityName).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            runForeCast(cityName);

        });


    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {

        switch (item.getItemId())
        {

            case R.id.hide_views:
                txttemp.setVisibility(View.INVISIBLE);
                txtdescription.setVisibility(View.INVISIBLE);
                txtmaxtemp.setVisibility(View.INVISIBLE);
                txtmintemp.setVisibility(View.INVISIBLE);
                txthumidity.setVisibility(View.INVISIBLE);
                cityTextField.setText("");
                imgIcon.setVisibility(View.INVISIBLE);
                break;
            case R.id.id_increase:
                oldSize++;
                txttemp.setTextSize(oldSize);
                txtdescription.setTextSize(oldSize);
                txtmaxtemp.setTextSize(oldSize);
                txtmintemp.setTextSize(oldSize);
                txthumidity.setTextSize(oldSize);
                cityTextField.setTextSize(oldSize);
                break;
            case R.id.id_decrease:
                oldSize=Float.max(oldSize-1,5);
                txttemp.setTextSize(oldSize);
                txtdescription.setTextSize(oldSize);
                txtmaxtemp.setTextSize(oldSize);
                txtmintemp.setTextSize(oldSize);
                txthumidity.setTextSize(oldSize);
                cityTextField.setTextSize(oldSize);
                break;
            case 5:
                String cityName=item.getTitle().toString();
                runForeCast(cityName);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void runForeCast (String cityName)
    {

        AlertDialog dialog=new AlertDialog.Builder(MainActivity.this)
                .setTitle("Getting Forecast")
                .setMessage("We are calling people in "+cityName+" to look outside their windows and tell us whats the weather like over there.")
                .setView(new ProgressBar(MainActivity.this))
                .show();

        Executor newThread = Executors.newSingleThreadExecutor();

        newThread.execute( () ->
        {

            try
            {

                stringUrl= "https://api.openweathermap.org/data/2.5/weather?q="+URLEncoder.encode(cityName,"UTF-8")+"&appid=7e943c97096a9784391a981c4d878b22&Units=Metric";
                URL url = new URL(stringUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String text = (new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)))
                        .lines()
                        .collect(Collectors.joining("\n"));


                JSONObject theDocument = new JSONObject( text);

                JSONArray weatherArray = theDocument.getJSONArray ( "weather" );
                JSONObject position0=weatherArray.getJSONObject(0);
                JSONObject mainObject = theDocument.getJSONObject( "main" );

                String description=position0.getString("description");
                String iconName=position0.getString("icon");
                double current=mainObject.getDouble("temp");
                double min=mainObject.getDouble("temp_min");
                double max=mainObject.getDouble("temp_max");
                int humidity=mainObject.getInt("humidity");
                Bitmap image;
                File file=new File(iconName+".png");
                if(file.exists())
                {
                    image=BitmapFactory.decodeFile(getFilesDir()+"/"+iconName+".png");
                }
                else
                {
                    image=downloadImage(iconName);
                }




                runOnUiThread(() -> {




                    txttemp.setVisibility(View.VISIBLE);
                    txtdescription.setVisibility(View.VISIBLE);
                    txtmaxtemp.setVisibility(View.VISIBLE);
                    txtmintemp.setVisibility(View.VISIBLE);
                    txthumidity.setVisibility(View.VISIBLE);
                    imgIcon.setVisibility(View.VISIBLE);

                    txttemp.setText("The current temperature is "+current);
                    txtmaxtemp.setText("The max temperature is "+max);
                    txtmintemp.setText("The min temperature is "+min);
                    txthumidity.setText("The humidity is "+humidity);
                    txtdescription.setText(description);




                    imgIcon.setImageBitmap(image);

                    dialog.hide();
                });


            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();

                e.printStackTrace();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run () {
                        dialog.hide();
                        Toast.makeText(MainActivity.this,"No Forecast found for "+cityName,Toast.LENGTH_LONG).show();
                    }
                });
            }



        } );


    }

    public  Bitmap  downloadImage (String iconName)
    {

        try
        {
            Bitmap image = null;
            URL imgUrl = new URL( "https://openweathermap.org/img/w/" + iconName + ".png" );
            HttpURLConnection connection = (HttpURLConnection) imgUrl.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                image = BitmapFactory.decodeStream(connection.getInputStream());
                saveImage(image,iconName);
                return image;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public  void  saveImage(Bitmap image,String iconName)
    {
        FileOutputStream fOut = null;
        try {
            fOut = openFileOutput( iconName + ".png", Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }




}