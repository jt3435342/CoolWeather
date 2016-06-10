package utils;

/**
 * Created by taojiang on 6/9/2016.
 */
public interface HttpListener {
    public void onResponse(String response);
    public void onError(Exception e);
}
