package com.example.filedowlandapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private EditText file_url;
    private ImageView image;
    private Button btnDownload, btnSetImage;
    DownloadManager downloadManager;
    Uri Download_Uri;
    private long refid;

    private int WRITE_PERMISSION_RQ = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        // Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionAgree();
        } else {
            Toast.makeText(this, "Ваша версия Android меньше 6.0", Toast.LENGTH_LONG).show();
        }


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_url = file_url.getText().toString();
                downloadInExternal(str_url);
            }
        });

        btnSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImageViewForRefId(refid);
            }
        });
    }

    private void setImageViewForRefId(long refid) {
        Uri download_file_uri = downloadManager.getUriForDownloadedFile(refid);
        Picasso.get().load(download_file_uri).into(image);
        //Toast.makeText(this, download_file_uri.toString(), Toast.LENGTH_SHORT).show();
    }

    private void permissionAgree() {
        if (!isWritePermissionGranted()) {
            requestWritePermission();
        } else {
            Toast.makeText(this, "Разрешение уже есть", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Show rationale
            new AlertDialog.Builder(this)
                    .setMessage("Без разрешения невозможно сохранить файл")
                    .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_RQ);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_RQ);
        }
    }

    // Обработка ответа пользователя на запрос разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != WRITE_PERMISSION_RQ) return;
        if (grantResults.length != 1) return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            /*String textToWrite = mInput.getText().toString();
            writeToFile(textToWrite);*/
            Toast.makeText(this, "Вы получили разрешение на запись во внешнюю память", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Вы можете дать разрешение в настройках приложения")
                    .setPositiveButton("Понятно", null)
                    .show();
        }

    }

    private void downloadInExternal(String str_url) {
        if (isImageUri(str_url)) {
            //Toast.makeText(this, "Оканчивается на .img", Toast.LENGTH_SHORT).show();
            if (!isWritePermissionGranted()) {
                requestWritePermission();
            } else {
                Download_Uri = Uri.parse(str_url);

                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle("Sample" + ".png");
                request.setDescription("Downloading " + "Sample" + ".png");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Sample" + ".png");

                refid = downloadManager.enqueue(request);
                btnSetImage.setEnabled(true);
            }
        } else {
            Toast.makeText(this, "Данная ссылка не является ссылкой на изображение", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isImageUri(String uri) {
        return uri.regionMatches(uri.length() - 4, ".jpg", 0, 4)
                || uri.regionMatches(uri.length() - 4, ".png", 0, 4)
                || uri.regionMatches(uri.length() - 4, ".bmp", 0, 4);
    }

    public void initUI() {
        file_url = findViewById(R.id.file_url);
        image = findViewById(R.id.imageView);
        btnDownload = findViewById(R.id.btn_file);
        btnSetImage = findViewById(R.id.set_image);
        downloadManager =(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    private boolean isWritePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}