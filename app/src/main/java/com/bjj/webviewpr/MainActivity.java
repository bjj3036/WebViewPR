package com.bjj.webviewpr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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

    private String getAnswerScript = "(function(){if(getAnswer&&typeof getAnswer == 'function'){return getAnswer()}return ''})()";

    private int currentProblemIndex = 0;

    private String[] mProblemFileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProblemFileNames = getResources().getStringArray(R.array.problem_file_names);

        mWebView = findViewById(R.id.webview_main);
        mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(this, "BRIDGE");
        findViewById(R.id.btn_main_submit).setOnClickListener(v -> {
            mWebView.evaluateJavascript(getAnswerScript, this::receiveAnswer);
        });
        loadCurrentProblem();
        try {
            //            mWebView.loadDataWithBaseURL("file:///android_asset/", loadAssetDecryptedData("test.htm"), "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void receiveAnswer(String answer) {
        if (answer == null || answer.length() <= 2) {
            Toast.makeText(this, "정답이 선택 또는 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
        } else {
            // "" 가 양 끝에 붙어서 값이 반환, "" 지우는 용도
            answer = answer.substring(1, answer.length() - 1);
            Toast.makeText(this, answer, Toast.LENGTH_SHORT).show();
            loadNextProblem();
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

    private void loadCurrentProblem() {
        mWebView.loadUrl("file:///android_asset/" + mProblemFileNames[currentProblemIndex]);
    }

    private void loadNextProblem() {
        ++currentProblemIndex;
        if (currentProblemIndex < mProblemFileNames.length) {
            mWebView.loadUrl("file:///android_asset/" + mProblemFileNames[currentProblemIndex]);
        } else {
            solvedAllProblems();
        }
    }

    // TODO : 결과 확인
    private void solvedAllProblems() {
        Toast.makeText(this, "모든 문제를 다 풀었습니다", Toast.LENGTH_SHORT).show();
        finish();
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

    static byte[] decodeAES(byte[] target) throws Exception {
        Key keySpec = new SecretKeySpec(key.getBytes(), "AES");
        String iv = key.substring(0, 16);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
        return c.doFinal(target);
    }

    private class CustomWebViewClient extends WebViewClient {

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
    }

}
