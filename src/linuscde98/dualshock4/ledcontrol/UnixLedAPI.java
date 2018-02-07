package linuscde98.dualshock4.ledcontrol;

import java.io.File;
import java.util.ArrayList;

public class UnixLedAPI {

	private static String DS4_VENDOR = "054c"; // Vendorid for Sony
	private static String DS4_PRODUCT = "05c4"; // Productid for the Dualshock4
	private static File FOLDER_LEDS = new File("/sys/class/leds");
	
	/**
	 * Gets system-interal ids of all DS4-Controllers
	 * @return Array of device-ids (or short devids) Format is: XXXX:054C:05C4.XXXX (X = numbers in HEX, remaining are vendorid and productid)
	 */
	public static String[] listControllers(){
		ArrayList<String> deviceIds = new ArrayList<String>();
		
		if(!FOLDER_LEDS.exists()) return new String[]{};
		
		for(File f : FOLDER_LEDS.listFiles()){
			
			if(f.isFile()) continue;
			if(!f.getName().contains(":")) continue;
			
			String deviceId = f.getName().substring(0, f.getName().lastIndexOf(":"));

			if(!deviceIds.contains(deviceId) && isValidDualshock4(deviceId)) deviceIds.add(deviceId);
		}
		
		return deviceIds.toArray(new String[]{});
	}
	
	/**
	 * Checks if device is a valid DS4-Controller
	 * @param deviceId Id of controller (see listControllers() for more)
	 * @return Whether it is a DS4-Controller or not
	 */
	public static boolean isValidDualshock4(String deviceId){

		// Check for existence of all three LED-Folders:
		File redLedFolder = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":red");
		File greenLedFolder = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":green");
		File blueLedFolder = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":blue");

		if(!redLedFolder.exists() || !greenLedFolder.exists() || !blueLedFolder.exists() ||
				!redLedFolder.isDirectory() || !greenLedFolder.isDirectory() || !blueLedFolder.isDirectory())
			return false;

		// Get Input-Device (which contains vendor- and product-information)
		File inputDir = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":red/device/input");
		
		if(!inputDir.exists() || !inputDir.isDirectory()) return false;
		
		String[] inputDevices = inputDir.list();
		String inputDevName = inputDevices.length == 0 ? null : inputDevices[0];
		if(inputDevName == null) return false;

		// Get Vendor and Prodoct ids:
		File vendorFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":red/device/input/" + inputDevName + "/id/vendor");
		File productFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":red/device/input/" + inputDevName + "/id/product");
		if(!vendorFile.exists() || !productFile.exists() || vendorFile.isDirectory() || productFile.isDirectory())
			return false;

		String[] vendorContent = ReadFile.read(vendorFile);
		String[] productContent = ReadFile.read(productFile);
		if(vendorContent == null || productContent == null || vendorContent.length == 0 || productContent.length == 0)
			return false;

		String vendor = vendorContent[0], product = productContent[0];
		if(!vendor.equals(DS4_VENDOR) || !product.equals(DS4_PRODUCT)) return false;
		
		return true;
	}
	
	/**
	 * Checks if program has enough rights to change the led-color of a given device
	 * @param deviceId Deviceid Id of controller (see listControllers() for more)
	 * @return Whether the led-color is changeable (i.e. having write-access for the files that contain the colors)
	 */
	public static boolean isAccessible(String deviceId){
		if(!isValidDualshock4(deviceId)) return false;

		// Color brightness/value files:
		File redValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":red/brightness");
		File greenValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":green/brightness");
		File blueValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceId + ":blue/brightness");
		
		if(!redValueFile.canWrite() || !greenValueFile.canWrite() || !blueValueFile.canWrite()) return false;

		return true;
	}
	
	/**
	 * Writes a color to a given DS4-Controller
	 * @param deviceid Deviceid Id of controller (see listControllers() for more)
	 * @param red The Red-Value of RGB-Color
	 * @param green The Green-Value of RGB-Color
	 * @param blue The Blue-Value of RGB-Color
	 * @return Whether it succeed to change the color
	 */
	public static boolean writeRGB(String deviceid, int red, int green, int blue){
		if(!isValidDualshock4(deviceid) || !isAccessible(deviceid)) return false;

		// Ensure color range between 0 and 255:
		red = Math.max(0, Math.min(red, 255));
		green = Math.max(0, Math.min(green, 255));
		blue = Math.max(0, Math.min(blue, 255));

		// Color brightness/value files:
		File redValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/brightness");
		File greenValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green/brightness");
		File blueValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue/brightness");

		// Write color values:
		WriteFile.write(redValueFile, new String[]{"" + red, ""}, false);
		WriteFile.write(greenValueFile, new String[]{"" + green, ""}, false);
		WriteFile.write(blueValueFile, new String[]{"" + blue, ""}, false);
		
		return true;
	}
	
	/**
	 * Reads the RGB-Color of the DS4-Controller
	 * @param deviceid Deviceid Id of controller (see listControllers() for more)
	 * @return The RGB-Color-Values as an array which is arranged as [0] = Red, [1] = Green, [2] = Blue
	 */
	public static int[] readRGB(String deviceid){
		if(!isValidDualshock4(deviceid)) return null;

		// Color brightness/value files:
		File redValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/brightness");
		File greenValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green/brightness");
		File blueValueFile = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue/brightness");
		
		int r = 0, g = 0, b = 0;
		
		String[] redContent = ReadFile.read(redValueFile);
		String[] greenContent = ReadFile.read(greenValueFile);
		String[] blueContent = ReadFile.read(blueValueFile);
		
		try{

			// Read rgb-values from files:
			if(redContent != null && redContent.length > 0) r = Integer.parseInt(redContent[0]);
			if(greenContent != null && greenContent.length > 0) g = Integer.parseInt(greenContent[0]);
			if(blueContent != null && blueContent.length > 0) b = Integer.parseInt(blueContent[0]);

			return new int[]{r, g, b};

		}catch(Exception ignored){}
		
		return null;
	}
	
	/**
	 * Returns the Battery-Capacity
	 * @param deviceid  Deviceid Id of controller (see listControllers() for more)
	 * @return Battery-Capacity in percent
	 */
	public static int getBatteryCapacity(String deviceid){
		if(!isValidDualshock4(deviceid)) return -1;

		File powerSupplyFolder = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/device/power_supply");
		if(!powerSupplyFolder.exists() || !powerSupplyFolder.isDirectory()) return -1;

		File[] powerSupplies = powerSupplyFolder.listFiles();
		String powerSupplyName = powerSupplies == null || powerSupplies.length == 0 ? null : powerSupplies[0].getName();
		if(powerSupplyName == null) return -1;
		
		File capacityFile = new File(powerSupplyFolder.getAbsolutePath() + "/" + powerSupplyName + "/capacity");
		
		String[] capacityContent = ReadFile.read(capacityFile);
		
		try{
		
			int capacity = -1;
			if(capacityContent != null && capacityContent.length > 0) capacity = Integer.parseInt(capacityContent[0]);
		
			return capacity;

		}catch(Exception ignored){}
		
		return -1;
	}
	
}
