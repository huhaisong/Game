package com.example.a111.game.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

/**
 * Created by 111 on 2016/9/5.
 */
public class MediaBean {

    private String path;
    private long thumbnailId;
    private Bitmap bitmap;
    private boolean isVideo;


    public MediaBean(String path, long thumbnailId, boolean isVideo) {
        this.path = path;
        this.thumbnailId = thumbnailId;
        this.isVideo = isVideo;
    }


    public Bitmap getBitmap(Context context) {
        Bitmap bitmap;
        if (isVideo) {
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                    context.getContentResolver(), thumbnailId,
                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
        } else {
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    context.getContentResolver(), thumbnailId,
                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }
        return bitmap;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "path=" + path;
    }
}
