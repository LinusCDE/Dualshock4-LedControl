/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linuscde98.dualshock4.ledcontrol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {
   
    public static void write(File f, String[] str, boolean append){
    
    //Checke ob Datei existiert.
    if(f.exists() == false){
            try {
                //Erstelle leere Datei
                f.createNewFile();
            } catch (IOException ex) {
            }
    }
    
    //Schreibe Datei
    try{
    BufferedWriter bw = new BufferedWriter(new FileWriter(f, append));
    
    for(int i = 0; i < str.length; i++){
    String prefix = System.getProperty("line.separator");
    if(i == 0){prefix = "";}
    bw.write(prefix + str[i]);
    }
    
    bw.flush();
    bw.close();
    
    }catch(Exception e){
    }
    
    }
    
}
