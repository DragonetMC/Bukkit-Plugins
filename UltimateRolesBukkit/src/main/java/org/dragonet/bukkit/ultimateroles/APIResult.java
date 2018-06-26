package org.dragonet.bukkit.ultimateroles;

import com.google.gson.JsonObject;

/**
 * Created on 2017/11/16.
 */
public class APIResult {

    public final String status;
    public final String message;

    public final JsonObject data;

    public APIResult(String status, String message, JsonObject data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
