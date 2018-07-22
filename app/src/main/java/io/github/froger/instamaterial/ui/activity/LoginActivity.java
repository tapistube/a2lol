package io.github.froger.instamaterial.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;

public class LoginActivity extends AppCompatActivity {

    EditText emailid, password;
    Button loginButton;
    TextView forgotPassword, signUp;
    Intent i;
    DatabaseReference ref;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    ProgressBar progressBar;
    DialogInterface.OnClickListener listener;
    private String userID;
    public static MediaPlayer clickSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(LoginActivity.this);
        ref = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
        clickSound = MediaPlayer.create(LoginActivity.this,R.raw.pop);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser!=null){
            // User already signed in

            // get the FCM token
            String token = FirebaseInstanceId.getInstance().getToken();
            // save the user info in the database to users/UID/
            // we'll use the UID as part of the path
           /* User user = new User(fbUser.getUid(), fbUser.getDisplayName(), token);
            database.child("users").child(user.uid).setValue(user);*/

            // go to feed activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }


        emailid = (EditText) findViewById(R.id.login_emailid);
        password = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.loginBtn);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        signUp = (TextView) findViewById(R.id.createAccount);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(getApplicationContext(),DaftarActivity.class);
                startActivity(i);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //i = new Intent(getApplicationContext(),LupaPasswordActivity.class);
              //  startActivity(i);
            }
        });
    }

    private void matikanKomponen(){
        emailid.setEnabled(false);
        password.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
    }

    private  void hidupkanKomponen(){
        emailid.setEnabled(true);
        password.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        loginButton.setEnabled(true);
    }
    private void checkValidation() {
        String getEmailId = emailid.getText().toString();
        String getPassword = password.getText().toString();
        matikanKomponen();

        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        if (getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0) {

            customToast("Email dan password harus diisi");
            hidupkanKomponen();
        }
        else if (!m.find()) {
            customToast("Email anda tidak valid");
            hidupkanKomponen();
        }
        else {
            doLogin(getEmailId,getPassword);
        }

    }

    private void doLogin(final String email,String passwordUser){
        fAuth.signInWithEmailAndPassword(email,passwordUser).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (!task.isSuccessful()){
                    hidupkanKomponen();
                    customToast("Login Gagal, periksa kembali email dan password anda");
                    //Toast.makeText(getApplicationContext(), "Login Gagal, periksa kembali email dan password anda", Toast.LENGTH_LONG).show();
                }else{
                    // Successfully signed in

                    // get the Firebase user
                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

                    // get the FCM token
                    String token = FirebaseInstanceId.getInstance().getToken();
                    userID = fbUser.getUid();
                    i = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(i);

                }


            }
        });
    }

    public  void customToast(String s){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_root));

        TextView text = (TextView) layout.findViewById(R.id.toast_error);
        text.setText(s);
        Toast toast = new Toast(getApplicationContext());// Get Toast Context
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);// Set
        toast.setDuration(Toast.LENGTH_SHORT);// Set Duration
        toast.setView(layout); // Set Custom View over toast
        toast.show();// Finally show toast
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Apakan anda tetap ingin menutup aplikasi?");
        builder.setCancelable(false);

        listener = new DialogInterface.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    finishAffinity();
                    System.exit(0);
                }

                if(which == DialogInterface.BUTTON_NEGATIVE){
                    dialog.cancel();
                }
            }
        };
        builder.setPositiveButton("Ya",listener);
        builder.setNegativeButton("Tidak", listener);
        builder.show();
    }

    public static void bunyiKlik(){
        clickSound.start();
    }
}
