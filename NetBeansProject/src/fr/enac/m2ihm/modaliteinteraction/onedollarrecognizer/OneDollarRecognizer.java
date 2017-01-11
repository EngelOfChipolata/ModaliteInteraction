/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.onedollarrecognizer;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author guilhem
 */
public class OneDollarRecognizer {
    
    private final OneDollarIvyInterface ivy;

    public OneDollarRecognizer() {
        this.ivy = new OneDollarIvyInterface();
        ivy.addPropertyChangeListener(OneDollarIvyInterface.RAW_SHAPE_PROPERTY, (evt) -> {
            if (evt.getNewValue() != null){
                Stroke stroke = new Stroke();
                for (Point p : ((List<Point>) evt.getNewValue())){
                    stroke.addPoint((int) p.getX(), (int) p.getY());
                }
                stroke.normalize();
                Gesture gesture = gestureRecognizer(stroke);
                ivy.sendGesture(gesture);
            }
        });
        
    }
    
    private Gesture gestureRecognizer(Stroke stroke){
        Map<Double, Gesture> distances = new HashMap<>();
        Map<Gesture, List<Point>> m = GesturesPattern.getPatterns();
        System.out.println(m);
        for (Map.Entry<Gesture, List<Point>> e : GesturesPattern.getPatterns().entrySet()){
            double sum = 0;
            for (int i=0; i < stroke.getPoints().size(); i++){
                Point2D c = stroke.getPoint(i);
                Point ti = e.getValue().get(i);
                sum += Math.sqrt(Math.pow(c.getX() - ti.getX(), 2) + Math.pow(c.getY() - ti.getY(), 2));
            }
            sum /= (stroke.getPoints().size() - 1);
            distances.put(sum, e.getKey());
        }
        
        return distances.get(Collections.min(distances.keySet()));
    }
    
    
    
    public static void main(String[] args){
        OneDollarRecognizer main = new OneDollarRecognizer();
    } 
}
