package com.mapbox.services.android.navigation.ui.v5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.directions.v5.DirectionsAdapterFactory;
import com.mapbox.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;

import java.util.HashMap;

/**
 * Use this class to launch the navigation UI
 * <p>
 * You can launch the UI with either a route you have already retrieved from
 * {@link com.mapbox.services.android.navigation.v5.navigation.NavigationRoute} or you can pass a
 * {@link Point} origin and {@link Point} destination and the UI will request the {@link DirectionsRoute}
 * while initializing.
 * </p><p>
 * You have an option to include a AWS Cognito Pool ID, which will initialize the UI with AWS Polly Voice instructions
 * </p><p>
 * For testing, you can launch with simulation, in which our
 * {@link com.mapbox.services.android.location.MockLocationEngine} will begin
 * following the given {@link DirectionsRoute} once the UI is initialized
 * </p>
 */
public class NavigationLauncher {

  /**
   * Starts the UI with a {@link DirectionsRoute} already retrieved from
   * {@link com.mapbox.services.android.navigation.v5.navigation.NavigationRoute}
   *
   * @param activity must be launched from another {@link Activity}
   * @param route    initial route in which the navigation will follow
   * @param options  with fields to customize the navigation view
   */
  public static void startNavigation(Activity activity, NavigationViewOptions options) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    SharedPreferences.Editor editor = preferences.edit();

    if (options.directionsRoute() != null) {
      storeDirectionsRouteValue(options, editor);
    } else if (options.origin() != null && options.destination() != null) {
      storeCoordinateValues(options, editor);
    } else {
      throw new RuntimeException("A valid DirectionsRoute or origin and "
        + "destination must be provided in NavigationViewOptions");
    }

    editor.putString(NavigationConstants.NAVIGATION_VIEW_AWS_POOL_ID, options.awsPoolId());
    editor.putBoolean(NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE, options.shouldSimulateRoute());
    editor.putInt(NavigationConstants.NAVIGATION_VIEW_UNIT_TYPE, options.unitType());
    editor.apply();

    Intent navigationActivity = new Intent(activity, NavigationActivity.class);
    activity.startActivity(navigationActivity);
  }

  /**
   * Used to extract the route used to launch the drop-in UI.
   * <p>
   * Extracts the route {@link String} from {@link SharedPreferences} and converts
   * it back to a {@link DirectionsRoute} object with {@link Gson}.
   *
   * @param context to retrieve {@link SharedPreferences}
   * @return {@link DirectionsRoute} stored when launching
   */
  static DirectionsRoute extractRoute(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    String directionsRoute = preferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "");
    return new GsonBuilder().registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create()
      .fromJson(directionsRoute, DirectionsRoute.class);
  }

  /**
   * Used to extract the origin and position coordinates used to launch
   * the drop-in UI.
   * <p>
   * A {@link HashMap} is used to ensure the correct coordinate is
   * extracted in the {@link NavigationView} with the defined constants.
   *
   * @param context to retrieve {@link SharedPreferences}
   * @return map with both origin and destination coordinates
   */
  static HashMap<String, Point> extractCoordinates(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    double originLng = Double.longBitsToDouble(preferences
      .getLong(NavigationConstants.NAVIGATION_VIEW_ORIGIN_LNG_KEY, 0));
    double originLat = Double.longBitsToDouble(preferences
      .getLong(NavigationConstants.NAVIGATION_VIEW_ORIGIN_LAT_KEY, 0));
    double destinationLng = Double.longBitsToDouble(preferences
      .getLong(NavigationConstants.NAVIGATION_VIEW_DESTINATION_LNG_KEY, 0));
    double destinationLat = Double.longBitsToDouble(preferences
      .getLong(NavigationConstants.NAVIGATION_VIEW_DESTINATION_LAT_KEY, 0));

    Point origin = Point.fromLngLat(originLng, originLat);
    Point destination = Point.fromLngLat(destinationLng, destinationLat);

    HashMap<String, Point> coordinates = new HashMap<>();
    coordinates.put(NavigationConstants.NAVIGATION_VIEW_ORIGIN, origin);
    coordinates.put(NavigationConstants.NAVIGATION_VIEW_DESTINATION, destination);
    return coordinates;
  }

  private static void storeDirectionsRouteValue(NavigationViewOptions options, SharedPreferences.Editor editor) {
    editor.putString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, new GsonBuilder()
      .registerTypeAdapterFactory(DirectionsAdapterFactory.create()).create().toJson(options.directionsRoute()));
  }

  private static void storeCoordinateValues(NavigationViewOptions options, SharedPreferences.Editor editor) {
    editor.putLong(NavigationConstants.NAVIGATION_VIEW_ORIGIN_LAT_KEY,
      Double.doubleToRawLongBits(options.origin().latitude()));
    editor.putLong(NavigationConstants.NAVIGATION_VIEW_ORIGIN_LNG_KEY,
      Double.doubleToRawLongBits(options.origin().longitude()));
    editor.putLong(NavigationConstants.NAVIGATION_VIEW_DESTINATION_LAT_KEY,
      Double.doubleToRawLongBits(options.destination().latitude()));
    editor.putLong(NavigationConstants.NAVIGATION_VIEW_DESTINATION_LNG_KEY,
      Double.doubleToRawLongBits(options.destination().longitude()));
  }
}
