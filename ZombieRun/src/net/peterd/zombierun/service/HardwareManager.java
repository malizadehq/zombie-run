package net.peterd.zombierun.service;

import java.util.HashSet;
import java.util.Set;

import net.peterd.zombierun.R;
import net.peterd.zombierun.constants.Constants;
import net.peterd.zombierun.game.GameEvent;
import net.peterd.zombierun.util.FloatingPointGeoPoint;
import net.peterd.zombierun.util.GeoPointUtil;
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import net.peterd.zombierun.util.Log;

public class HardwareManager implements GameEventListener, LocationListener {
  
  private Vibrator vibrator;
  private LocationManager locationManager;
  private String bestLocationProvider;
  
  public HardwareManager(Activity activity) {
    locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
    vibrator = (Vibrator) activity.getSystemService(Activity.VIBRATOR_SERVICE);
  }
  
  /**
   * Initialize the various hardware systems that the ZombieRun game needs.
   * 
   * @param activity the {@link Activity} in which the game is running.
   * @return A message id if there was an error, else null.
   */
  public Integer initializeHardware() {
    Integer errorMessage = null;
    errorMessage = initializeLocationManager();
    if (errorMessage != null) {
      return errorMessage;
    }
    
    errorMessage = initializeVibrator();
    if (errorMessage != null) {
      return errorMessage;
    }
    return null;
  }
  
  /**
   * Deregister this Hardware Manager's hooks into the various services the system provides.
   */
  public void deregisterManager() {
    locationManager.removeUpdates(this);
  }
  
  private Integer initializeLocationManager() {
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setAltitudeRequired(true);
    bestLocationProvider = locationManager.getBestProvider(criteria, false);
    if (bestLocationProvider == null) {
      return R.string.error_no_gps;
    } else if (!locationManager.isProviderEnabled(bestLocationProvider)) {
      return R.string.error_gps_disabled;
    }
    return null;
  }
  
  private Integer initializeVibrator() {
    return vibrator == null ? R.string.error_no_vibrator : null;
  }

  public Vibrator getVibrator() {
    return vibrator;
  }

  private final Set<LocationListener> locationListeners = new HashSet<LocationListener>();
  
  public boolean registerLocationListener(LocationListener listener) {
    locationManager.requestLocationUpdates(bestLocationProvider,
        5 * 1000,
        1,
        this);
    return locationListeners.add(listener);
  }
  
  public boolean removeLocationListener(LocationListener listener) {
    return locationListeners.remove(listener);
  }
  
  public FloatingPointGeoPoint getLastKnownLocation() {
    return new FloatingPointGeoPoint(
        GeoPointUtil.fromLocation(
            locationManager.getLastKnownLocation(bestLocationProvider)));
  }
  
  public void receiveEvent(GameEvent event) {
    Vibrator vibrator = getVibrator();
    if (event == GameEvent.GAME_LOSE ||
        event == GameEvent.GAME_QUIT ||
        event == GameEvent.GAME_WIN) {
      locationManager.removeUpdates(this);
      this.locationListeners.clear();
      Log.d("ZombieRun.HardwareManager", "Vibrating on game end.");
      vibrator.vibrate(Constants.onGameEndVibrationTimeMs);
    } else if (event == GameEvent.GAME_PAUSE) {
      locationManager.removeUpdates(this);
    } else if (event == GameEvent.GAME_RESUME ||
        event == GameEvent.GAME_START) {
      locationManager.requestLocationUpdates(bestLocationProvider, 0, 0, this);
    } else if (event == GameEvent.ZOMBIE_NEAR_PLAYER) {
      vibrator.vibrate(Constants.onZombieNearPlayerVibrationTimeMs);
    } else if (event == GameEvent.ZOMBIE_NOTICE_PLAYER) {
      vibrator.vibrate(Constants.onZombieNoticePlayerVibrationTimeMs);
    }
  }

  public void onLocationChanged(Location location) {
    if (Log.loggingEnabled()) {
      Log.d("ZombieRun.HardwareManager", "Received updated location, distributing to " +
          locationListeners.size() + " listeners.");
    }
    for (LocationListener listener : locationListeners) {
      listener.onLocationChanged(location);
    }
  }
  public void onProviderDisabled(String provider) { }
  public void onProviderEnabled(String provider) { }
  public void onStatusChanged(String provider, int status, Bundle extras) { }
}
