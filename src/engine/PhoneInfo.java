package engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;
import android.util.Log;

/**
* retrieve phone info
*
* 
*/
public class PhoneInfo {
        private static final String TAG = PhoneInfo.class.getSimpleName();
        private static final String FILE_MEMORY = "/proc/meminfo";
        private static final String FILE_CPU = "/proc/cpuinfo";
        public String mIMEI;
        public int mPhoneType;
        public int mSysVersion;
        public String mNetWorkCountryIso;
        public String mNetWorkOperator;
        public String mNetWorkOperatorName;
        public int mNetWorkType;
        public boolean mIsOnLine;
        public String mConnectTypeName;
        public long mFreeMem;
        public long mTotalMem;
        public String mCupInfo;
        public String mProductName;
        public String mModelName;
        public String mManufacturerName;
        public int mCallState;
        public int mDataActivity;
        public int mDataState;
        public String mDeviceSoftwareVersion;
        public String mLine1Number;
        public String mSimCountryIso;
        public String mSimOperator;
        public String mSimOperatorName;
        public String mSimSerialNumber;
        public int mSimState;
        public String mSubscriberId;
        public String mVoiceMailAlphaTag;
        public String mVoiceMailNumber;
        public int mLac;
        public int mCi;
        public int mHeight;
        public int mWidth;
        public String mApps;
        public String mScanResults;
        public String mBSSID;
        public String mMacAddress;
        public String mWifiInfo;
        public int mIPAddress;
        public int mNetworkId;
        public String mNeighboringCellInfo;
        public String mCurrentTime;
        /**
         * private constructor
         */
        private PhoneInfo() {

        }
        
        
        public static String getTime(){
        SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss     ");     
        Date   curDate   =   new   Date(System.currentTimeMillis());
        //获取当前时间    
        String   str   =   formatter.format(curDate);
		return str;  
        }
        
        public static String getNeighboringCellInfo(Context context){
            TelephonyManager manager = (TelephonyManager) context
                    .getSystemService(Activity.TELEPHONY_SERVICE);
            List<NeighboringCellInfo> infos=manager.getNeighboringCellInfo();
            StringBuilder str=new StringBuilder();
	    	for (int i = 0; i < infos.size(); i++) {  
	    		 
	    			 NeighboringCellInfo thisCell = infos.get(i);  
	    		     int thisNeighCID = thisCell.getCid();  
	    		     int thisNeighLAC = thisCell.getLac();
	    		     int thisNeighRSSI = thisCell.getRssi();
	    		     str.append("No."+(i+1)+":"+"\n");
	    		     str.append("LAC:"+thisNeighLAC+"\n");
	    		     str.append("CID:"+thisNeighCID+"\n");
	    		     str.append("RSSI:"+(-113+2*thisNeighRSSI)+"dBm"+"\n");

	    	}

	    		return str.toString();
	        }
        	
        	
        	
        	
      
     
        
        
        
        
        
        public static int getNetworkId(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getNetworkId();
        }
        
        
        
        public static int getIPAddress(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getIPAddress();
        }
        
        
        public static String getWifiInfo(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getWifiInfo();
        }
        
        
        public static String getMacAddress(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getMacAddress();
        }
        
        
        
        public static String getBSSID(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getBSSID();
        }

        
        
        
        
        public static String getScanResults(Context context){
    		WifiAdmin wifiadmin=new WifiAdmin(context);
			return wifiadmin.getScanResult();
        	
        }

        
        
