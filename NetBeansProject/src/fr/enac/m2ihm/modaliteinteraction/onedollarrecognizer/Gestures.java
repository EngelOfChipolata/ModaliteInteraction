/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

/**
 *
 * @author guilhem
 */
public enum Gestures {
    ELLIPSE("ellipse"),
    RECTANGLE("rectangle"),
    LINE("line"),
    ALPHA("alpha");
    
    private final String gestureName;
    
    private Gestures(String name){
        gestureName = name;
    }

    @Override
    public String toString() {
        return gestureName;
    }
    
    
}
