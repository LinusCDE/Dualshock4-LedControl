package linuscde98.dualshock4.ledcontrol;

import java.io.File;
import java.util.ArrayList;

public class UnixLedAPI {

	private static String DS4_VENDOR = "054c";
	private static String DS4_PRODUCT = "05c4";
	private static File FOLDER_LEDS = new File("/sys/class/leds");
	
	public static String[] listControllers(){
		ArrayList<String> devids = new ArrayList<String>();
		
		if(!FOLDER_LEDS.exists()) return new String[]{};
		
		for(File f : FOLDER_LEDS.listFiles()){
			
			if(f.isFile()) continue;
			if(!f.getName().contains(":")) continue;
			
			String devid = f.getName().substring(0, f.getName().lastIndexOf(":"));
			
			if(!devids.contains(devid) && isValidDualshock4(devid)) devids.add(devid);
		}
		
		return devids.toArray(new String[]{});
	}
	
	public static boolean isValidDualshock4(String deviceid){
		File red = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red");
		File green = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green");
		File blue = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue");
		
		if(!red.exists() || !green.exists() || !blue.exists() || !red.isDirectory() || !green.isDirectory() || !blue.isDirectory())
			return false;
		
		File input_dir = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/device/input");
		
		if(!input_dir.exists() || !input_dir.isDirectory()) return false;
		
		String[] input_devices = input_dir.list();
		String input_devname = input_devices.length != 1 ? null : input_devices[0];
		
		if(input_devname == null) return false;
		
		File f_vendor = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/device/input/" + input_devname + "/id/vendor");
		File f_product = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/device/input/" + input_devname + "/id/product");
		
		if(!f_vendor.exists() || !f_product.exists() || f_vendor.isDirectory() || f_product.isDirectory())
			return false;

		String[] ret_vendor = ReadFile.read(f_vendor);
		String[] ret_product = ReadFile.read(f_product);
		
		if(ret_vendor == null || ret_product == null || ret_vendor.length == 0 || ret_product.length == 0)
			return false;
		
		String vendor = ret_vendor[0], product = ret_product[0];
		
		if(!vendor.equals(DS4_VENDOR) || !product.equals(DS4_PRODUCT)) return false;
		
		return true;
	}
	
	public static boolean isAccessible(String deviceid){
		if(!isValidDualshock4(deviceid)) return false;
		
		File red_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/brightness");
		File green_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green/brightness");
		File blue_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue/brightness");
		
		if(!red_val.canWrite() || !green_val.canWrite() || !blue_val.canWrite()) return false;
		
		return true;
	}
	
	public static boolean writeRGB(String deviceid, int red, int green, int blue){
		if(!isValidDualshock4(deviceid) || !isAccessible(deviceid)) return false;
		
		File red_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/brightness");
		File green_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green/brightness");
		File blue_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue/brightness");
		
		if(red > 255) red = 255; 		if(red < 0) red = 0;
		if(green > 255) green = 255; 	if(green < 0) green = 0;
		if(blue > 255) blue = 255; 		if(blue < 0) blue = 0;
		
		WriteFile.write(red_val, new String[]{"" + red, ""}, false);
		WriteFile.write(green_val, new String[]{"" + green, ""}, false);
		WriteFile.write(blue_val, new String[]{"" + blue, ""}, false);
		
		return true;
	}
	
	public static int[] readRGB(String deviceid){
		if(!isValidDualshock4(deviceid)) return null;
		
		File red_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/brightness");
		File green_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":green/brightness");
		File blue_val = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":blue/brightness");
		
		int r = 0, g = 0, b = 0;
		
		String[] res_red = ReadFile.read(red_val);
		String[] res_green = ReadFile.read(green_val);
		String[] res_blue = ReadFile.read(blue_val);
		
		try{
		
		if(res_red != null && res_red.length > 0) r = Integer.parseInt(res_red[0]);
		if(res_green != null && res_green.length > 0) g = Integer.parseInt(res_green[0]);
		if(res_blue != null && res_blue.length > 0) b = Integer.parseInt(res_blue[0]);	
		
		return new int[]{r, g, b};
		}catch(Exception ex){}
		
		return null;
	}
	
	public static int getBatteryCapacity(String deviceid){
		if(!isValidDualshock4(deviceid)) return -1;
		
		File power_supply_folder = new File(FOLDER_LEDS.getAbsolutePath() + "/" + deviceid + ":red/device/power_supply");
		if(!power_supply_folder.exists() || !power_supply_folder.isDirectory()) return -1;
		File[] power_supplys = power_supply_folder.listFiles();
		String power_name = power_supplys == null || power_supplys.length == 0 ? null : power_supplys[0].getName();
		if(power_name == null) return -1;
		
		File capacity_val = new File(power_supply_folder.getAbsolutePath() + "/" + power_name + "/capacity");
		
		String[] res_capacity = ReadFile.read(capacity_val);
		
		try{
		
		int capacity = -1;
		if(res_capacity != null && res_capacity.length > 0) capacity = Integer.parseInt(res_capacity[0]);
		
		return capacity;
		}catch(Exception ex){}
		
		return -1;
	}
	
}
