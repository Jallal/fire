package edu.msu.becketta.fire;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String PREFERENCES = "preferences";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    /**
     * The user's saved or entered username
     */
    private String username;

    /**
     * The user's saved or entered password
     */
    private String password;


    /**
     * Create the activity
     *
     * @param savedInstanceState Stored activity bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readPreferences();
    }

    private void readPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        username = settings.getString(USERNAME, "");
        password = settings.getString(PASSWORD, "");

        setLoginStatus();
    }

    private void writePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(USERNAME, username);
        editor.putString(PASSWORD, password);

        editor.commit();
    }

    /**
     * Start the game activity
     *
     * @param view The view calling this function
     */
    public void onLogin(View view) {
        // Login
        TextView user = (TextView) findViewById(R.id.username);
        username = user.getText().toString();
        TextView pass = (TextView) findViewById(R.id.password);
        password = pass.getText().toString();

        // Remember the login info if they want
        CheckBox remember = (CheckBox)findViewById(R.id.remember);
        if (remember.isChecked()) {
            writePreferences();
        }

        setLoginStatus();
    }

    /**
     * Create a new user via dialog box
     */
    public CreateUserDialog onCreateUser(View view) {
        CreateUserDialog userDialog = new CreateUserDialog();
        userDialog.show(getFragmentManager(), "create");

        return userDialog;
    }

    private void setLoginStatus() {

        if (username == "" || password == "") {
            Toast.makeText(MainActivity.this,
                    R.string.login_fail,
                    Toast.LENGTH_LONG).show();
        } else {

            new AsyncTask<String, Void, Boolean>() {

                private ProgressDialog progressDialog;
                private Server server = new Server();

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog = ProgressDialog.show(MainActivity.this,
                            getString(R.string.please_wait),
                            getString(R.string.logging_in),
                            true, true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    server.cancel();
                                }
                            });
                }

                @Override
                protected Boolean doInBackground(String... params) {
                    boolean success = server.login(params[0], params[1]);
                    return success;
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    progressDialog.dismiss();
                    if (success) {
                        startFireActivity();
                        Toast.makeText(MainActivity.this,
                                R.string.login_success,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                R.string.login_fail,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(username, password);

        }
    }

    private void startFireActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
