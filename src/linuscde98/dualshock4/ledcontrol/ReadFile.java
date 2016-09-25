package linuscde98.dualshock4.ledcontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadFile {
    
    public static String[] read(File f){
    
    // Buffer for the lines
    ArrayList<String> list = new ArrayList<String>();
   
    // Checking if file exists
    if(f.exists() == false){
    return null;
    }
    
    // Reading file
    try{
    BufferedReader br = new BufferedReader(new FileReader(f));
    
    // Buffer for current line
    String line;
    // Read line by line
    while((line = br.readLine()) != null){
    if(line.equalsIgnoreCase("") == false && !line.startsWith("#")){
    list.add(line);
    }
    }
    
    br.close();
    }catch(Exception e){
    }
    
    // Convert ArrayList to a StringArray
    String[] str = new String[]{};
    str = list.toArray(str);
    
    return str;
    }
    
}
