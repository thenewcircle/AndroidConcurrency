package com.marakana.android.labs.concurrency;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;

import com.marakana.android.labs.concurrency.svc.IPostCompletionHandler;
import com.marakana.android.labs.concurrency.svc.IPostingService;
import com.marakana.android.labs.concurrency.svc.PostingService;


public class MainActivity extends Activity implements ServiceConnection {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = -1;

    private static final String TAG = "MAIN";


    class PostCompletionHandler extends IPostCompletionHandler.Stub {
        private final Handler handler;

        public PostCompletionHandler() { this.handler = new Handler(); }

        @Override
        public void postCompleted(final int code) {
            handler.post(new Runnable() {
                @Override public void run() { postComplete(code); }
            });
        }
    };

    private View status;
    private Button button;
    private EditText postText;
    private boolean posting;
    private IPostingService service;
    private PostCompletionHandler hdlr;

    @Override
    public void onServiceConnected(ComponentName name, IBinder svc) {
        this.service = (IPostingService) svc;
        this.hdlr = new PostCompletionHandler();
        button.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        button.setEnabled(true);
        this.service = null;
        this.hdlr = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postText = (EditText) findViewById(R.id.mainText);
        status = findViewById(R.id.mainStatus);

        button = (Button) findViewById(R.id.mainButton);
        button.setEnabled(false);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    @Override public void onClick(View v) { post(); }
                } );
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, PostingService.class), this, BIND_AUTO_CREATE);
    }

    void post() {
        if (null == service) { return; }

        if (posting) { return; }
        posting = true;

        String text = postText.getText().toString();
        if (TextUtils.isEmpty(text)) { return; }

        postText.setText("");
        status.setBackgroundColor(Color.YELLOW);

        Log.d(TAG, "posting: " + text);
        try { service.post(text, hdlr); }
        catch (RemoteException e) { postComplete(STATUS_FAIL); }
    }

    void postComplete(int ret) {
        Log.d(TAG, "post complete");
        status.setBackgroundColor((0 <= ret) ? Color.GREEN : Color.RED);
        posting = false;
    }
}
