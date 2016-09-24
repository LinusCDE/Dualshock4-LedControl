/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linuscde98.dualshock4.ledcontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadFile {
    
    public static String[] read(File f){
    
    //Speicher für die Zeilen
    ArrayList<String> list = new ArrayList<String>();
   
    //Checke ob Datei existiert.
    if(f.exists() == false){
    return null;
    }
    
    //Lese Datei
    try{
    BufferedReader br = new BufferedReader(new FileReader(f));
    
    //Speicher für aktuelle Zeile
    String line;
    //Lese Zeile für Zeile
    while((line = br.readLine()) != null){
    if(line.equalsIgnoreCase("") == false && !line.startsWith("#")){
    list.add(line);
    }
    }
    
    br.close();
    }catch(Exception e){
    }
    
    //Konvertiere ArrayList in einen StringArray
    String[] str = new String[]{};
    str = list.toArray(str);
    
    //Gebe den StringArray zurück
    return str;
    }
    
}
