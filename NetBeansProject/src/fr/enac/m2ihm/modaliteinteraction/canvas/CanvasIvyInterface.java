/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.canvas;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;
import fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer.Gesture;
import fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer.GesturesPattern;
import fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer.Stroke;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilhem
 */
public class CanvasIvyInterface {
    private Ivy ivy;
    //private Map<Gesture, List<Point>> temp;
    //private int i = 0;

    public CanvasIvyInterface() {
        ivy = new Ivy("Canvas", "Canvas Online", null);
        try {
            ivy.start("127.255.255:2010");
                   System.out.println("Working Directory = " +
              System.getProperty("user.dir"));
        } catch (IvyException ex) {
            Logger.getLogger(CanvasIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendDraw(List<Point> shape){
        String toSend = "Canvas:Shape ";
        //Stroke s = new Stroke();
        for (Point p : shape){
            //s.addPoint((int)p.getX(), (int)p.getY());
            toSend = toSend.concat("X=" + p.getX() + ",Y=" + p.getY() + ";");
        }
        try {
            ivy.sendMsg(toSend.substring(0, toSend.length() - 1));
        } catch (IvyException ex) {
            Logger.getLogger(CanvasIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
