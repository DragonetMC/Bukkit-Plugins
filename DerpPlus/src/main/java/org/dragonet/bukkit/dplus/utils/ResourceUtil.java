package org.dragonet.bukkit.dplus.utils;

import org.dragonet.bukkit.dplus.DerpPlus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 2017/11/13.
 */
public class ResourceUtil {

    public final static void saveResource(String path, File output, boolean override) {
        if(output.exists() && !override) return;
        try {
            output.getParentFile().mkdirs();
            InputStream in = DerpPlus.class.getResourceAsStream(path);
            OutputStream out = new FileOutputStream(output);
            byte[] buffer = new byte[2048];
            int len;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
