package com.mapplace;

//import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List; 
import java.util.Locale; 

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.graphics.drawable.Drawable;
import android.location.Address; 
import android.location.Criteria; 
import android.location.Geocoder; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.os.Bundle; 
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.RatingBar;
import android.widget.Toast;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;

public class MyGoogleMap extends MapActivity 
{ 
  //private TextView mTextView01;
  static public MyGoogleMap my;
  private String strLocationProvider = ""; 

  private static int DB_VERSION = 1;

  private LocationManager mLocationManager01; 
  private Location mLocation01; 
  private MapController mMapController01; 
  private MapView mMapView; 
  
  private SQLiteDatabase db;
  private SQLiteHelper dbHelper;
  private Cursor cursor;

  private MyOverLay overlay;
  private List<MapLocation> mapLocations;

  private Button mButton01,mButton02,mButton03,mButton04,mButton05;
  private int intZoomLevel=0;//geoLatitude,geoLongitude; 
  public GeoPoint nowGeoPoint;

  public static  MapLocation mSelectedMapLocation;
  
  private String str;
  
  private static final int MSG_SHOW_MESSAGE = 1;  
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 

    my = this;
    
    mMapView = (MapView)findViewById(R.id.myMapView1); 
    mMapController01 = mMapView.getController(); 
     
    mMapView.setSatellite(false);
    mMapView.setStreetView(true);
    mMapView.setEnabled(true);
    mMapView.setClickable(true);
     
    intZoomLevel = 15; 
    mMapController01.setZoom(intZoomLevel); 

    try{
      dbHelper = new SQLiteHelper(this, SQLiteHelper.DB_NAME, null, DB_VERSION);
      db = dbHelper.getWritableDatabase();
    }
    catch(IllegalArgumentException e){
      e.printStackTrace();
      ++ DB_VERSION;
      dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
    }
    
