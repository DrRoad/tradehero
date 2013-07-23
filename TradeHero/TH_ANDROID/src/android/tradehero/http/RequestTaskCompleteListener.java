package android.tradehero.http;

import org.json.JSONObject;



public interface RequestTaskCompleteListener {
   public void onTaskComplete(JSONObject pResponseObject);
   public void onErrorOccured(int pErrorCode,String pErrorMessage);
}
