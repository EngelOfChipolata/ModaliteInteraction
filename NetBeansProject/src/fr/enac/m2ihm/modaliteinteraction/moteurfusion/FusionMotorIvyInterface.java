/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.moteurfusion;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author buisangu
 */
public class FusionMotorIvyInterface {
    private final PropertyChangeSupport support;
    private final Ivy ivy;
    
    public FusionMotorIvyInterface(){
        support = new PropertyChangeSupport(this);
        ivy = new Ivy("FusionMotor", "Fusion Motor Online", null);
        try {
            ivy.start("127.255.255.255:2010");
            ivy.bindMsg("1Dollar:Gesture ([^ ]*)", (IvyClient e, String[] args) -> {
                Forme f = Forme.valueOf(args[0]);
                System.out.println(f);
        });
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