    mLocationManager01 =  
    (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
     
    getLocationProvider(); 
     
    nowGeoPoint = getGeoByLocation(mLocation01); 
    
    if (nowGeoPoint != null)
    {
      refreshMapViewByGeoPoint(nowGeoPoint, 
          mMapView, intZoomLevel); 
     //openOptionsDialog("no GPS rec");    
    }
    
     
    mLocationManager01.requestLocationUpdates 
    (strLocationProvider, 2000, 10, mLocationListener01); 
     
    getMapLocations(true);
    
    overlay = new MyOverLay(this);
    mMapView.getOverlays().add(overlay);
    //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());

    mButton01 = (Button)findViewById(R.id.myButton1); 
    mButton01.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        //nowGeoPoint
        double geoLatitude = 0.0; 
        double geoLongitude = 0.0; 

        if (nowGeoPoint != null)
        {
          geoLatitude = (int)nowGeoPoint.getLatitudeE6()/1E6; 
          geoLongitude = (int)nowGeoPoint.getLongitudeE6()/1E6;
        }
        
      } 
    }); 
     
    mButton02 = (Button)findViewById(R.id.myButton2); 
    mButton02.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel++; 
        if(intZoomLevel>mMapView.getMaxZoomLevel()) 
        { 
          intZoomLevel = mMapView.getMaxZoomLevel(); 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    } ); 
     

    mButton03 = (Button)findViewById(R.id.myButton3); 
    mButton03.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub 
        intZoomLevel--; 
        if(intZoomLevel<1) 
        { 
          intZoomLevel = 1; 
        } 
        mMapController01.setZoom(intZoomLevel); 
      } 
    });

    //Satellite
    mButton04 = (Button)findViewById(R.id.myButton4); 
    mButton04.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      { 
        // TODO Auto-generated method stub
       String str = mButton04.getText().toString();
        
       if (str.equals("衛星"))
       {
        mButton04.setText("街道");
        mMapView.setStreetView(false);
        mMapView.setSatellite(true);
        mMapView.setTraffic(false);
       }
       else
       {
         mButton04.setText("衛星");
         mMapView.setStreetView(true);
         mMapView.setSatellite(false);
         mMapView.setTraffic(false);
       }
      } 
    }); 

    mButton05 = (Button)findViewById(R.id.myButton5); 
    mButton05.setOnClickListener(new Button.OnClickListener() 
    { 
      public void onClick(View v) 
      {
      } 
    });
   
   /* GeoPoint gp = new GeoPoint((int)geoLatitude,(int)geoLongitude);
    Drawable dr = getResources().getDrawable
    (
      android.R.drawable.arrow 
     );
    dr.setBounds(-15,-15,15, 15);
    
    MyItemOverlay mOverlay01 = new MyItemOverlay(dr,gp);
    List<Overlay> overlays = mMapView.getOverlays();
    overlays.add(mOverlay01);*/
  }
  
  public List<MapLocation> getMapLocations(boolean doit) 
  {
    if (mapLocations == null || doit == true) 
    {
      mapLocations = new ArrayList<MapLocation>();

      //loading db      
      //Query DATABASE
      try{
          cursor = db.query(SQLiteHelper.TB_NAME, null, null, null, null, null, null);

        cursor.moveToFirst();
        
        //no data
        if (cursor.isAfterLast())
        {
          MapLocation place = new MapLocation("net服飾", 24.992476, 121.542821, null);
          mapLocations.add(place);
          place = new MapLocation("衣服店", 25.0007561, 121.5419251, null);
          mapLocations.add(place);
          
          return mapLocations;
        }
        
        int i=0;
        
        while(!cursor.isAfterLast())
        {
          store_item sitem = new store_item();
          sitem.name = cursor.getString(1);
          sitem.intro = cursor.getString(2);
          sitem.time = cursor.getString(3);
          sitem.phone = cursor.getString(4);
          sitem.addr = cursor.getString(5);
          sitem.commit = cursor.getString(6);
          //openOptionsDialog(sitem.addr);
          
          GeoPoint nowgp = getGeoPoint(getLocationInfo(sitem.addr)); 
              //getGeoByAddress(sitem.addr);

          if (nowgp == null)
          {
            cursor.moveToNext();
            continue;
          }
          double Latitude = nowgp.getLatitudeE6()/ 1E6;
          double Longitude = nowgp.getLongitudeE6()/ 1E6;
          
           MapLocation newLoaction = new MapLocation(sitem.name, Latitude, Longitude, sitem);
           mapLocations.add(newLoaction);
           cursor.moveToNext();
          }
      }catch(IllegalArgumentException e){
        e.printStackTrace();
        ++ DB_VERSION;
        dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
      }               
      
    }
    
    //openOptionsDialog(Integer.toString(mapLocations.size()));
    
    
    return mapLocations;
  }

 
  public final LocationListener mLocationListener01 =  
  new LocationListener() 
  { 
    public void onLocationChanged(Location location) 
    { 
      // TODO Auto-generated method stub 
       
      mLocation01 = location; 
      nowGeoPoint = getGeoByLocation(location); 
      refreshMapViewByGeoPoint(nowGeoPoint, 
            mMapView, intZoomLevel); 
    } 

    public void onProviderDisabled(String provider) 
    { 
      // TODO Auto-generated method stub 
      mLocation01 = null; 
    } 
     
    public void onProviderEnabled(String provider) 
    { 
      // TODO Auto-generated method stub 
       
    } 
     
    public void onStatusChanged(String provider, 
                int status, Bundle extras) 
    { 
      // TODO Auto-generated method stub 
       
    } 
  }; 
   
  private GeoPoint getGeoByLocation(Location location) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if (location != null) 
      { 
        double geoLatitude = location.getLatitude()*1E6; 
        double geoLongitude = location.getLongitude()*1E6; 
        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return gp; 
  } 
   
  private GeoPoint getGeoByAddress(String strSearchAddress) 
  { 
    GeoPoint gp = null; 
    try 
    { 
      if(strSearchAddress!="") 
      { 
        Geocoder mGeocoder01 = new Geocoder(MyGoogleMap.this, Locale.getDefault()); 
         
        List<Address> lstAddress = mGeocoder01.getFromLocationName
                           (strSearchAddress, 10);
        if (!lstAddress.isEmpty()) 
        { 
          /*for (int i = 0; i < lstAddress.size(); ++i)
          {
            Address adsLocation = lstAddress.get(i);
            //Log.i(TAG, "Address found = " + adsLocation.toString()); 
            double geoLatitude = adsLocation.getLatitude();
            double geoLongitude = adsLocation.getLongitude();
          } */
          Address adsLocation = lstAddress.get(0); 
          double geoLatitude = adsLocation.getLatitude()*1E6; 
          double geoLongitude = adsLocation.getLongitude()*1E6; 
          gp = new GeoPoint((int) geoLatitude, (int) geoLongitude); 
        }
        
      } 
    } 
    catch (Exception e) 
    {  
      e.printStackTrace();  
    } 
    return gp; 
  } 
   
  public static void refreshMapViewByGeoPoint 
  (GeoPoint gp, MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(gp); 
      myMC.setZoom(zoomLevel); 
      //mapview.setSatellite(false);
      
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  public static void refreshMapViewByCode 
  (double latitude, double longitude, 
      MapView mapview, int zoomLevel) 
  { 
    try 
    { 
      GeoPoint p = new GeoPoint((int) latitude, (int) longitude); 
      mapview.displayZoomControls(true); 
      MapController myMC = mapview.getController(); 
      myMC.animateTo(p); 
      myMC.setZoom(zoomLevel); 
      mapview.setSatellite(false); 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
  } 
   
  private String GeoPointToString(GeoPoint gp) 
  { 
    String strReturn=""; 
    try 
    { 
      if (gp != null) 
      { 
        double geoLatitude = (int)gp.getLatitudeE6()/1E6; 
        double geoLongitude = (int)gp.getLongitudeE6()/1E6; 
        strReturn = String.valueOf(geoLatitude)+","+
          String.valueOf(geoLongitude); 
      } 
    } 
    catch(Exception e) 
    { 
      e.printStackTrace(); 
    } 
    return strReturn; 
  }
  
    
  
  public String getIEMI()
  {
    return  ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
  }
   
  public void getLocationProvider() 
  { 
    try 
    { 
      Criteria mCriteria01 = new Criteria(); 
      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE); 
      mCriteria01.setAltitudeRequired(false); 
      mCriteria01.setBearingRequired(false); 
      mCriteria01.setCostAllowed(true); 
      mCriteria01.setPowerRequirement(Criteria.POWER_LOW); 
      strLocationProvider =  
      mLocationManager01.getBestProvider(mCriteria01, true); 
       
      mLocation01 = mLocationManager01.getLastKnownLocation (strLocationProvider); //?
    } 
    catch(Exception e) 
    { 
      //mTextView01.setText(e.toString()); 
      e.printStackTrace(); 
    } 
  }
  
 /* private class MyItemOverlay extends ItemizedOverlay<OverlayItem>
  {
    private List<OverlayItem> items = new ArrayList<OverlayItem>();
    public MyItemOverlay(Drawable defaultMarker , GeoPoint gp)
    {
      super(defaultMarker);
      items.add(new OverlayItem(gp,"Title","Snippet"));
      populate();
    }
    
    @Override
    protected OverlayItem createItem(int i)
    {
      return items.get(i);
    }
    
    @Override
    public int size()
    {
      return items.size();
    }
    
    @Override
    protected boolean onTap(int pIndex)
    {
      Toast.makeText
      (
        Flora_Expo.this,items.get(pIndex).getSnippet(),
        Toast.LENGTH_LONG
      ).show();
      return true;
    }
  }*/
   
  @Override 
  protected boolean isRouteDisplayed() 
  { 
    // TODO Auto-generated method stub 
    return false; 
  } 

  public void showPlaceDiag(MapLocation mSelectedMapLocation2)
  {
    // TODO Auto-generated method stub
    str = "店家: " + mSelectedMapLocation2.sitem.name + "\n" 
                + "簡介: " + mSelectedMapLocation2.sitem.intro + "\n"
                + "營業時間: " + mSelectedMapLocation2.sitem.time + "\n"
                + "電話: " + mSelectedMapLocation2.sitem.phone + "\n"
                + "地址: " + mSelectedMapLocation2.sitem.addr + "\n";
    
    Message msg = new Message();
    msg.what = MSG_SHOW_MESSAGE;
    myHandler.sendMessage(msg);   }
  
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_SHOW_MESSAGE:
            openOptionsDialog(str);
          default:
        }
        super.handleMessage(msg);
    }
};   

