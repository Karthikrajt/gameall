package com.siragu.gameall.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import id.zelory.compressor.Compressor;

/**
 * Created by a_man on 04-12-2017.
 */

public class FirebaseUploader {
    private UploadListener uploadListener;
    private UploadTask uploadTask;
    private AsyncTask<File, Void, String> compressionTask;
    private boolean replace;
    private StorageReference uploadRef;
    private Uri fileUri;

    public FirebaseUploader(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public FirebaseUploader(UploadListener uploadListener, StorageReference storageRef) {
        this.uploadListener = uploadListener;
        this.uploadRef = storageRef;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public void uploadImage(Context context, File file) {
        compressAndUpload(context, "pictures", file);
    }

    public void uploadAudio(Context context, File file) {
        compressAndUpload(context, "audios", file);
    }

    public void uploadVideo(Context context, File file) {
        compressAndUpload(context, "videos", file);
    }

    public void uploadOthers(Context context, File file) {
        compressAndUpload(context, "others", file);
    }

    @SuppressLint("StaticFieldLeak")
    private void compressAndUpload(final Context context, final String child, final File file) {
        compressionTask = new AsyncTask<File, Void, String>() {
            @Override
            protected String doInBackground(File... files) {
                if (child.equals("pictures")) {
                    try {
                        File tempFile = new Compressor(context).setQuality(70).compressToFile(files[0]);
                        return tempFile.getAbsolutePath();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return files[0].getAbsolutePath();
                    }
                } else {
                    return files[0].getAbsolutePath();
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                File compressed = new File(s);
                if (child.equals("pictures")) {
                    Log.d("pSizeO-KB", String.valueOf(file.length()));
                    Log.d("pSizeB-KB", String.valueOf(compressed.length()));
                }
                fileUri = Uri.fromFile(compressed.length() > 0 ? compressed : file);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                if (uploadRef == null)
                    uploadRef = storage.getReference().child(child).child(fileUri.getLastPathSegment());

                if (replace) {
                    upload();
                } else {
                    checkIfExists();
                }
            }
        };
        compressionTask.execute(file);
    }

    private void checkIfExists() {
        uploadRef.getDownloadUrl().addOnSuccessListener(uri -> uploadListener.onUploadSuccess(uri.toString())).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                upload();
            }
        });
    }

    private void upload() {
        uploadTask = uploadRef.putFile(fileUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return uploadRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                uploadListener.onUploadSuccess(downloadUri.toString());
            } else {
                uploadListener.onUploadFail(task.getException().getMessage());
            }
        }).addOnFailureListener(e -> uploadListener.onUploadFail(e.getMessage()));
        uploadTask.addOnProgressListener(taskSnapshot -> {
            long progress = 0;
            try {
                progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            } catch (ArithmeticException ex) {
            }
            uploadListener.onUploadProgress((int) progress);
        });
    }

    public void cancelUpload() {
        if (compressionTask != null && compressionTask.getStatus() != AsyncTask.Status.FINISHED) {
            compressionTask.cancel(true);
        }
        if (uploadTask != null && uploadTask.isInProgress()) {
            uploadTask.cancel();
            uploadListener.onUploadCancelled();
        }
    }

    public interface UploadListener {
        void onUploadFail(String message);

        void onUploadSuccess(String downloadUrl);

        void onUploadProgress(int progress);

        void onUploadCancelled();
    }
}
