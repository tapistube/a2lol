package io.github.froger.instamaterial.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.github.froger.instamaterial.Kelas.Image;
import io.github.froger.instamaterial.Manifest;
import io.github.froger.instamaterial.R;

public class UploadActivity extends AppCompatActivity {

    ImageView imgBrowse;
    TextView txtRule;
    EditText etJudul,etSumber;
    Button btnUpload;
    DatabaseReference ref;
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fStateListener;
    ArrayList<Image> images = new ArrayList<>();
    CheckBox terms_conditions;
    private String judul,sumber,section;

    static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    static final int RC_IMAGE_GALLERY = 2;

    FirebaseUser fbUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(UploadActivity.this);
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null) {
            finish();
        }
        ref = FirebaseDatabase.getInstance().getReference();

        imgBrowse = (ImageView) findViewById(R.id.img_browse);
        txtRule = (TextView) findViewById(R.id.already_user);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        etJudul = (EditText) findViewById(R.id.judulGambar);
        etSumber = (EditText) findViewById(R.id.sumber);
        terms_conditions = (CheckBox) findViewById(R.id.terms_conditions);

        imgBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.bunyiKlik();
              /*  if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_READ_EXTERNAL_STORAGE);
                } else {*/
               // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("image/*");
                //startActivityForResult(intent, RC_IMAGE_GALLERY);
                // }

                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UploadActivity.this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_READ_EXTERNAL_STORAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, RC_IMAGE_GALLERY);
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void checkValidation(){
        matikanKomponen();

        
    }

    private void matikanKomponen(){
        etJudul.setEnabled(false);
        etSumber.setEnabled(false);
        imgBrowse.setEnabled(false);
        terms_conditions.setEnabled(false);
    }

    private void hidupkanKomponen(){
        etJudul.setEnabled(true);
        etSumber.setEnabled(true);
        imgBrowse.setEnabled(true);
        terms_conditions.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, RC_IMAGE_GALLERY);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            final String tipe = GetMimeType(UploadActivity.this,uri);
            Toast.makeText(UploadActivity.this, "Tipe : !\n" + tipe, Toast.LENGTH_LONG).show();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imagesRef = storageRef.child("images");
            StorageReference userRef = imagesRef.child(fbUser.getUid());
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = fbUser.getUid() + "_" + timeStamp;
            StorageReference fileRef = userRef.child(filename);

            UploadTask uploadTask = fileRef.putFile(uri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(UploadActivity.this, "Upload failed!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(UploadActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();

                    // save image to database
                    String key = ref.child("images").push().getKey();
                    Image image = new Image(key, fbUser.getUid(), downloadUrl.toString(),tipe);
                    ref.child("images").child(key).setValue(image);
                }
            });
        }
    }

    public static String GetMimeType(Context context, Uri uriImage)
    {
        String strMimeType = null;

        Cursor cursor = context.getContentResolver().query(uriImage,
                new String[] { MediaStore.MediaColumns.MIME_TYPE },
                null, null, null);

        if (cursor != null && cursor.moveToNext())
        {
            strMimeType = cursor.getString(0);
        }

        return strMimeType;
    }
}
