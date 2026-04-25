package com.earthworm.bms.config;

public class GlobalConfig {
    private int maxUsers;
    private boolean maintenanceMode;

    public GlobalConfig(int maxUsers, boolean maintenanceMode) {
        this.maxUsers = maxUsers;
        this.maintenanceMode = maintenanceMode;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    @Override
    public String toString() {
        return "GlobalConfig{maxUsers=" + maxUsers + ", maintenanceMode=" + maintenanceMode + "}";
    }
}
