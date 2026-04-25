// src/main/resources/scripts/startup_script.kts

// Define global variables
val appName = "My Custom Banking App"
val version = "1.0.0"

// Define global functions
fun getFormattedTime(): String {
    val now = java.time.LocalDateTime.now()
    return now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
}

// You can also define objects or classes here if needed
class GlobalConfig(val maxUsers: Int, val maintenanceMode: Boolean)
val config = GlobalConfig(maxUsers = 1000, maintenanceMode = false)

println("Startup script loaded: appName=$appName, version=$version")
