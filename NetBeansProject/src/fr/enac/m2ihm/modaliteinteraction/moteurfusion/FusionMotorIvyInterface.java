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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final String moveEvent = "moveEventProperty";
    public static final String keywordPositionEvent = "keywordPositionProperty";
    public static final String clicEvent = "clicProperty";
    public static final String keywordColorEvent = "keywordColorProperty";
    public static final String keywordDeCetteCouleurEvent = "keywordDCCProperty";
    public static final String keywordObjectEvent = "keywordObjectProperty";

    private final double speechConfidenceIndex = 0.65;
    private final PropertyChangeSupport support;
    private final Ivy ivy;
    private FSMCreate fsmCreate;
    private final FSMDelete fsmDelete;
    private final FSMMove fsmMove;

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
            ivy.bindMsg("sra5 Parsed=SRA:Objet ([^ ]*) Confidence=([^ ]*)", (client, args) -> {
                double confidence = Double.valueOf(args[1].replace(',', '.'));
                if (confidence >= speechConfidenceIndex) {
                    Forme f = Forme.valueOf(args[0].toUpperCase());
                    support.firePropertyChange(keywordObjectEvent, null, f);
                }
            });

        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        fsmCreate = new FSMCreate();
        fsmDelete = new FSMDelete();
        fsmMove = new FSMMove();
    }

    private Color colorFromFrenchString(String str) {
        Color c;
        switch (str.toUpperCase()) {
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

    public FusionMotorIvyInterface(PropertyChangeSupport support, Ivy ivy, FSMCreate fsmCreate, FSMDelete fsmDelete, FSMMove fsmMove) {
        this.support = support;
        this.ivy = ivy;
        this.fsmCreate = fsmCreate;
        this.fsmDelete = fsmDelete;
        this.fsmMove = fsmMove;
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

    public void create(Integer x, Integer y, Color color, Forme forme, Point colorXY) {
        fsmCreate.createEvent(x, y, color, forme, colorXY);
    }

    private enum FSMCreateState {
        IDLE,
        WAIT_NAME_CREATE,
        WAIT_INFO_CREATE
    }

    private class FSMCreate {

        private final Timer timer1;
        private FSMCreateState state;
        private Integer x;
        private Integer y;
        private Forme f;

        private FSMCreate() {
            state = FSMCreateState.IDLE;
            timer1 = new Timer(250, (e) -> {
                create(x, y, null, f);
                state = FSMCreateState.IDLE;
            });
            timer1.setRepeats(false);
            try {
                ivy.bindMsg("Palette:ResultatTesterPoint x=[^ ]* y=[^ ]* nom=([^ ]*)", (client, args) -> {
                    if (state == FSMCreateState.WAIT_NAME_CREATE) {
                        timer1.stop();
                        demanderInfo(args[0]);
                        state = FSMCreateState.WAIT_INFO_CREATE;
                    }
                });
                ivy.bindMsg("Palette:Info nom=[^ ]* x=[^ ]* y=[^ ]* longueur=[^ ]* hauteur=[^ ]* couleurFond=([^ ]*) couleurContour=[^ ]*", (client, args) -> {
                    if (state == FSMCreateState.WAIT_INFO_CREATE) {
                        create(x, y, stringColorToColor(args[0]), f);
                        state = FSMCreateState.IDLE;
                    }
                });

            } catch (IvyException ex) {
                Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void createEvent(Integer x, Integer y, Color c, Forme f, Point colorXY) {
            switch (state) {
                case IDLE:
                    if (colorXY == null) {
                        create(x, y, c, f);
                    } else if (colorXY != null) {
                        this.x = x;
                        this.y = y;
                        this.f = f;
                        testerPoint(colorXY.x, colorXY.y);
                        state = FSMCreateState.WAIT_NAME_CREATE;
                        timer1.restart();
                    }
                    break;
            }
        }
    }

    private void testerPoint(int x, int y) {
        try {
            ivy.sendMsg("Palette:TesterPoint x=" + x + " y=" + y);
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

    private enum FSMDeleteState {
        IDLE,
        DELETE_GET_ALL_BELOW_POINT,
        DELETE_GET_ALL_INFOS
    }

    private final class FSMDelete {

        private FSMDeleteState state;
        private Forme forme;
        private Color color;
        private final Timer timer1;
        private final Timer timer2;
        private int size;
        private List<String> noms;

        private FSMDelete() {
            state = FSMDeleteState.IDLE;
            timer1 = new Timer(250, null);
            timer1.setRepeats(false);
            timer2 = new Timer(250, null);
            timer2.setRepeats(false);
            timer1.addActionListener((e) -> {
                switch (state) {
                    case DELETE_GET_ALL_BELOW_POINT:
                        if (forme == Forme.ANY) {
                            size = noms.size() - 1;
                            demanderInfo(noms.get(size));
                            timer1.stop();
                            state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                            timer2.restart();
                        } else if (forme != Forme.ANY) {
                            trierFormes();
                            if (!noms.isEmpty()) {
                                size = noms.size() - 1;
                                demanderInfo(noms.get(size));
                                timer1.stop();
                                state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                                timer2.restart();
                            } else {
                                timer1.stop();
                                state = FSMDeleteState.IDLE;
                            }
                        } else {
                            throw new RuntimeException("Forme to delete is not defined !");
                        }
                        break;
                }
            });
            timer2.addActionListener((e) -> {
                switch (state) {
                    case DELETE_GET_ALL_BELOW_POINT:
                        if (size != 0) {
                            noms.remove(size);
                            demanderInfo(noms.get(--size));
                            state = FSMDeleteState.DELETE_GET_ALL_BELOW_POINT;
                            timer2.restart();
                        } else if (size == 0 && !noms.isEmpty()) {
                            supprimer(noms.get(0));
                            state = FSMDeleteState.IDLE;
                            timer2.stop();
                        } else if (size == 0 && noms.isEmpty()) {
                            state = FSMDeleteState.IDLE;
                            timer2.stop();
                        }
                        break;
                }
            });
            try {
                ivy.bindMsg("Palette:ResultatTesterPoint x=[^ ]* y=[^ ]* nom=([^ ]*)", (client, args) -> {
                    switch (state) {
                        case DELETE_GET_ALL_BELOW_POINT:
                            noms.add(args[0]);
                            timer1.restart();
                            break;
                    }
                });
                ivy.bindMsg("Palette:Info nom=([^ ]*) x=[^ ]* y=[^ ]* longueur=[^ ]* hauteur=[^ ]* couleurFond=([^ ]*) couleurContour=[^ ]*", (client, args) -> {
                    switch (state) {
                        case DELETE_GET_ALL_INFOS:
                            if (size != 0) {
                                if (color != null && color != stringColorToColor(args[1])) {
                                    noms.remove(args[0]);
                                }
                                demanderInfo(noms.get(--size));
                                state = FSMDeleteState.DELETE_GET_ALL_INFOS;
                                timer2.restart();
                            } else if (size == 0 && color == null && !noms.isEmpty()) {
                                timer2.stop();
                                state = FSMDeleteState.IDLE;
                                supprimer(noms.get(0));
                            } else if (size == 0 && color != null && !noms.isEmpty()) {
                                System.out.println("color :" + args[1].toUpperCase());
                                System.out.println("To DELETE GET ALL INFOS Color : " + color + " vs " + stringColorToColor(args[1]));
                                if (color != stringColorToColor(args[1])) {
                                    noms.remove(args[0]);
                                }
                                timer2.stop();
                                state = FSMDeleteState.IDLE;
                                if (!noms.isEmpty()) {
                                    supprimer(noms.get(0));
                                }
                            } else if (size == 0 && noms.isEmpty()) {
                                timer2.stop();
                                state = FSMDeleteState.IDLE;
                            }
                    }
                });
            } catch (IvyException ex) {
                Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void delete(int x, int y, Forme f, Color c) {
            noms = new ArrayList<>();
            forme = f;
            color = c;
            state = FSMDeleteState.DELETE_GET_ALL_BELOW_POINT;
            testerPoint(x, y);
            timer1.restart();
        }

        private void trierFormes() {
            List<String> filteredList = new ArrayList<>();
            String prefix = "";
            if (forme == Forme.ELLIPSE) {
                prefix = "E";
            }
            if (forme == Forme.RECTANGLE) {
                prefix = "R";
            }
            for (String nom : noms) {
                if (nom.startsWith(prefix)) {
                    filteredList.add(nom);
                }
            }
            noms = filteredList;
        }
    }

    private Color stringColorToColor(String s) {
        Color tempColor;
        try {
            Field field = Class.forName("java.awt.Color").getField(s.toLowerCase());
            tempColor = (Color) field.get(null);
        } catch (Exception e) {
            tempColor = null; // Not defined
        }
        System.out.println("Couleur : " + tempColor + " string : " + s);
        return tempColor;
    }

    private enum FSMMoveState {
        IDLE,
        GET_ALL_BELOW_POINT_MOVE,
        GET_ALL_INFOS_MOVE
    }

    private final class FSMMove {

        private FSMMoveState state;
        private List<String> noms;
        private HashMap<String, String[]> formes; //[nom; [color, position], ...]
        private Timer timer1;
        private Timer timer2;
        private Forme forme;
        private Point destination;
        private Color color;

        private int size;

        private FSMMove() {
            state = FSMMoveState.IDLE;
            timer1 = new Timer(250, null);
            timer2 = new Timer(250, null);
            timer1.setRepeats(false);
            timer2.setRepeats(false);
            timer1.addActionListener((e) -> {
                if (state == FSMMoveState.GET_ALL_BELOW_POINT_MOVE) {
                    if (forme != Forme.ANY) {
                        formes = new HashMap<>();
                        timer1.stop();
                        trierFormes();
                        if (!noms.isEmpty()) {
                            size = noms.size() - 1;
                            demanderInfo(noms.get(size));
                            noms.remove(size);
                            state = FSMMoveState.GET_ALL_INFOS_MOVE;
                            timer2.restart();
                        } else {
                            state = FSMMoveState.IDLE;
                        }
                    } else if (forme == Forme.ANY) {
                        formes = new HashMap<>();
                        timer1.stop();
                        size = noms.size() - 1;
                        demanderInfo(noms.get(size));
                        noms.remove(size);
                        state = FSMMoveState.GET_ALL_INFOS_MOVE;
                        timer2.restart();
                    }
                }
            });
            timer2.addActionListener((e) -> {
                if (state == FSMMoveState.GET_ALL_INFOS_MOVE) {
                    if (size != 0) {
                        timer2.restart();
                        demanderInfo(noms.get(--size));
                        noms.remove(size);
                        state = FSMMoveState.GET_ALL_INFOS_MOVE;
                    } else if (size == 0 && color != null && !formes.isEmpty()) {
                        timer2.stop();
                        trierColor();
                        Map.Entry<String, String[]> firstEntry = formes.entrySet().iterator().next();
                        String[] point = firstEntry.getValue()[1].split(",");
                        Point shapePoint = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
                        deplacer(firstEntry.getKey(), destination.x - shapePoint.x, destination.y - shapePoint.y);
                        state = FSMMoveState.IDLE;
                    } else if (size == 0 && color == null && !formes.isEmpty()) {
                        timer2.stop();
                        Map.Entry<String, String[]> firstEntry = formes.entrySet().iterator().next();
                        String[] point = firstEntry.getValue()[1].split(",");
                        Point shapePoint = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
                        deplacer(firstEntry.getKey(), destination.x - shapePoint.x, destination.y - shapePoint.y);
                        state = FSMMoveState.IDLE;
                    } else { // noms.isEmpty && size == 0
                        timer2.stop();
                        state = FSMMoveState.IDLE;
                    }
                }
            });
            try {
                ivy.bindMsg("Palette:ResultatTesterPoint x=[^ ]* y=[^ ]* nom=([^ ]*)", (client, args) -> {
                    if (state == FSMMoveState.GET_ALL_BELOW_POINT_MOVE) {
                        noms.add(args[0]);
                        timer1.restart();
                        state = FSMMoveState.GET_ALL_BELOW_POINT_MOVE;
                    }

                });
                ivy.bindMsg("Palette:Info nom=([^ ]*) x=([^ ]*) y=([^ ]*) longueur=[^ ]* hauteur=[^ ]* couleurFond=([^ ]*) couleurContour=[^ ]*", (client, args) -> {
                    if (state == FSMMoveState.GET_ALL_INFOS_MOVE) {
                        if (size != 0) {
                            timer2.restart();
                            String[] infos = {args[3], args[1] + "," + args[2]};
                            formes.put(args[0], infos);
                            state = FSMMoveState.GET_ALL_INFOS_MOVE;
                            demanderInfo(noms.get(--size));
                            noms.remove(size);
                        } else if (size == 0 && color != null) {
                            timer2.stop();
                            String[] infos = {args[3], args[1] + "," + args[2]};
                            formes.put(args[0], infos);
                            trierColor();
                            Map.Entry<String, String[]> firstEntry = formes.entrySet().iterator().next();
                            String[] point = firstEntry.getValue()[1].split(",");
                            Point shapePoint = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
                            System.out.println("dest " + destination + " shapePoint" + shapePoint);
                            deplacer(firstEntry.getKey(), destination.x - shapePoint.x, destination.y - shapePoint.y);
                            state = FSMMoveState.IDLE;
                        } else if (size == 0 && color == null) {
                            timer2.stop();
                            String[] infos = {args[3], args[1] + "," + args[2]};
                            formes.put(args[0], infos);
                            Map.Entry<String, String[]> firstEntry = formes.entrySet().iterator().next();
                            String[] point = firstEntry.getValue()[1].split(",");
                            Point shapePoint = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
                            deplacer(firstEntry.getKey(), destination.x - shapePoint.x, destination.y - shapePoint.y);
                            state = FSMMoveState.IDLE;
                        } else {
                            timer2.stop();
                            state = FSMMoveState.IDLE;
                        }
                    }
                });
            } catch (IvyException ex) {
                Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void trierColor() {
            List<String> toDelete = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : formes.entrySet()) {
                if (color != stringColorToColor(entry.getValue()[0])) {
                    toDelete.add(entry.getKey());
                }
            }
            for (String key : toDelete) {
                formes.remove(key);
            }
        }

        private void move(Forme f, Color c, Point formePosition, Point destination) {
            switch (state) {
                case IDLE:
                    noms = new ArrayList<>();
                    testerPoint(formePosition.x, formePosition.y);
                    forme = f;
                    color = c;
                    this.destination = destination;
                    state = FSMMoveState.GET_ALL_BELOW_POINT_MOVE;
                    timer1.restart();
                    break;
            }
        }

        private void trierFormes() {
            List<String> filteredList = new ArrayList<>();
            String prefix = "";
            if (forme == Forme.ELLIPSE) {
                prefix = "E";
            }
            if (forme == Forme.RECTANGLE) {
                prefix = "R";
            }
            for (String nom : noms) {
                if (nom.startsWith(prefix)) {
                    filteredList.add(nom);
                }
            }
            noms = filteredList;
        }
    }

    public void delete(int x, int y, Forme f, Color c) {
        fsmDelete.delete(x, y, f, c);
    }

    public void move(Forme f, Color c, Point formePosition, Point destination) {
        fsmMove.move(f, c, formePosition, destination);
    }

    public void supprimer(String name) {
        try {
            ivy.sendMsg("Palette:SupprimerObjet nom=" + name);
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deplacer(String nom, int dx, int dy) {
        try {
            ivy.sendMsg("Palette:DeplacerObjet nom=" + nom + " x=" + dx + " y=" + dy);
        } catch (IvyException ex) {
            Logger.getLogger(FusionMotorIvyInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
