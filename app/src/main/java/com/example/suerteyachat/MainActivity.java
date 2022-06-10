package com.example.suerteyachat;

import static com.example.suerteyachat.MainActivity.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

public class MainActivity<reloadWebView> extends AppCompatActivity {



    String url = "https://chat.suerteya.com";
    SwipeRefreshLayout Srl;
    WebView webView;
    final Context context = this;
    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    private final int MY_PERMISSIONS_REQUEST_READ_CAMERA = 0;
    private final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCameraPhotoPath;
    private String token;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    public static final int REQUEST_CODE_LOLIPOP = 1;
    private final static int RESULT_CODE_ICE_CREAM = 2;

    private ValueCallback<Uri[]> mFilePathCallback;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview);
        assert webView != null;
        WebSettings wS = webView.getSettings();
        wS.setSupportZoom(false);
        wS.setJavaScriptEnabled(true);
        wS.setAllowFileAccess(true);

        wS.setAppCacheEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wS.setSafeBrowsingEnabled(false);
            wS.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    wS.getInstance().setAcceptThirdPartyCookies(webView, true);
        }


        // Extras tried for Android 9.0, can be removed if want.
        wS.setAllowContentAccess(true);
        wS.setAllowFileAccess(true);
        wS.setBlockNetworkImage(false);

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);


        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        comprobarVersion();


        if (Build.VERSION.SDK_INT >= 22) {
            wS.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }





        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setVerticalScrollBarEnabled(false);
        webView.loadUrl(url);


        getToken();
        notificationChannel();




    }





    @Override
    protected void onPause() {
        super.onPause();
        WebSettings wS = webView.getSettings();
        wS.setJavaScriptEnabled(false);
        NotificationManagerCompat.from(context).cancelAll();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getToken();



    }

    @Override
    protected void onStop() {
        super.onStop();
        getToken();
        NotificationManagerCompat.from(context).cancelAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl(url);
        getToken();
        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){

            }
        });
        WebSettings wS = webView.getSettings();
        wS.setJavaScriptEnabled(true);
    }

    //CONTROLAR LA FLECHA DE ANDROID PARA IR ATRAS
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void comprobarVersion()  {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chat = db.collection("suerteya_chat");
        chat.document("data").get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isComplete()) {
            String lastVersion = task.getResult().getString("ver");
            String url = task.getResult().getString("download");
            if(!lastVersion.equals(BuildConfig.VERSION_NAME)){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Hay una version actualizada de la aplicacion: " +lastVersion)
                        .setPositiveButton("Descargar",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("Ahora no", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                 builder.create();
                builder.show();
            }
            }
            }
        });
    }



//FUNCION QUE DEVUELVE EL TOKEN DEL DISPOSITIVO
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String to) {
                webView.setWebViewClient(new WebViewClient(){
                    public void onPageFinished(WebView view, String url){
                        webView.loadUrl("javascript:obtenerToken('"+to+"')");
                    }
                });
            }
        });
    }

    //FUNCION QUE GENERA EL CANAL DE NOTIFICACIONES
    private void notificationChannel(){

        String CHANNEL_ID="NOTIFICACION";
        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name= "NOTIFICACION";
            NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager=(NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }









}


//INTERFAZ PARA EJECUTAR FUNCIONES DEL JS EN ANDROID
class WebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void closeNotifications() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }

}







