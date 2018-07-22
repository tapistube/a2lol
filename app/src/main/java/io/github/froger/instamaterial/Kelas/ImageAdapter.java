package io.github.froger.instamaterial.Kelas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import io.github.froger.instamaterial.ui.activity.MainActivity;
import io.github.froger.instamaterial.R;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<Image> mDataset;
    private MainActivity mActivity;
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";
    private Context context;
    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;
    private final int cellSize,cellHeight;
    private  int pos;
    private int cek;



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageView;
        public ImageButton mLikeButton;
        public View vBgLike;
        public ImageView ivLike;
        public ImageButton btnComments;
        public ImageButton btnMore;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.txtJudulGambar);
            mImageView = (ImageView) v.findViewById(R.id.ivFeedCenter);
            mLikeButton = (ImageButton) v.findViewById(R.id.btnLike);
            vBgLike = (View) v.findViewById(R.id.vBgLike);
            ivLike = (ImageView) v.findViewById(R.id.ivLike);
            btnComments = (ImageButton) v.findViewById(R.id.btnComments);
            btnMore = (ImageButton) v.findViewById(R.id.btnMore);
        }
    }

    public ImageAdapter(ArrayList<Image> myDataset, MainActivity activity) {
        mDataset = myDataset;
        mActivity = activity;
        this.context = context;
        this.cellSize = Utils.getScreenWidth(activity);
        this.cellHeight = Utils.getScreenHeight(activity);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);

        ViewHolder vh = new ViewHolder(v);
        Log.d("viewTypeIA:",""+viewType);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        cek = 0;
        pos = position;
        final Image image = (Image) mDataset.get(position);
        if (image.user != null) {
            holder.mTextView.setText(image.user.displayName);
        }


        if (image.tipe.equals("image/gif")){
            Glide.with(this.mActivity).load(image.downloadUrl)
                    .asGif()
                    .placeholder(R.drawable.gif_placeholder)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mImageView);
        }else {
            Glide.with(this.mActivity).load(image.downloadUrl)
                    .asBitmap()
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mImageView);
        }



        //

        //if gif =
        //Glide.with.asGif()

        //holder.mLikeButton.setText("Like (" + image.likes + ")");

        holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                mActivity.showLikedSnackbar();
                Toast.makeText(mActivity,"button like",Toast.LENGTH_SHORT).show();
            }
        });
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(position,ACTION_LIKE_IMAGE_CLICKED);
                mActivity.showLikedSnackbar();
                Toast.makeText(mActivity,"image like",Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onCommentsClick(v,position);
            }
        });
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onMoreClick(v,position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addImage(Image image) {
        mDataset.add(0, image);
        notifyDataSetChanged();
    }

    private int dpToPx(int dp) {
        float density = mActivity.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private void scaleImage(ImageView view) throws NoSuchElementException  {
        // Get bitmap from the the ImageView.
        Bitmap bitmap = null;

        final Image image = (Image) mDataset.get(pos);
        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {
            // Check bitmap is Ion drawable
           //bitmap = Glide.with(mActivity).
        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(250);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        Log.i("Test", "done");
    }
}
