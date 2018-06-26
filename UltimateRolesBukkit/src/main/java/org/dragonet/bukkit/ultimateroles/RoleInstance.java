package org.dragonet.bukkit.ultimateroles;

/**
 * Created on 2017/9/15.
 */
public class RoleInstance {

    public final int roleId;
    public final long endTime;

    public RoleInstance(int roleId, long endTime) {
        this.roleId = roleId;
        this.endTime = endTime;
    }
}
