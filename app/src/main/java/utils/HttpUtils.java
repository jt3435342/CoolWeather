package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by taojiang on 6/9/2016.
 */
public class HttpUtils {
    public static void sendHttpRequest(final String address,final HttpListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    System.setProperty("http.keepAlive", "false");//solve a eof exception
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader Reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = Reader.readLine())!=null) response.append(line);
                    if(listener!=null) listener.onResponse(response.toString());
                }catch (Exception e){
                    if(listener!=null) listener.onError(e);
                }finally {
                    connection.disconnect();
                }
            }
        }).start();

    }
}

