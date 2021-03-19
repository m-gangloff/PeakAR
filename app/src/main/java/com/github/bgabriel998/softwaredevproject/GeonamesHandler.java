package com.github.bgabriel998.softwaredevproject;


import android.os.AsyncTask;
import android.util.Log;

import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.UserPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.stream.Collectors;



public abstract class GeonamesHandler extends AsyncTask<Object,Void,Object> implements GeonamesHandlerIF{

    //Query Constants
    private static final int DEFAULT_RANGE_IN_KM = 20;
    private static final int DEFAULT_QUERY_MAX_RESULT = 300;
    private static final int DEFAULT_QUERY_TIMEOUT = 10;

    //List containing query POI's
    private ArrayList<POI> POIs;

    // API used to retrieve peaks POI
    private final OverpassAPIProvider poiProvider;

    private final UserPoint userLocation;
    private final double rangeInKm;
    private final int queryMaxResults;
    private final int queryTimeout;
    private String queryUrl;

    /**
     * Initializes provider
     * @param userLocation UserPoint containing user location inforamtions
     */
    public GeonamesHandler(UserPoint userLocation) {
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");
        this.userLocation = userLocation;
        this.poiProvider = new OverpassAPIProvider();
        this.POIs = new ArrayList<POI>();
        this.rangeInKm = DEFAULT_RANGE_IN_KM;
        this.queryMaxResults = DEFAULT_QUERY_MAX_RESULT;
        this.queryTimeout = DEFAULT_QUERY_TIMEOUT;
    }

    /**
     * Class constructor.
     * Initialises query parameters and OverPassAPIProvider
     * Initialises result array list
     * @param userLocation user location (center of the query bounding box)
     * @param boundingBoxRangeKm range around the user location to compute the bounding box
     * @param queryMaxResults max results that the query should return (please do not exceed 500)
     * @param queryTimeout query timeout
     */
    public GeonamesHandler(UserPoint userLocation, double boundingBoxRangeKm, int queryMaxResults, int queryTimeout){
        if(userLocation == null)
            throw new IllegalArgumentException("UserPoint user location can't be null");
        if(boundingBoxRangeKm <= 0.1)
            throw new IllegalArgumentException("BoundingBoxRangeKm can't be null or negative (also not under 100m)");
        if(queryMaxResults < 1)
            throw new IllegalArgumentException("QueryMaxResult parameter can't be less than 1");
        if(queryTimeout <= 1)
            throw new IllegalArgumentException("QueryTimeout parameter can't be less than 1 sec");

        this.userLocation = userLocation;
        this.rangeInKm = boundingBoxRangeKm;
        this.queryMaxResults = queryMaxResults;
        this.queryTimeout = queryTimeout;
        this.poiProvider = new OverpassAPIProvider();
        this.POIs = new ArrayList<POI>();
    }



    /**
     * onPreExecute method.
     * Setup bounding box for the POI query
     * Creates the url query
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BoundingBox boundingBox = userLocation.computeBoundingBox(rangeInKm);
        queryUrl = poiProvider.urlForTagSearchKml("natural=peak", boundingBox,queryMaxResults,queryTimeout);
    }

    /**
     * See onPostExecute comments
     * @param o
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if(o != null) {
            //Filter out POI where the name or altitude is null
            POIs = ((ArrayList<POI>) o).stream().filter(point -> point.mType != null && point.mLocation.getAltitude() != 0).collect(Collectors.toCollection(ArrayList::new));
            onResponseReceived(POIs);
        }
        else onResponseReceived(null);
    }

    /**
     * See doInBackground comments
     * @param objects nothing
     * @return list of POI
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        return getPOIsFromUrl(queryUrl);
    }


    /**
     * Search for POI.
     * @param url full URL request, built with #urlForPOISearch or equivalent.
     * Main requirements: <br>
     * - Content must be in JSON format<br>
     * - ways and relations must contain the "center" element. <br>
     * @return elements as a list of POI
     */

