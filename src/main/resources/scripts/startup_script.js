// src/main/resources/scripts/startup_script.js

// Define global variables
var appName = "My Custom Banking App";
var version = "1.0.0";

// Define global functions
function getFormattedTime() {
    const now = new Date();
    return now.toLocaleTimeString();
}

// Access a Java class directly
const GlobalConfigClass = Java.type('com.earthworm.bms.config.GlobalConfig');

// Create an instance of the Java class
const config = new GlobalConfigClass(5000, true);

console.log("Startup script loaded: appName=" + appName + ", version=" + version);
console.log("Global Config from Java: " + config.toString());
