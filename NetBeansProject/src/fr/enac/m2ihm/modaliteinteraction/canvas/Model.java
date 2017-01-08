/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.canvas;

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class Model {
    
    public static final String TRACE_PROPERTY = "traceChanged";
    private List<Point> pointList;
    private final CanvasIvyInterface ivy;

    private void beginDraw(Point pt) {
        switch(state){
            case IDLE:
                state = State.DRAWING;
                List<Point> oldList = new ArrayList<>(pointList);;
                pointList = new ArrayList<>();
                pointList.add(pt);
                support.firePropertyChange(TRACE_PROPERTY, oldList, pointList);
                break;
            case DRAWING:
                break;
            
        }
    }

    private void draw(Point pt) {
        switch(state){
            case IDLE:
                break;
            case DRAWING:
                state = State.DRAWING;
                List<Point> oldList = new ArrayList<>(pointList);
                pointList.add(pt);
                support.firePropertyChange(TRACE_PROPERTY, oldList, pointList);
                break;
        }
    }

    private void endDraw(Point pt) {
        switch(state){
            case IDLE:
                break;
            case DRAWING:
                state = State.IDLE;
                List<Point> oldList = new ArrayList<>(pointList);;
                pointList.add(pt);
                support.firePropertyChange(TRACE_PROPERTY, oldList, pointList);
                ivy.sendDraw(pointList);
                break;
        }
    }
    
    private enum State{
        IDLE,
        DRAWING
    }
    
    private final PropertyChangeSupport support;
    private State state;
    
    public Model(){
        state = State.IDLE;
        support = new PropertyChangeSupport(this);
        pointList = new ArrayList<>();
        ivy = new CanvasIvyInterface();
    }
    
    public void handleEvent(DrawEvents evt, Point pt){
        switch(evt){
            case BEGIN_DRAW:
                beginDraw(pt);
                break;
            case DRAW:
                draw(pt);
                break;
            case END_DRAW:
                endDraw(pt);
                break;
            default:
                throw new AssertionError(evt.name());
            
        }
    }
    
    void addPropertyListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }
    void addPropertyListener(String propertyName, PropertyChangeListener listener){
        support.addPropertyChangeListener(propertyName, listener);
    }
    void removePropertyListener(PropertyChangeListener listener){
        support.removePropertyChangeListener(listener);
    }
    void removePropertyListener(String propertyName, PropertyChangeListener listener){
        support.removePropertyChangeListener(propertyName, listener);
    }
    
    
    
}
