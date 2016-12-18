package cn.edu.gdaibzyy.autoupdate;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载调度管理器，调用我们的UpdateDownloadRequest
 */
public class UpdateManager {

    public static UpdateManager manager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    private UpdateManager(){
        threadPoolExecutor=(ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    static {
        manager = new UpdateManager();
    }

    public static UpdateManager getInstance(){
        return manager;
    }

    public void startDownloads(String downloadUrl,String localPath,
                               UpdateDownloadListener listener){
        if (request != null) {
            return;
        }

        checkLocalFilePath(localPath);

        //开始真正的去下载任务
        request = new UpdateDownloadRequest(downloadUrl, localPath, listener);
        Future<?> future=threadPoolExecutor.submit(request);
    }

    /**
     * 检查文件路径是否已经存在
     * @param path
     */
    private void checkLocalFilePath(String path) {
        File dir=new File(path.substring(0,path.lastIndexOf("/")+1));
        if (!dir.exists()){
            dir.mkdir();
        }
        File file=new File(path);
        if (!file.exists()){
            try {
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
