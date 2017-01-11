/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

import java.io.Serializable;

/**
 *
 * @author guilhem
 */
public enum Gesture implements Serializable{
    ELLIPSE("ellipse"),
    RECTANGLE("rectangle"),
    LINE("line"),
    ALPHA("alpha");
    
    private final String gestureName;
    
    private Gesture(String name){
        gestureName = name;
    }

    @Override
    public String toString() {
        return gestureName;
    }
    
    
}
