package com.example.fcih_.mehnawy_task;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import static com.example.fcih_.mehnawy_task.R.string.repository;

//by ahmed elsayed 18 mar 2017
public class MainActivity extends AppCompatActivity {
    private static String url ="https://api.github.com/users/Square/repos";
    private static final String TAG_ID = "id";
    private static final String TAG__NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TAG_OWNER = "address";
    private static final String TAG_OWNER_login= "gender";
    private static final String TAG_OWNER_avatar_url = "sphone";
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList; //arraylist for add each object and its value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);
        new GetContacts().execute();

        /*making OnItemLongClickListener for list view that when user long click on specific item , return html_url for owner
        repository*/
       lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                   int pos, long id) {
       // String selected = ((TextView)findViewById(R.id.name)).getText().toString();
        String val =arg0.getItemAtPosition(pos).toString();
        final String[] arr=val.split(" ");
        final String[] res = {""};
        //create AlertDialog
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Ask")
                .setMessage("to go to repository `html_url` or owner `html_url` ")
                .setPositiveButton(repository, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {//if user choose repository html-url event
                        for(int i=0;i<arr.length;i++){
                             //return all text on clicked list item and return text to array to put each word to index with split function
                            if(arr[i].contains("html_url")) { //if text include "html-url" word then take this index and return value for that word
                                res[0] =arr[i].replace("html_url=", "");
                                res[0] = res[0].replace(",","");
                                //ex : html-url=https://github.com/square,  then remove "html-url=" and return value of url
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(res[0])); //intent to open url on browser
                                startActivity(browserIntent);
                            }

                        }
                    }
                })
                .setNegativeButton(R.string.owner, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {// if user choose owner html-url event
                        for(int i=0;i<arr.length;i++){
                            if(arr[i].contains("rep_html")) {
                                res[0] =arr[i].replace("rep_html=", "");
                                res[0] = res[0].replace(",","");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(res[0]));
                                startActivity(browserIntent);
                            }
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
        return true;
    }
});
    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray objects = new JSONArray(jsonStr);
                    // Getting JSON Array node
                    // looping through All objects
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject c = objects.getJSONObject(i);
                        String id = c.getString("id");
                        String name = c.getString("name");
                        String description = c.getString("description");
                        String html_url=c.getString("html_url");

                        // Owner node is JSON Object
                        JSONObject Owner = c.getJSONObject("owner");
                        String rep_html=Owner.getString("html_url");
                        String login = Owner.getString("login");
                        String avatar = Owner.getString("avatar_url");
                        // tmp hash map for single object
                        HashMap<String, String> object = new HashMap<>();
                        // adding each child node to HashMap key => value
                        object.put("name", name);
                        object.put("description", description);
                        object.put("html_url",html_url);
                        object.put("rep_html",rep_html);
                        object.put("login", login);
                        object.put("avatar",avatar);
                        // adding contact to contact list
                        contactList.add(object);
                        SaveToSharedPreferences(contactList); //cache list locally using sharedpreferences
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_view, new String[]{"name", "description","login"
                    ,"avatar"}, new int[]{R.id.name,
                    R.id.description, R.id.login,R.id.avatar});

            lv.setAdapter(adapter);
        }

        private void SaveToSharedPreferences(ArrayList arr){ // add list to sharedpreferences


            SharedPreferences prefs = getSharedPreferences("List", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson(); // convert to gson

            String jsonText = gson.toJson(arr);
            editor.putString("key", jsonText); //put to editor
            editor.commit(); // submit
        }

    }
}