        public static String getAllApp(Context context) {  
            String result = "";  
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);  
            for (PackageInfo i : packages) {  
                if ((i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {  
                    result += i.applicationInfo.loadLabel(context.getPackageManager()).toString() + ",";  
                }  
            }  
            return result.substring(0, result.length() - 1);  
    }
        
        
        
        public static int getHeight(Context context) {
        	DisplayMetrics dm2 = context.getResources().getDisplayMetrics();
            return dm2.heightPixels;
            }
        
        
        
        
        public static int getWidth(Context context) {
        	DisplayMetrics dm2 = context.getResources().getDisplayMetrics();
            return dm2.widthPixels;
            }
        
 
        
        
//        这里只有gsmlocation下的lac+ci的获取
        public static int getLac(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            GsmCellLocation gsm = (GsmCellLocation) manager.getCellLocation();
            return gsm.getLac();
            }
        
        
        
        public static int getCi(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            GsmCellLocation gsm = (GsmCellLocation) manager.getCellLocation();
            return gsm.getCid();
            }
        
        
        
        public static String getSubscriberId(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSubscriberId();
            }
        public static String getVoiceMailAlphaTag(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getVoiceMailAlphaTag();
            }
        public static String getVoiceMailNumber(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getVoiceMailNumber();
            }
        
        
        
        
        
        public static int getSimState(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSimState();
            }
        
        
        
        
        public static String getSimSerialNumber(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSimSerialNumber();
            }
        
        
        public static String getSimOperatorName(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSimOperatorName();
            }
        
        
        
        
        public static String getSimOperator(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSimOperator();
            }
        
        public static String getSimCountryIso(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getSimCountryIso();
            }
        
        
        public static String getLine1Number(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getLine1Number();
            }
        
        
        
        
        public static String getDeviceSoftwareVersion(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getDeviceSoftwareVersion();
            }
        
        
        
        
        
        public static int getDataActivity(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getDataActivity();
            }
        
        
        
        
        public static int getDataState(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getDataState();
            }
        
        
        
        
        
        public static int getCallState(Context context) {
            TelephonyManager manager = (TelephonyManager) context
                            .getSystemService(Activity.TELEPHONY_SERVICE);
            return manager.getCallState();
            }
        
        
        
        

        /**
         * get imei
         * 
         * @return
         */
        public static String getIMEI(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                // check if has the permission
                if (PackageManager.PERMISSION_GRANTED == context.getPackageManager()
                                .checkPermission(Manifest.permission.READ_PHONE_STATE,
                                                context.getPackageName())) {
                        return manager.getDeviceId();
                } else {
                        return null;
                }
        }

        /**
         * get phone type,like :GSM??CDMA??SIP??NONE
         * 
         * @param context
         * @return
         */
        public static int getPhoneType(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                return manager.getPhoneType();
        }

        /**
         * get phone sys version
         * 
         * @return
         */
        public static int getSysVersion() {
                return Build.VERSION.SDK_INT;
        }

        /**
         * Returns the ISO country code equivalent of the current registered
         * operator's MCC (Mobile Country Code).
         * 
         * @param context
         * @return
         */
        public static String getNetWorkCountryIso(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                return manager.getNetworkCountryIso();
        }

        /**
         * Returns the numeric name (MCC+MNC) of current registered operator.may not
         * work on CDMA phone
         * 
         * @param context
         * @return
         */
        public static String getNetWorkOperator(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                return manager.getNetworkOperator();
        }

        /**
         * Returns the alphabetic name of current registered operator.may not work
         * on CDMA phone
         * 
         * @param context
         * @return
         */
        public static String getNetWorkOperatorName(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                return manager.getNetworkOperatorName();
        }

        /**
         * get type of current network
         * 
         * @param context
         * @return
         */
        public static int getNetworkType(Context context) {
                TelephonyManager manager = (TelephonyManager) context
                                .getSystemService(Activity.TELEPHONY_SERVICE);
                return manager.getNetworkType();
        }

        /**
         * is webservice aviliable
         * 
         * @param context
         * @return
         */
        public static boolean isOnline(Context context) {
                ConnectivityManager manager = (ConnectivityManager) context
                                .getSystemService(Activity.CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                        return true;
                }
                return false;
        }

        /**
         * get current data connection type name ,like ,Mobile??WIFI??OFFLINE
         * 
         * @param context
         * @return
         */
        public static String getConnectTypeName(Context context) {
                if (!isOnline(context)) {
                        return "OFFLINE";
                }
                ConnectivityManager manager = (ConnectivityManager) context
                                .getSystemService(Activity.CONNECTIVITY_SERVICE);
                NetworkInfo info = manager.getActiveNetworkInfo();
                if (info != null) {
                        return info.getTypeName();
                } else {
                        return "OFFLINE";
                }
        }

        /**
         * get free memory of phone, in M
         * 
         * @param context
         * @return
         */
        public static long getFreeMem(Context context) {
                ActivityManager manager = (ActivityManager) context
                                .getSystemService(Activity.ACTIVITY_SERVICE);
                MemoryInfo info = new MemoryInfo();
                manager.getMemoryInfo(info);
                long free = info.availMem / 1024 / 1024;
                return free;
        }

        /**
         * get total memory of phone , in M
         * 
         * @param context
         * @return
         */
        public static long getTotalMem(Context context) {
                try {
                        FileReader fr = new FileReader(FILE_MEMORY);
                        BufferedReader br = new BufferedReader(fr);
                        String text = br.readLine();
                        String[] array = text.split("\\s+");
                        Log.w(TAG, text);
                        return Long.valueOf(array[1]) / 1024;
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return -1;
        }

        public static String getCpuInfo() {
                try {
                        FileReader fr = new FileReader(FILE_CPU);
                        BufferedReader br = new BufferedReader(fr);
                        String text = br.readLine();
                        String[] array = text.split(":\\s+", 2);
                        for (int i = 0; i < array.length; i++) {
                                Log.w(TAG, " .....  " + array[i]);
                        }
                        Log.w(TAG, text);
                        return array[1];
                } catch (FileNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return null;
        }

        /**
         * get product name of phone
         * 
         * @return
         */
        public static String getProductName() {
                return Build.PRODUCT;
        }

        /**
         * get model of phone
         * 
         * @return
         */
        public static String getModelName() {
                return Build.MODEL;
        }

        /**
         * get Manufacturer Name of phone
         * 
         * @return
         */
        public static String getManufacturerName() {
                return Build.MANUFACTURER;
        }

        public static PhoneInfo getPhoneInfo(Context context) {
                PhoneInfo result = new PhoneInfo();
                result.mCurrentTime=getTime();
                result.mNeighboringCellInfo=getNeighboringCellInfo(context);
                result.mNetworkId=getNetworkId(context);
                result.mIPAddress=getIPAddress(context);
                result.mWifiInfo=getWifiInfo(context);
                result.mMacAddress=getMacAddress(context);
                result.mBSSID=getBSSID(context);
                result.mScanResults=getScanResults(context);
                result.mApps=getAllApp(context);
                result.mHeight=getHeight(context);
                result.mWidth=getWidth(context);
                result.mLac=getLac(context);
                result.mCi=getCi(context);
                result.mSubscriberId=getSubscriberId(context);
                result.mVoiceMailAlphaTag=getVoiceMailAlphaTag(context);
                result.mVoiceMailNumber=getVoiceMailNumber(context);
                result.mSimState=getSimState(context);
                result.mSimSerialNumber=getSimSerialNumber(context);
                result.mSimOperatorName=getSimOperatorName(context);
                result.mSimOperator=getSimOperator(context);
                result.mSimCountryIso=getSimCountryIso(context);
                result.mLine1Number=getLine1Number(context);
                result.mDeviceSoftwareVersion=getDeviceSoftwareVersion(context);
                result.mDataActivity=getDataActivity(context);
                result.mDataState=getDataState(context);
                result.mCallState=getCallState(context);
                result.mIMEI = getIMEI(context);
                result.mPhoneType = getPhoneType(context);
                result.mSysVersion = getSysVersion();
                result.mNetWorkCountryIso = getNetWorkCountryIso(context);
                result.mNetWorkOperator = getNetWorkOperator(context);
                result.mNetWorkOperatorName = getNetWorkOperatorName(context);
                result.mNetWorkType = getNetworkType(context);
                result.mIsOnLine = isOnline(context);
                result.mConnectTypeName = getConnectTypeName(context);
                result.mFreeMem = getFreeMem(context);
                result.mTotalMem = getTotalMem(context);
                result.mCupInfo = getCpuInfo();
                result.mProductName = getProductName();
                result.mModelName = getModelName();
                result.mManufacturerName = getManufacturerName();
                return result;
        }

        @Override
        public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("mCurrentTime : "+mCurrentTime+"\n");
                builder.append("mNeighboringCellInfo : "+"\n"+mNeighboringCellInfo+"\n");
                builder.append("mNetworkId : "+mNetworkId+"\n");
                builder.append("mIPAddress : "+mIPAddress+"\n");
                builder.append("mWifiInfo : "+mWifiInfo+"\n");
                builder.append("mMacAddress : "+mMacAddress+"\n");
                builder.append("mBSSID : "+mBSSID+"\n");
                builder.append("mScanResults : "+"\n"+mScanResults+"\n");
                builder.append("mApps : "+mApps+"\n");
                builder.append("mHeight : "+mHeight+"\n");
                builder.append("mWidth : "+mWidth+"\n");
                builder.append("mLac : "+mLac+"\n");
                builder.append("mCi : "+mCi+"\n");
                builder.append("mSubscriberId : "+mSubscriberId+"\n");
                builder.append("mVoiceMailAlphaTag : "+mVoiceMailAlphaTag+"\n");
                builder.append("mVoiceMailNumber : "+mVoiceMailNumber+"\n");
                builder.append("mSimState : "+mSimState+"\n");
                builder.append("mSimSerialNumber : "+mSimSerialNumber+"\n");
                builder.append("mSimOperatorName : "+mSimOperatorName+"\n");
                builder.append("mSimOperator : "+mSimOperator+"\n");
                builder.append("mSimCountryIso : "+mSimCountryIso+"\n");
                builder.append("mLine1Number : "+mLine1Number+"\n");
                builder.append("mDeviceSoftwareVersion : "+mDeviceSoftwareVersion+"\n");
                builder.append("mDataActivity : "+mDataActivity+"\n");
                builder.append("mDatamDataState : "+mDataState+"\n");
                builder.append("mCallState : "+mCallState+"\n");
                builder.append("IMEI : "+mIMEI+"\n");
                builder.append("mPhoneType : "+mPhoneType+"\n");
                builder.append("mSysVersion : "+mSysVersion+"\n");
                builder.append("mNetWorkCountryIso : "+mNetWorkCountryIso+"\n");
                builder.append("mNetWorkOperator : "+mNetWorkOperator+"\n");
                builder.append("mNetWorkOperatorName : "+mNetWorkOperatorName+"\n");
                builder.append("mNetWorkType : "+mNetWorkType+"\n");
                builder.append("mIsOnLine : "+mIsOnLine+"\n");
                builder.append("mConnectTypeName : "+mConnectTypeName+"\n");
                builder.append("mFreeMem : "+mFreeMem+"M\n");
                builder.append("mTotalMem : "+mTotalMem+"M\n");
                builder.append("mCupInfo : "+mCupInfo+"\n");
                builder.append("mProductName : "+mProductName+"\n");
                builder.append("mModelName : "+mModelName+"\n");
                builder.append("mManufacturerName : "+mManufacturerName+"\n");
                return builder.toString();
        }
        
}
