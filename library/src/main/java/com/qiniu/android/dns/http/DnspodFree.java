package com.qiniu.android.dns.http;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bailong on 15/6/12.
 */
public final class DnspodFree implements IResolver {
    private final String ip;

    public DnspodFree(String ip) {
        this.ip = ip;
    }

    public DnspodFree() {
        this("119.29.29.29");
    }

    @Override
    public Record[] resolve(Domain domain, NetworkInfo info) throws IOException {
        URL url = new URL("http://119.29.29.29/d?ttl=1&dn=" + domain.domain);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setConnectTimeout(5000);
        httpConn.setReadTimeout(10000);
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        int length = httpConn.getContentLength();
        if (length <= 0 || length > 1024){
            return null;
        }
        InputStream is = httpConn.getInputStream();
        byte[] data = new byte[length];
        int read = is.read(data);
        is.close();
        String response = new String(data, 0, read);
        String[] r1 = response.split(",");
        if (r1.length != 2) {
            return null;
        }
        int ttl;
        try {
            ttl = Integer.parseInt(r1[1]);
        } catch (Exception e) {
            return null;
        }
        String[] ips = r1[0].split(";");
        if (ips.length == 0) {
            return null;
        }
        Record[] records = new Record[ips.length];
        long time = System.currentTimeMillis() / 1000;
        for (int i = 0; i < ips.length; i++) {
            records[i] = new Record(ips[i], Record.TYPE_A, ttl, time);
        }
        return records;
    }
}
