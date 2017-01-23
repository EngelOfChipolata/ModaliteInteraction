/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.moteurfusion;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author buisangu
 */
public class FusionMotorIvyInterface {

    public static final String createEvent = "createEventProperty";
    public static final String deleteEvent = "deleteEventProperty";
    public static final String moveEvent = "deleteEventProperty";
    public static final String keywordPositionEvent = "keywordPositionProperty";
    public static final String clicEvent = "clicProperty";
    public static final String keywordColorEvent = "keywordColorProperty";
    public static final String keywordDeCetteCouleurEvent = "keywordDCCProperty";

    private final PropertyChangeSupport support;
    private final Ivy ivy;

    public FusionMotorIvyInterface() {
        support = new PropertyChangeSupport(this);
        ivy = new Ivy("FusionMotor", "Fusion Motor Online", null);
        try {
            ivy.start("127.255.255.255:2010");
            ivy.bindMsg("1Dollar:Gesture ([^ ]*)", (IvyClient e, String[] args) -> {
                Forme f = Forme.valueOf(args[0].toUpperCase());
                support.firePropertyChange(createEvent, null, f);
                System.out.println(f);
            });
            ivy.bindMsg("", (client, args) -> {
                
            });
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addPropertyChangeListener(PropertyChangeListener li) {
        support.addPropertyChangeListener(li);
    }

    public void addPropertyChangeListener(String propName, PropertyChangeListener li) {
        support.addPropertyChangeListener(propName, li);
    }

    public void removePropertyChangeListener(PropertyChangeListener li) {
        support.removePropertyChangeListener(li);
    }

    public void removePropertyChangeListener(String propName, PropertyChangeListener li) {
        support.removePropertyChangeListener(propName, li);
    }
}
