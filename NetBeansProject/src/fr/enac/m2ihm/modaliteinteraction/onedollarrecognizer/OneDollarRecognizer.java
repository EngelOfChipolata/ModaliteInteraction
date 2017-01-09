/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

import java.awt.Point;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class OneDollarRecognizer {
    
    private final OneDollarIvyInterface ivy;

    public OneDollarRecognizer() {
        this.ivy = new OneDollarIvyInterface();
        ivy.addPropertyChangeListener(OneDollarIvyInterface.RAW_SHAPE_PROPERTY, (evt) -> {
            System.out.println("Yah old pirate");
        });
        
    }
    
    private Gestures gestureRecognizer(List<Point> normalizedShape){
        return null;
    }
    
    
    
    public static void main(String[] args){
        OneDollarRecognizer main = new OneDollarRecognizer();
    } 
}
