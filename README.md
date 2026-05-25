# Pouring-System

> ⚠️ **Project Note**
> This project is calibrated for a specific cup with a fixed empty weight and volume. Therefore, the system may not work correctly if a different cup is used.

## 📂 Project Structure

* `build/web`, `dist`, `nbproject`, `src`, `web`, and `build.xml`: Contain the components of the Java Web application.
* `SmartWaterAuditDB.sql` & `smartwaterauditdb_additional.sql`: Script files used for Microsoft SQL Server / SQL Server Management Studio (SSMS).
* `pouring_system.cpp`: Device-side source code, running on Arduino/ESP32 microcontrollers.

---

## 🛠 Library Setup Instructions (Dependencies)

This project uses the Ant build system (not Maven), so dependency management is handled manually. All required `.jar` files (including GSON, Weka, SQL Server JDBC, HikariCP, SLF4J, etc.) are pre-packaged in the `Library Pouring System` folder.

To run the project without compilation errors (e.g., *"Cannot find symbol"*), you need to perform the following 2 steps:

### Step 1: Copy Libraries to the Web Folder

Copy all `.jar` files from the `Library Pouring System` folder and paste them into your project's `web/WEB-INF/lib` (or `build/web/WEB-INF/lib`) directory. This ensures the Tomcat server recognizes the libraries during runtime.

### Step 2: Configure Your IDE (NetBeans/Eclipse)

**For NetBeans:**

1. In the `Projects` pane (on the left), right-click the **Libraries** folder.
2. Select **Add JAR/Folder...**
3. Highlight all `.jar` files in the `Library Pouring System` folder and click **Open**.

**For Eclipse:**

1. Right-click the Project name ➔ **Build Path** ➔ **Configure Build Path...**
2. Switch to the **Libraries** tab.
3. Click **Add External JARs...** and add all the `.jar` files.

---

## 🌐 Network Mechanism & Host IP Configuration

This system operates on a local area network (LAN / Wi-Fi). The ESP32 board and the server computer (hosting Tomcat & SQL Server) **must be connected to the same Wi-Fi network** (alternatively, the ESP32 can act as an Access Point for the computer to connect to).

**Communication Flow:**
`ESP32 (Client)` ➔ *sends data via Wi-Fi to the computer's IP* ➔ `Java Web Server (Tomcat)` ➔ *saves to* `Database (SQL Server)`.

### 🔄 When do you need to change the IP?

If you move the project to another computer, or if your Wi-Fi network assigns a new IP address to your machine (e.g., jumping from `192.168.4.2` to `192.168.4.3`), the system will lose connection. The ESP32 won't be able to find the Java Server, and the Java Server might not be able to connect to the Database.

### Steps to get the new IP and Update the Code:

**Step 1: Find the IPv4 address of the Host machine (the computer running the Web & Database)**

1. Press `Windows + R`, type `cmd`, and press **Enter**.
2. Type `ipconfig` and look for the **IPv4 Address** line under your Wi-Fi adapter section (e.g., `192.168.4.3`). This is your Host's new IP.

**Step 2: Update the IP in the Java Backend (`DBContext.java`)**
Open the `src/java/utils/DBContext.java` file. Locate the `PRIMARY_DB_URL` configuration line and replace the old IP segment with your new IP.

```java
// Change the IP in the jdbc:sqlserver://[NEW_IP]:1433... section
private static final String PRIMARY_DB_URL = "jdbc:sqlserver://192.168.4.3:1433;databaseName=SmartWaterAuditDB;encrypt=true;trustServerCertificate=true;";

```

**Step 3: Update the IP in the ESP32 Firmware (`pouring_system.cpp`)**
The ESP32 needs to know the destination to send data. Open the C++ source code file, find the server address definition variable (usually at the top of the file), and change the IP to match the IPv4 address you found in Step 1. (Note: The default Tomcat port is usually `8080`).

```cpp
// Example: updating IP from .2 to .3
const char* serverUrl = "http://192.168.4.3:8080/SmartWaterAuditDB/api/pour-session/batch"; 

```