public static JSONObject getLocationInfo(String address) {  
  
  HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" + address  
          + "ka&sensor=false");  
  HttpClient client = new DefaultHttpClient();  
  HttpResponse response;  
  StringBuilder stringBuilder = new StringBuilder();  

  try {  
      response = client.execute(httpGet);  
      HttpEntity entity = response.getEntity();  
      InputStream stream = entity.getContent();  
      int b;  
      while ((b = stream.read()) != -1) {  
          stringBuilder.append((char) b);  
      }  
  } catch (ClientProtocolException e) {  
  } catch (IOException e) {  
  }  

  JSONObject jsonObject = new JSONObject();  
  try {  
      jsonObject = new JSONObject(stringBuilder.toString());  
  } catch (JSONException e) {  
      // TODO Auto-generated catch block  
      e.printStackTrace();  
  }  

  return jsonObject;  
}  

public static GeoPoint getGeoPoint(JSONObject jsonObject) {  
  
  Double lon = new Double(0);  
  Double lat = new Double(0);  

  try {  

      lon = ((JSONArray)jsonObject.get("results")).getJSONObject(0)  
          .getJSONObject("geometry").getJSONObject("location")  
          .getDouble("lng");  

      lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)  
          .getJSONObject("geometry").getJSONObject("location")  
          .getDouble("lat");  

  } catch (JSONException e) {  
      // TODO Auto-generated catch block  
      e.printStackTrace();  
  }  

  return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));  

}  

  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
         }
         }
        )
    .show();
  }

}
