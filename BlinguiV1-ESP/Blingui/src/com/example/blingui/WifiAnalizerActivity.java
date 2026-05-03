package com.example.blingui;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class WifiAnalizerActivity extends Activity {
    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.blingui);
       mainText = (TextView) findViewById(R.id.mainText);
       mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
       receiverWifi = new WifiReceiver();
       registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
       mainWifi.startScan();

       mainText.setText("\nStarting Scan...\n");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }
    
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            mainWifi.startScan();
           // c.startActivity(intent);
        	sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
           
            for (ScanResult result : wifiList) {
            	
            //for(int i = 0; i < wifiList.size(); i++){
         
            //	sb.append(new Integer(i+1).toString() + ".");
                

        		    sb.append(result.SSID+"(");
            		sb.append(result.BSSID+"): ");
            		sb.append(" RSSI:"+result.level+",");
            		sb.append(" Frequencia: "+result.frequency+",");
            		sb.append(" Time:"+result.timestamp);
                    
                    
           sb.append("\n");
            	    
           sb.append("\n");
            //	}
            	  //   sb.append(mainWifi.getConnectionInfo().getMacAddress());
                //sb.append("\n");

               
            }
            //sb.append(mainWifi.getConnectionInfo().getMacAddress().toString());
           // sb.append(mainWifi.getConnectionInfo().getIpAddress());
            
            mainText.setText(sb);

            Toast t= Toast.makeText(c, "Executou o BroadCast", Toast.LENGTH_LONG);
            t.show();
            

        }
    }
}