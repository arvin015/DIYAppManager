package com.activels.als.diyappmanager.qrcode;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.activels.als.diyappmanager.R;
import com.activels.als.diyappmanager.utils.ToastUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * @author arvin.li
 */
public class CaptureActivity extends Activity implements Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ImageButton mImageButtonBack;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_scan);

        CameraManager.init(getApplication());
        initControl();

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void initControl() {
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        initTitle();
    }

    private void initTitle() {
        mImageButtonBack = (ImageButton) findViewById(R.id.btn_left);
        mImageButtonBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    public void run() {
                        Instrumentation inst = new Instrumentation();
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                    }
                }).start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {

        inactivityTimer.onActivity();

        playBeepSoundAndVibrate();

        String resultStr = result.getText().toString();

        try {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("result", resultStr);

            setResult(RESULT_OK, resultIntent);

            finish();

        } catch (Exception e) {

            ToastUtil.toastLong(this, getString(R.string.scanning_fail_text));
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod(
                    "setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

        if (CameraManager.camera != null) {
            Camera.Parameters parameters = CameraManager.camera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {// 判断系统版本是否大于等于2.2
                setDisplayOrientation(CameraManager.camera, 90);// 旋转90，前提是当前页portrait，纵向
            } else { // 系统版本在2.2以下的采用下面的方式旋转
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);
                }
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "landscape");
                    parameters.set("rotation", 90);
                }
            }

            CameraManager.camera.setDisplayOrientation(0);
            CameraManager.camera.setParameters(parameters);
            CameraManager.camera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    /**
     * 扫描正确后的震动声音,如果感觉apk大了,可以删除
     */
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}
