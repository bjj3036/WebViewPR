package com.bjj.webviewpr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.webview_main);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                try {
                    return shouldInterceptRequest(view, uri.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.startsWith("file:///android_asset")) {
                    url = url.substring(21);
                    try {
                        return loadAssetDecryptedResponse(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
        //        mWebView.loadUrl("file:///android_asset/test.html");
        try {
            mWebView.loadDataWithBaseURL("file:///android_asset/", loadAssetDecryptedData("test.htm"), "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadAssetDecryptedData(String path) throws Exception {
        InputStream inputStream = getAssets().open(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return new String(decodeAES(byteArrayOutputStream.toByteArray()), "UTF-8");
    }

    private WebResourceResponse loadAssetDecryptedResponse(String path) throws Exception {
        InputStream inputStream = getAssets().open(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(decodeAES(byteArrayOutputStream.toByteArray())));
    }

    @Override
    public void onBackPressed() {
        if (mWebView == null || !mWebView.canGoBack()) {
            super.onBackPressed();
            return;
        }
        mWebView.goBack();
    }

    private static String key = "q4hs0d3ksd0d0384ksowk29dohskd954";

    static byte[] encodeAES(byte[] target) throws Exception {
        Key keySpec = new SecretKeySpec(key.getBytes(), "AES");
        String iv = key.substring(0, 16);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        return c.doFinal(target);
    }

    static byte[] decodeAES(byte[] target) throws Exception{
        Key keySpec = new SecretKeySpec(key.getBytes(), "AES");
        String iv = key.substring(0, 16);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        return c.doFinal(target);
    }

}
