package com.earthworm.bms.service;

import org.springframework.stereotype.Service;

@Service
public class GlobalJsScopeService {

    private String startupScript = "";

    public String getStartupScript() {
        return startupScript;
    }

    public void setStartupScript(String startupScript) {
        this.startupScript = startupScript;
    }
}
