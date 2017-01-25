/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.moteurfusion;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

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
    public static final String keywordObjectEvent = "keywordObjectProperty";

    private final double speechConfidenceIndex = 0.8;
    private final PropertyChangeSupport support;
    private final Ivy ivy;
    private final FSMColor fsmColor;
    private final FSMDelete fsmDelete;

    public FusionMotorIvyInterface() {
        support = new PropertyChangeSupport(this);
        ivy = new Ivy("FusionMotor", "Fusion Motor Online", null);
        try {
            ivy.start("127.255.255.255:2010");
            ivy.bindMsg("1Dollar:Gesture (ellipse|rectangle)", (IvyClient e, String[] args) -> {
                Forme f = Forme.valueOf(args[0].toUpperCase());
                support.firePropertyChange(createEvent, null, f);
                System.out.println(f);
            });
            ivy.bindMsg("1Dollar:Gesture alpha", (client, args) -> {
                support.firePropertyChange(deleteEvent, null, null);
            });
            ivy.bindMsg("1Dollar:Gesture line", (client, args) -> {
                support.firePropertyChange(moveEvent, null, null);
            });
            ivy.bindMsg("Palette:MouseClicked x=([^ ]*) y=([^ ]*)", (IvyClient e, String[] args) -> {
                Point pt = new Point(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
                support.firePropertyChange(clicEvent, null, pt);
            });
            ivy.bindMsg("sra5 Parsed=SRA:Position here Confidence=([^ ]*)", (IvyClient e, String[] args) -> {
                double confidence = Double.valueOf(args[0].replace(',', '.'));
                if (confidence >= speechConfidenceIndex) {
                    support.firePropertyChange(keywordPositionEvent, null, null);
                }
            });
            ivy.bindMsg("sra5 Parsed=SRA:Couleur ([^ ]*) Confidence=([^ ]*)", (client, args) -> {
                double confidence = Double.valueOf(args[1].replace(',', '.'));
                if (confidence >= speechConfidenceIndex) {
                    Color c = colorFromFrenchString(args[0]);
                    support.firePropertyChange(keywordColorEvent, null, c);
                }
            });
            ivy.bindMsg("sra5 Parsed=SRA:Attribut couleur Confidence=([^ ]*)", (client, args) -> {
                double confidence = Double.valueOf(args[0].replace(',', '.'));
                if (confidence >= speechConfidenceIndex) {
                    support.firePropertyChange(keywordDeCetteCouleurEvent, null, null);
                }
            });
            ivy.bindMsg("sra5 Parsed=SRA:Objet ([^ ]*) Confidence=([^ ]*)", (client, args) ->{
                double confidence = Double.valueOf(args[1].replace(',', '.'));
                if (confidence >= speechConfidenceIndex) {
                    Forme f = Forme.valueOf(args[0].toUpperCase());
                    support.firePropertyChange(keywordObjectEvent, null, f);
                }
            });

        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        fsmColor = new FSMColor();
        fsmDelete = new FSMDelete();
    }
    
    private Color colorFromFrenchString(String str){
        Color c;
        switch (str.toUpperCase()){
                        case "NOIR":
                            c = Color.BLACK;
                            break;
                        case "ROUGE":
                            c = Color.RED;
                            break;
                        case "BLEU":
                            c = Color.BLUE;
                            break;
                        case "BLANC":
                            c = Color.WHITE;
                            break;
                        case "VERT":
                            c = Color.GREEN;
                            break;
                        case "GRIS":
                            c = Color.GRAY;
                            break;
                        case "JAUNE":
                            c = Color.YELLOW;
                            break;
                        default:
                            c = Color.WHITE;
                            break;
        }
        return c;
    }

    public FusionMotorIvyInterface(PropertyChangeSupport support, Ivy ivy, FSMColor fsmColor, FSMDelete fsmDelete) {
        this.support = support;
        this.ivy = ivy;
        this.fsmColor = fsmColor;
        this.fsmDelete = fsmDelete;
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

    public void create(Integer x, Integer y, Color color, Forme forme) {
        String toSend = "Palette:Creer";
        if (forme == Forme.RECTANGLE) {
            toSend += "Rectangle";
        } else if (forme == Forme.ELLIPSE) {
            toSend += "Ellipse";
        }
        if (x != null && y != null) {
            toSend += " x=" + x + " y=" + y;
        }
        if (color != null) {
            toSend += " couleurFond=" + color.getRed() + ":" + color.getGreen() + ":" + color.getBlue();
        }
        try {
            ivy.sendMsg(toSend);
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private enum FSMColorState {
        IDLE,
        WAIT_NAME,
        WAIT_INFO
    }

    private final class FSMColor {

        private FSMColorState state;
        private final Timer timer1;
        private boolean answered;
        private String name;
        private Color finalColor;

        private FSMColor() {
            try {
                ivy.bindMsg("Palette:ResultatTesterPoint x=[^ ]* y=[^ ]* nom=([^ ]*)", (client, args) -> {
                    name = args[0];
                    state = FSMColorState.WAIT_INFO;
                    answered = true;
                });
                ivy.bindMsg("Palette:Info nom=[^ ]* x=[^ ]* y=[^ ]* longueur=[^ ]* hauteur=[^ ]* couleurFond=([^ ]*) couleurContour=[^ ]*", (client, args) -> {
                   finalColor = Color.getColor(args[0]);
                   state = FSMColorState.IDLE;
                   answered = true;
                });
            } catch (IvyException ex) {
                Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
            state = FSMColorState.IDLE;
            timer1 = new Timer(500, e -> {
                finalColor = Color.RED;
                state = FSMColorState.IDLE;
                answered = true;
            });
            timer1.setRepeats(false);
        }

        private Color fsmColor(int x, int y) {
            state = FSMColorState.WAIT_NAME;
            timer1.restart();
            testerPoint(x, y);
            while (!answered) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            answered = false;
            if (state == FSMColorState.IDLE) {
                return finalColor;
            }
            if (state == FSMColorState.WAIT_INFO) {
                demanderInfo(name);
                while (!answered) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                answered = false;
                return finalColor;
            }
            throw new RuntimeException("FSM Color : Not in a suitable state !");
        }
    }

    public Color fsmColor(int x, int y) {
        return fsmColor.fsmColor(x, y);

    }

    private void testerPoint(int x, int y) {
        try {
            ivy.sendMsg("Palette:TesterPoint x="+ x +" y=" + y);
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void demanderInfo(String name) {
        try {
            ivy.sendMsg("Palette:DemanderInfo nom=" + name);
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private enum FSMDeleteState{
        IDLE,
        DELETE_GET_ALL_BELOW_POINT,
        DELETE_GET_ALL_INFOS
    }
    private final class FSMDelete{
        
        private FSMDeleteState state;
        private Forme forme;
        private Color color;
        private final Timer timer1;
        private final Timer timer2;
        private int size;
        private List<String> noms;
        
        private FSMDelete(){
            state = FSMDeleteState.IDLE;
            timer1 = new Timer(250, null);
            timer1.setRepeats(false);
            timer2 = new Timer(250, null);
            timer2.setRepeats(false);
            timer1.addActionListener((e) -> {
                switch(state){
                    case DELETE_GET_ALL_BELOW_POINT:
                        if (forme == Forme.ANY){
                            size = noms.size() - 1;
                            demanderInfo(noms.get(size));
                            timer1.stop();
                            state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                            timer2.restart();
                        }else if (forme != Forme.ANY){
                            trierFormes();
                            size = noms.size() - 1;
                            demanderInfo(noms.get(size));
                            timer1.stop();
                            state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                            timer2.restart();
                        }else{
                            throw new RuntimeException("Forme to delete is not defined !");
                        }
                        break;
                }
            });
            timer2.addActionListener((e) -> {
                switch (state){
                    case DELETE_GET_ALL_BELOW_POINT:
                        if (size != 0){
                            noms.remove(size);
                            demanderInfo(noms.get(--size));
                            state = FSMDeleteState.DELETE_GET_ALL_BELOW_POINT;
                            timer2.restart();
                        }else if (size == 0 && !noms.isEmpty()){
                            supprimer(noms.get(0));
                            state = FSMDeleteState.IDLE;
                            timer2.stop();
                        }else if (size == 0 && noms.isEmpty()){
                            state = FSMDeleteState.IDLE;
                            timer2.stop();
                        }
                        break;
                }
            });
            try {
                ivy.bindMsg("Palette:ResultatTesterPoint x=[^ ]* y=[^ ]* nom=([^ ]*)", (client, args) -> {
                    switch (state){
                        case DELETE_GET_ALL_BELOW_POINT:
                            noms.add(args[0]);
                            timer1.restart();
                            break;
                    }
                });
                ivy.bindMsg("Palette:Info nom=([^ ]*) x=[^ ]* y=[^ ]* longueur=[^ ]* hauteur=[^ ]* couleurFond=([^ ]*) couleurContour=[^ ]*", (client, args) -> {
                    switch (state){
                        case DELETE_GET_ALL_INFOS:
                            if (size != 0){
                                if (color != null && color != colorFromFrenchString(args[1])){
                                    noms.remove(args[0]);
                                }
                                demanderInfo(noms.get(--size));
                                state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                                timer2.restart();
                            }else if(size == 0 && color == null && !noms.isEmpty()){
                                timer2.stop();
                                state = FSMDeleteState.IDLE;
                                supprimer(noms.get(0));
                            }else if(size == 0 && color != null && !noms.isEmpty()){
                                if (color != colorFromFrenchString(args[1])){
                                    noms.remove(args[0]);
                                }
                                timer2.stop();
                                state = FSMDeleteState.IDLE;
                                supprimer(noms.get(0));
                            }else if(size == 0 && noms.isEmpty()){
                                timer2.stop();
                                state=FSMDeleteState.IDLE;
                            }
                    }
                });
            } catch (IvyException ex) {
                Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private void delete(int x, int y, Forme f, Color c){
            noms = new ArrayList<>();
            forme = f;
            color = c;
            state = FSMDeleteState.DELETE_GET_ALL_BELOW_POINT;
            testerPoint(x, y);
            timer1.restart();
        }
        
        private void trierFormes(){
            List<String> filteredList = new ArrayList<>();
            String prefix = "";
            if (forme == Forme.ELLIPSE){
                prefix = "ellipse";
            }
            if (forme == Forme.RECTANGLE){
                prefix = "rectangle";
            }
            for (String nom: noms){
                if (nom.startsWith(prefix)){
                    filteredList.add(nom);
                }
            }
            noms = filteredList;
        }
    }
    
    public void delete(int x, int y, Forme f, Color c){
        fsmDelete.delete(x, y, f, c);
    }
    
    public void supprimer(String name){
        //TODO ivy send supprimer(name)
    }
}
