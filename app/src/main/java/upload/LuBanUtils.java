package upload;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class LuBanUtils {
    /**
     * notice 图片压缩(集合)
     * @param photoPaths 压缩文件的路径集合
     */
    private void compressPic(final ArrayList<String> photoPaths, final Context context) {
        final List<File> uploadImages = new ArrayList<>();
        final LinkedList<Runnable> taskList = new LinkedList<>();
        final Handler handler = new Handler();
        class Task implements Runnable {
            String pathPic;

            Task(String path) {
                this.pathPic = path;
            }

            @Override
            public void run() {
                Luban.with(context)
                        .putGear(3)//压缩等级
                        .load(new File(pathPic))
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onSuccess(File file) {
                                long length = file.length() / 1024;
                                System.out.println(length);
                                uploadImages.add(file);
                                if (!taskList.isEmpty()) {
                                    Runnable runnable = taskList.pop();
                                    handler.post(runnable);
                                } else {
                                    //notice 集合压缩完毕 向服务器上传压缩后的文件
                                    // dosomthing(uploadImages)
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.i("ooo", "图片处理失败");
                            }
                        }).launch();
            }
        }
        //循环遍历原始路径 添加至linklist中
        for (String path : photoPaths) {
            taskList.add(new Task(path));
        }
        handler.post(taskList.pop());
    }

}
