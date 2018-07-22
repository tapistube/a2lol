package io.github.froger.instamaterial.Kelas;

/**
 * Created by Glory on 08/07/2018.
 */
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String uid;
    public String displayName;
    public String token;
    String nope;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String displayName, String token) {
        this.uid = uid;
        this.displayName = displayName;
        this.token = token;
    }


}
