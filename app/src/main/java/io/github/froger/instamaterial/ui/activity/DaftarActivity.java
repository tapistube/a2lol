package io.github.froger.instamaterial.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.froger.instamaterial.Kelas.User;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;

public class DaftarActivity extends AppCompatActivity {

    EditText fullName, emailId,
            password, confirmPassword;
    TextView login;
    Button signUpButton;
    CheckBox terms_conditions;
    Intent i;
    DatabaseReference ref;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    ProgressBar progressBar;
    User userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(DaftarActivity.this);
        ref = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();

        fullName = (EditText) findViewById(R.id.fullName);
        emailId = (EditText) findViewById(R.id.userEmailId);

        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        signUpButton = (Button) findViewById(R.id.signUpBtn);
        login = (TextView) findViewById(R.id.already_user);
        terms_conditions = (CheckBox) findViewById(R.id.terms_conditions);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
        fStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null ){
                    //user sedang login
                    Log.d("Fauth : ","onAuthStateChanged:signed_in:" + user.getUid());
                }
                //user sedang logout
                Log.d("Fauth : ","onAuthStateChanged:signed_out");
            }
        };
    }

    private void matikanKomponen(){
        progressBar.setVisibility(View.VISIBLE);
        fullName.setEnabled(false);
        emailId.setEnabled(false);
        password.setEnabled(false);
        confirmPassword.setEnabled(false);
        terms_conditions.setEnabled(false);
    }

    private void hidupkanKomponen(){
        progressBar.setVisibility(View.GONE);
        fullName.setEnabled(true);
        emailId.setEnabled(true);
        password.setEnabled(true);
        confirmPassword.setEnabled(true);
        terms_conditions.setEnabled(true);
    }

    private void checkValidation() {

        matikanKomponen();

        // Get all edittext texts
        String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();

        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0) {

            customToast("Semua field harus diiisi");
            hidupkanKomponen();
        }
        //check valid email
        else if (!m.find()) {
            customToast("Email anda tidak valid");
            hidupkanKomponen();
        }
        // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword)) {
            customToast("Konfirmasi Password tidak sesuai");
            hidupkanKomponen();
        }
        // Make sure user should check Terms and Conditions checkbox
        else if (!terms_conditions.isChecked()) {
            customToast("Term and Condition belum di ceklis");
            hidupkanKomponen();
        }
        // Else do signup or do your stuff
        else
            signUp(getEmailId,getPassword);

    }
    private void signUp(final String email, String passwordUser){

        fAuth.createUserWithEmailAndPassword(email,passwordUser).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d("Fauth :","createUserWithEmail:onComplete: " + task.isSuccessful());
                /**
                 * Jika sign in gagal, tampilkan pesan ke user. Jika sign in sukses
                 * maka auth state listener akan dipanggil dan logic untuk menghandle
                 * signed in user bisa dihandle di listener.
                 */

                if (!task.isSuccessful()){
                    Log.e("Eror gagal daftar ",task.getException().toString());
                    Toast.makeText(DaftarActivity.this,"Proses Pendaftaran gagal"+task.getException().toString(),Toast.LENGTH_LONG).show();

                    hidupkanKomponen();
                }else {
                    FirebaseUser user = fAuth.getCurrentUser();
                    String userID =  fAuth.getCurrentUser().getUid();
                    String token  = FirebaseInstanceId.getInstance().getToken();
                    //ganti nama
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName.getText().toString()).build();
                    user.updateProfile(profileChangeRequest);

                    userModel = new User(userID,
                            fullName.getText().toString(),
                            token);
                    ref = ref.child("users");
                    ref.child(userID).setValue(userModel);

                    Toast.makeText(DaftarActivity.this,"Proses Pendaftaran berhasil silakan menuju menu login \n" +
                            "Email : "+email,Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    hidupkanKomponen();
                    fullName.setText("");
                    emailId.setText("");
                    password.setText("");
                    confirmPassword.setText("");
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

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fStateListener != null) {
            fAuth.removeAuthStateListener(fStateListener);
        }
    }
}
