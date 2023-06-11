package com.exchangee;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class BonjourManagerModule extends ReactContextBaseJavaModule {
  private NsdManager nsdManager;
  private NsdManager.DiscoveryListener discoveryListener;

  public BonjourManagerModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "BonjourManagerModule";
  }

  @ReactMethod
  public void getServiceInfo(final String serviceName, final Callback callback) {
    if (nsdManager == null) {
      nsdManager = (NsdManager) getReactApplicationContext().getSystemService(Context.NSD_SERVICE);
    }

    if (discoveryListener == null) {
      discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
          callback.invoke("Start discovery failed: " + errorCode, null);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
          callback.invoke("Stop discovery failed: " + errorCode, null);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
          // Not used in this example
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
          // Not used in this example
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
          if (serviceInfo.getServiceName().equals(serviceName)) {
            NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
              @Override
              public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                callback.invoke("Resolve failed: " + errorCode, null);
              }

              @Override
              public void onServiceResolved(NsdServiceInfo serviceInfo) {
                callback.invoke(null, getServiceInfoMap(serviceInfo));
              }
            };

            nsdManager.resolveService(serviceInfo, resolveListener);
            nsdManager.stopServiceDiscovery(discoveryListener);
          }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
          // Not used in this example
        }
      };
    }

    nsdManager.discoverServices("tcp-service", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
  }

  private WritableMap getServiceInfoMap(NsdServiceInfo serviceInfo) {
    WritableMap serviceInfoMap = Arguments.createMap();
    serviceInfoMap.putString("serviceName", serviceInfo.getServiceName());
    serviceInfoMap.putString("serviceType", serviceInfo.getServiceType());
    serviceInfoMap.putString("host", serviceInfo.getHost().getHostAddress());
    serviceInfoMap.putInt("port", serviceInfo.getPort());

    // Add more properties as needed

    return serviceInfoMap;
  }
}
