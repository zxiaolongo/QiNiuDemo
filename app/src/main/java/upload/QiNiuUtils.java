package upload;

import android.support.annotation.NonNull;
import android.util.Log;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QiNiuUtils {
    private StringBuilder stringBuilder;

    /**
     * notice 没有进度监听--上传一个图片
     * @param data = <File对象、或 文件路径、或 字节数组>
     * @param key = <指定七牛服务上的文件名，或 null >;null时候
     * @param token token = <从服务端SDK获取 >;
     */
    public static void uploadProject(File data, String key, String token, @NonNull final FinishCallback<String> callback) {
        //指定zone的具体区域
        //FixedZone.zone0   华东机房
        //FixedZone.zone1   华北机房
        //FixedZone.zone2   华南机房
        //FixedZone.zoneNa0 北美机房

        //自动识别上传区域
        //AutoZone.autoZone
        UploadManager uploadManager = UploadManagerFactory.getInstance().getDefultUploadManager();
        uploadManager.put(data, key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                        if (info.isOK()) {
                            callback.onFinish("qiniu"+ key + ",\r\n " + info + ",\r\n " + res);
                            Log.i("qiniu", "Upload Success");
                        } else {
                            Log.i("qiniu", "Upload Fail");
                            //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                        }
                        Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                    }
                }, null);

    }



    /**
     * notice 添加进度监听，上传一个图片
     * @param data = <File对象、或 文件路径、或 字节数组>
     * @param key = <指定七牛服务上的文件名，或 null >;
     * @param token token = <从服务端SDK获取 >;
     */
    public void upload(File data, String key, String token) {
        //指定zone的具体区域
        //FixedZone.zone0   华东机房
        //FixedZone.zone1   华北机房
        //FixedZone.zone2   华南机房
        //FixedZone.zoneNa0 北美机房

        //自动识别上传区域
        //AutoZone.autoZone
        UploadManager uploadManager = UploadManagerFactory.getInstance().getDefultUploadManager();
        uploadManager.put(data, key, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                        if (info.isOK()) {
                            Log.i("qiniu", "Upload Success");
                        } else {
                            Log.i("qiniu", "Upload Fail");
                            //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                        }
                        Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                    }
                }, new UploadOptions(null, null, false, new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        // notice:  上传进度 
                        Log.i("qiniu", key + ": " + percent);
                    }
                },null));

    }

    /**
     * notice 上传文件集合
     * 发送图片(集合)到七牛服务器《token从服务器获取》
     * @param uploadImages 项目中采用鲁班压缩的file集合
     * @param picPaths 原图片路径的集合
     * @param token  服务器获取的七牛token
     */
    private void upLoadList(List<File> uploadImages, final List<String> picPaths, String token) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        stringBuilder = new StringBuilder();
        final List<String> photoKeys = new ArrayList();
        UploadManager uploadManager = UploadManagerFactory.getInstance().getDefultUploadManager();
        for (int i = 0; i < uploadImages.size(); i++) {
            File data = uploadImages.get(i);//<File对象、或 文件路径、或 字节数组>
            String photoPath = picPaths.get(i);
            String fileName = photoPath.substring(photoPath.lastIndexOf("/") + 1);
            String key = "zxl/image/" + date + "/" + fileName;//<指定七牛服务上的文件名，或// null>;
            uploadManager.put(data, key, token,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                photoKeys.add(key);
                                if (photoKeys.size() == picPaths.size()) {
                                    for (String photoKey : photoKeys) {
                                        if (stringBuilder.length() == 0) {
                                            stringBuilder.append(photoKey);
                                        } else {
                                            stringBuilder.append("," + photoKey);
                                        }
                                    }
                                    // notice:  这里是“，” 拼接的上传图片字符串
//                                    vcPicUrl = stringBuilder.toString();
                                }
                            } else {
                                Log.i("qiniu", "上传失败 Fail--");
                                //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                            }
                            Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                        }
                    }, null);
        }
    }
}