    private ArrayList<POI> getPOIsFromUrl(String url){
        Log.d(BonusPackHelper.LOG_TAG, "OverpassAPIProvider:getPOIsFromUrl:"+url);
        String jString = BonusPackHelper.requestStringFromUrl(url);
        if (jString == null) {
            Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: request failed.");
            return null;
        }
        try {
            //parse JSON and build POIs
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(jString);
            JsonObject jResult = json.getAsJsonObject();
            JsonArray jElements = jResult.get("elements").getAsJsonArray();
            ArrayList<POI> pois = new ArrayList<POI>(jElements.size());
            for (JsonElement j:jElements){
                JsonObject jo = j.getAsJsonObject();
                POI poi = new POI(POI.POI_SERVICE_OVERPASS_API);
                poi.mId = jo.get("id").getAsLong();
                poi.mCategory = jo.get("type").getAsString();
                if (jo.has("tags")){
                    JsonObject jTags = jo.get("tags").getAsJsonObject();
                    poi.mType = tagValueFromJson("name", jTags);
                    //Try to set a relevant POI type by searching for an OSM commonly used tag key, and getting its value:
                    poi.mDescription = //tagValueFromJsonNotNull("amenity", jTags)
                            /*+ tagValueFromJsonNotNull("boundary", jTags)
                            + tagValueFromJsonNotNull("building", jTags)
                            + tagValueFromJsonNotNull("craft", jTags)
                            + tagValueFromJsonNotNull("emergency", jTags)
                            + tagValueFromJsonNotNull("highway", jTags)
                            + tagValueFromJsonNotNull("historic", jTags)
                            + tagValueFromJsonNotNull("landuse", jTags)
                            + tagValueFromJsonNotNull("leisure", jTags)*/
                             tagValueFromJsonNotNull("natural", jTags);
                            /*+ tagValueFromJsonNotNull("shop", jTags)
                            + tagValueFromJsonNotNull("sport", jTags)
                            + tagValueFromJsonNotNull("tourism", jTags);*/
                    //remove first "," (quite ugly, I know)
                    if (poi.mDescription.length()>0)
                        poi.mDescription = poi.mDescription.substring(1);
                    //We could try to replicate Nominatim/lib/lib.php/getClassTypes(), but it sounds crazy for the added value.
                    poi.mUrl = tagValueFromJson("website", jTags);

                }
                if ("node".equals(poi.mCategory)){
                    poi.mLocation = geoPointFromJson(jo);
                }
                if (poi.mLocation != null)
                    pois.add(poi);
            }
            return pois;
        } catch (JsonSyntaxException e) {
            Log.e(BonusPackHelper.LOG_TAG, "OverpassAPIProvider: parsing error.");
            return null;
        }
    }

    /**
     * Extract value identified by key of JSON
     * @param key key tag for value extraction
     * @param jTags JSON object containing tags
     * @return Value
     */
    private String tagValueFromJson(String key, JsonObject jTags){
        JsonElement jTag = jTags.get(key);
        if (jTag == null)
            return null;
        else
            return jTag.getAsString();
    }

    /**
     * Extract value identified by key of JSON with null handling
     * @param key key tag for value extraction
     * @param jTags JSON object containing tags
     * @return Value
     */
    private String tagValueFromJsonNotNull(String key, JsonObject jTags){
        String v = tagValueFromJson(key, jTags);
        return (v != null ? ","+v : "");
    }

    /**
     * Create location Geopoint ot of Json
     * @param jLatLon JSON object containing lat and long
     * @return Location of POI
     */
    private GeoPoint geoPointFromJson(JsonObject jLatLon){
        double lat = jLatLon.get("lat").getAsDouble();
        double lon = jLatLon.get("lon").getAsDouble();
        String eleStr = (tagValueFromJsonNotNull("ele",jLatLon.get("tags").getAsJsonObject())).replace(",","");
        if(eleStr.isEmpty())
            return new GeoPoint(lat, lon);
        double alt = Double.parseDouble(eleStr);
        return new GeoPoint(lat, lon,alt);
    }

    /**
     * Callback function called when POI list is received
     * @param result ArrayList containing POI
     */
    public abstract void onResponseReceived(Object result);
}
