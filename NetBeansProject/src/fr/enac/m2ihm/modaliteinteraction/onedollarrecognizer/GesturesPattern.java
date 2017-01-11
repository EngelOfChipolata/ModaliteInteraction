/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilhem
 */
public final class GesturesPattern{
    private static final String FILE = "gestures/patterns.txt";
    private static Map<Gesture, List<Point>> patterns;
    

    public static Map<Gesture, List<Point>> getPatterns() {
        if(patterns == null){
            try {
                FileInputStream fileIn = new FileInputStream(FILE);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                patterns = (HashMap<Gesture, List<Point>>) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(GesturesPattern.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return patterns;
    }
       
}
