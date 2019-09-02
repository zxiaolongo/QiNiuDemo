package upload;


import com.qiniu.android.common.FixedZone;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UploadManager;

/**
 * notice 区域设置很重要，zone(FixedZone.zone0)如果跟七牛服务器区域不同，可能导致上传失败
 */
public class UploadManagerFactory {
    private static UploadManager uploadManager;
    private static UploadManager dnsloadManager;
    private UploadManagerFactory() {
    }
    public static UploadManagerFactory getInstance() {
        return SingleHolder.mSingle;
    }
    //静态内部类
    private static class SingleHolder {
        private static final UploadManagerFactory mSingle = new UploadManagerFactory();
    }

    public UploadManager getDefultUploadManager() {
        if (uploadManager == null) {
            //notice 导入storage包下的Configuration   ***FixedZone.zone0（需与服务器上一致，否则容易导致上传失败）
            Configuration config = new Configuration.Builder()
                    .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                    .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                    .connectTimeout(10)           // 链接超时。默认10秒
                    .useHttps(true)               // 是否使用https上传域名
                    .responseTimeout(60)          // 服务器响应超时。默认60秒
//                .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
//                .recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                    .zone(FixedZone.zone0)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                    .build();
// 重用uploadManager。一般地，只需要创建一个uploadManager对象
            uploadManager = new UploadManager(config);
        }
        return uploadManager;
    }
//notice    部分老本版（7.3.12 及以下版本）默认使用 happy-dns-android ，在 android 8 下运行可能报错
//notice    java.io.IOException: cant get local dns server，可以禁用外部 dns，规避错误:
    public UploadManager getDnsUploadManager() {
        if (dnsloadManager == null) {
            Configuration config = new Configuration.Builder()
                    .dns(null)
                    .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                    .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                    .connectTimeout(10)           // 链接超时。默认10秒
                    .useHttps(true)               // 是否使用https上传域名
                    .responseTimeout(60)          // 服务器响应超时。默认60秒
//                .recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
//                .recorder(recorder, keyGen)   // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
                    .zone(FixedZone.zone0)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                    .build();
// 重用uploadManager。一般地，只需要创建一个uploadManager对象
            dnsloadManager = new UploadManager(config);
        }
        return dnsloadManager;
    }
//    推荐使用最新版，7.3.13 及以上版本。
//    从 7.3.13 开始，不在强制依赖 happy-dns-android，默认不再提供 httpDns，可以调用 Configuration.Builder#dns(com.qiniu.android.http.Dns)方法设置外部 Dns，自定义 Dns 要求实现 com.qiniu.android.http.Dns 接口。
//    如果可以明确 区域 的话，最好指定固定区域，这样可以少一步网络请求，少一步出错的可能。
//    public Dns buildDefaultDns() {
//        // 适当调整不同 IResolver 的加入顺序
//        ArrayList<IResolver> rs = new ArrayList<IResolver>(3);
//        try {
//            IResolver r1 = new Resolver(InetAddress.getByName("119.29.29.29"));
//            rs.add(r1);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        try {
//            rs.add(new Resolver(InetAddress.getByName("114.114.114.114")));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        try {
//            // 读取系统相关属性
//            // android 27 及以上 会报错
//            IResolver r2 = AndroidDnsServer.defaultResolver();
//            rs.add(r2);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        if (rs.size() == 0) {
//            return null;
//        }
//
//        final DnsManager happlyDns = new DnsManager(NetworkInfo.normal, rs.toArray(new IResolver[rs.size()]));
//
//        Dns dns = new Dns() {
//            // 若抛出异常 Exception ，则使用 okhttp 组件默认 dns 解析结果
//            @Override
//            public List<InetAddress> lookup(String hostname) throws UnknownHostException {
//                InetAddress[] ips;
//                try {
//                    ips = happlyDns.queryInetAdress(new Domain(hostname));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    throw new UnknownHostException(e.getMessage());
//                }
//                if (ips == null || ips.length == 0) {
//                    throw new UnknownHostException(hostname + " resolve failed.");
//                }
//                List<InetAddress> l = new ArrayList<>();
//                Collections.addAll(l, ips);
//                return l;
//            }
//        };
//        return dns;
//    }
}
