/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.canvas;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;
import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilhem
 */
public class CanvasIvyInterface {
    private Ivy ivy;

    public CanvasIvyInterface() {
        ivy = new Ivy("Canvas", "Canvas Online", null);
        try {
            ivy.start("127.255.255:2010");
        } catch (IvyException ex) {
            Logger.getLogger(CanvasIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendDraw(List<Point> shape){
        String toSend = "Canvas:Shape ";
        for (Point p : shape){
            toSend = toSend.concat("X=" + p.getX() + ",Y=" + p.getY() + ";");
        }
        try {
            ivy.sendMsg(toSend.substring(0, toSend.length() - 1));
        } catch (IvyException ex) {
            Logger.getLogger(CanvasIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
