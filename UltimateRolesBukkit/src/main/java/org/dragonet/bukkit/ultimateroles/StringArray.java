package org.dragonet.bukkit.ultimateroles;

/**
 * Created on 2017/11/16.
 */
public final class StringArray {

    public static String implode(String glue, String[] pieces) {
        if (pieces == null || pieces.length == 0) return "";
        if (glue == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pieces.length; i++) {
            sb.append(pieces[i]);
            if(i < pieces.length - 1) sb.append(glue);
        }
        return sb.toString();
    }

}
