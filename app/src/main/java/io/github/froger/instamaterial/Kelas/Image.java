package io.github.froger.instamaterial.Kelas;

import com.google.firebase.database.Exclude;

/**
 * Created by Glory on 09/07/2018.
 */

public class Image {
    public String key;
    public String userId;
    public String downloadUrl;
    public String tipe;

    @Exclude
    public User user;

    @Exclude
    public int likes = 0;

    @Exclude
    public boolean hasLiked = false;

    @Exclude
    public String userLike;

    public Image() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Image(String key, String userId, String downloadUrl,String tipe) {
        this.key = key;
        this.userId = userId;
        this.downloadUrl = downloadUrl;
        this.tipe = tipe;
    }

    public void addLike() {
        this.likes++;
    }

    public void removeLike() {
        this.likes--;
    }

}
