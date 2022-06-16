package com.app.nearbyplaces

import org.json.JSONObject
import org.json.JSONException
import org.json.JSONArray
import java.util.ArrayList
import java.util.HashMap

class DataParser {
    private fun getSingleNearbyPlace(googlePlaceJSON: JSONObject): HashMap<String, String> {
        val googlePlaceMap = HashMap<String, String>()
        var NameOfPlace = "-NA-"
        var vicinity = "-NA-"
        var latitude = ""
        var longitude = ""
        var reference = ""
        var icon=""
        try {
            if (!googlePlaceJSON.isNull("name")) {
                NameOfPlace = googlePlaceJSON.getString("name")
            }
            if (!googlePlaceJSON.isNull("vicinity")) {
                vicinity = googlePlaceJSON.getString("vicinity")
            }
            if (!googlePlaceJSON.isNull("icon")) {
               icon=googlePlaceJSON.getString("icon")
            }
            latitude =
                googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat")
            longitude =
                googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng")
            reference = googlePlaceJSON.getString("reference")
            googlePlaceMap["place_name"] = NameOfPlace
            googlePlaceMap["vicinity"] = vicinity
            googlePlaceMap["lat"] = latitude
            googlePlaceMap["lng"] = longitude
            googlePlaceMap["reference"] = reference
            googlePlaceMap["icon"] = icon
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return googlePlaceMap
    }

    private fun getAllNearbyPlaces(jsonArray: JSONArray?): List<HashMap<String, String>?> {
        val counter = jsonArray!!.length()
        val NearbyPlacesList: MutableList<HashMap<String, String>?> = ArrayList()
        var NearbyPlaceMap: HashMap<String, String>? = null
        for (i in 0 until counter) {
            try {
                NearbyPlaceMap = getSingleNearbyPlace(jsonArray[i] as JSONObject)
                NearbyPlacesList.add(NearbyPlaceMap)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return NearbyPlacesList
    }

    fun parse(jSONdata: String?): List<HashMap<String, String>?> {
        var jsonArray: JSONArray? = null
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(jSONdata)
            jsonArray = jsonObject.getJSONArray("results")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return getAllNearbyPlaces(jsonArray)
    }
}