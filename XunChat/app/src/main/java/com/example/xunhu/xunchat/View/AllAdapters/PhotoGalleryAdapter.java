package com.example.xunhu.xunchat.View.AllAdapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.xunhu.xunchat.Model.AsyTasks.PicassoClient;
import com.example.xunhu.xunchat.Model.Entities.GalleryPhoto;
import com.example.xunhu.xunchat.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by xunhu on 7/30/2017.
 */

public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder> {
    private static Cursor mMediaStoreCursor;
    Activity activity;
    PhotoSelectListener photoSelectListener;
    public PhotoGalleryAdapter(Activity activity,PhotoSelectListener photoSelectListener){
        this.activity=activity;
        this.photoSelectListener=photoSelectListener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // an instance of ViewHolder class is created
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_unit_photo,
                parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap bitmap = getBitmapFromMediaStore(position);
        if (bitmap!=null){
            holder.getIvPhotoGallery().setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return (mMediaStoreCursor==null)? 0:mMediaStoreCursor.getCount();
    }
    private Cursor swapCursor(Cursor cursor){
        if (mMediaStoreCursor==cursor){
            return null;
        }
        Cursor oldCursor = mMediaStoreCursor;
        this.mMediaStoreCursor = cursor;
        if (cursor!=null){
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }
    public void changeCursor(Cursor cursor){
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor!=null){
            oldCursor.close();
        }
    }
    private Bitmap getBitmapFromMediaStore(int position){
        int idIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int mediaTypeIndex = mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        mMediaStoreCursor.moveToPosition(position);
        switch (mMediaStoreCursor.getInt(mediaTypeIndex)){
            case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                return MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(),
                        mMediaStoreCursor.getLong(idIndex),
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                );
            default:
                return null;
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView ivPhotoGallery;
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ivPhotoGallery= (ImageView) itemView.findViewById(R.id.ivPhotoGallery);
        }
        public ImageView getIvPhotoGallery() {
            return ivPhotoGallery;
        }
        @Override
        public void onClick(View v) {
            photoSelectListener.photoSelected(v,getAdapterPosition());
        }
    }
    public interface PhotoSelectListener{
        void photoSelected(View view, int position);
    }
}