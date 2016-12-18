package cn.edu.gdaibzyy.autoupdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import java.io.File;

/**
 * App更新下载后台服务
 */
public class UpdateService extends Service {

    private String apkURL;
    private String filePath;
    private NotificationManager notificationManager;
    private Notification mNotification;

    @Override
    public void onCreate() {

        notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        filePath= Environment.getExternalStorageDirectory()+"/autoupdate/update.apk";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent==null){
            notifyUser(getString(R.string.update_download_failed)
                    ,getString(R.string.update_download_failed_msg),0);
            stopSelf();
        }
        apkURL=intent.getStringExtra("apkUrl");
        notifyUser(getString(R.string.update_download_start)
                ,getString(R.string.update_download_start),0);
        startDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateManager.getInstance().startDownloads(apkURL, filePath,
                new UpdateDownloadListener() {
                    @Override
                    public void onStarted() {

                    }

                    @Override
                    public void onProgressChanged(int progress, String downloadUrl) {
                        notifyUser(getString(R.string.update_download_processing)
                                ,getString(R.string.update_download_processing),progress);
                    }

                    @Override
                    public void onFinished(int completeSize, String downloadUrl) {
                        notifyUser(getString(R.string.update_download_finish)
                                ,getString(R.string.update_download_finish),100);
                        stopSelf();
                    }

                    @Override
                    public void onFailure() {
                        notifyUser(getString(R.string.update_download_failed)
                                ,getString(R.string.update_download_failed_msg),0);
                        stopSelf();
                    }
                });
    }

    /**
     * 更新我们的notification来告知用户当前下载的进度
     * @param result
     * @param reason
     * @param progress
     */
    private void notifyUser(String result, String reason,int progress) {

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources()
                        ,R.mipmap.ic_launcher)).setContentTitle(getString(R.string.app_name));
        if (progress > 0 && progress < 100) {
            builder.setProgress(100,progress,false);
        }else{
            builder.setProgress(0,0,false);
        }

        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(result);
        builder.setContentIntent(progress >= 100 ? getContentIntent()
                :PendingIntent.getActivity(this,0,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT));
        mNotification=builder.build();
        notificationManager.notify(0,mNotification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private PendingIntent getContentIntent(){
        File apkFile=new File(filePath);
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://"+apkFile.getAbsolutePath())
                ,"application/vnd.android.package-archive");
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent
                ,PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
}
