package com.zxl.demo.qiniudemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import upload.UploadManagerFactory;

public class MainActivity extends AppCompatActivity {

    private UploadManager uploadManager;
    private StringBuilder stringBuilder;
    private List<String> picPaths;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Configuration config = new Configuration.Builder()
                .chunkSize(256 * 1024)  //分片上传时，每片的大小。 默认256K
                .putThreshhold(512 * 1024)  // 启用分片上传阀值。默认512K
                .connectTimeout(10) // 链接超时。默认10秒
                .responseTimeout(60) // 服务器响应超时。默认60秒
                .zone(FixedZone.zone1) // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                .build();
        // 重用uploadManager。一般地，只需要创建一个uploadManager对象
        uploadManager = new UploadManager(config);
    }

    /**
     * 发送图片(集合)到七牛服务器《token从服务器获取》
     * @param uploadImages 项目中采用鲁班压缩的file集合
     * @param token 服务器获取的七牛token
     */
    private void pushPicToQiniu(List<File> uploadImages, String token) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        stringBuilder = new StringBuilder();
        final List<String> photoKeys = new ArrayList();
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
