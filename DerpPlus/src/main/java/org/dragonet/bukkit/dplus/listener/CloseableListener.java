package org.dragonet.bukkit.dplus.listener;

import org.bukkit.event.Listener;

/**
 * Created on 2017/11/13.
 */
public interface CloseableListener extends Listener {

    void close();

}
