/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilhem
 */
public class OneDollarIvyInterface {
    
    public static final String RAW_SHAPE_PROPERTY = "rawShapeProperty";
    
    private final PropertyChangeSupport support;
    private final Ivy ivy;
    
    private List<Point> shapeBuffer;

    public OneDollarIvyInterface() {
        support = new PropertyChangeSupport(this);
        
        ivy = new Ivy("1DollarReco", "1DollarReco Online", null);
        
        shapeBuffer = new ArrayList<>();
        
        try {
            ivy.start("127.255.255:2010");
        } catch (IvyException ex) {
            Logger.getLogger(OneDollarIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            ivy.bindMsg("^Canvas:Shape (.*)", (IvyClient client, String[] args) -> {
                List<Point> oldList = new ArrayList<>(shapeBuffer);
                shapeBuffer = new ArrayList<>();
                String rawPoints = args[0]; //rawPoints = X=12.0,Y=25.0;...;X=15.0,Y=120.0
                String[] points = rawPoints.split(";"); //points = ["X=12.0,Y=25.0", ..., "X=15.0,Y=120.0"]
                
                for(String pt : points){ //pt = "X=12.0, Y=25.0"
                    String[] coord = pt.split(","); //coord = ["X=12.0", "Y=25.0"]
                    int x = (int)Float.parseFloat(coord[0].substring(2));
                    int y = (int)Float.parseFloat(coord[1].substring(2));
                    shapeBuffer.add(new Point(x, y));
                }
                support.firePropertyChange(RAW_SHAPE_PROPERTY, oldList, shapeBuffer);
            });
        } catch (IvyException ex) {
            Logger.getLogger(OneDollarIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener li){
        support.addPropertyChangeListener(li);
    }
    
    public void addPropertyChangeListener(String name, PropertyChangeListener li){
        support.addPropertyChangeListener(name, li);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener li){
        support.removePropertyChangeListener(li);
    }
    
    public void removePropertyChangeListener(String name, PropertyChangeListener li){
        support.removePropertyChangeListener(name, li);
    }
    
}
