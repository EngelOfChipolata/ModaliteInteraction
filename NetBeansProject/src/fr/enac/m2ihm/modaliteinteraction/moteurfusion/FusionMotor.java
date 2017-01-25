/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enac.m2ihm.modaliteinteraction.moteurfusion;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.Timer;

/**
 *
 * @author guilhem
 */
public class FusionMotor {

    private final int[] timerDurations = {3000, 3000, 3000, 4500, 4500, 4500, // Timer6, end creation
                                            4500, 4500, 4500, 3000, 3000      // Timer 10, end supression
};

    private final FusionMotorIvyInterface ivyInterface;
    private FusionMotorState state;

    private Forme creationValueForme;
    private Point creationPosition;
    private Point creationXY;
    private Color creationColor;
    private final Timer[] timers;
    
    private Forme suppressionForme;

    public FusionMotor() {
        ivyInterface = new FusionMotorIvyInterface();
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.createEvent, ((evt) -> {
            creer((Forme) evt.getNewValue());
        }));
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.keywordPositionEvent, (evt) -> {
            kwPosition();
        });
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.clicEvent, (evt) -> {
            clic((Point) evt.getNewValue());
        });
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.keywordColorEvent, (evt) -> {
            kwColor((Color) evt.getNewValue());
        });
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.keywordDeCetteCouleurEvent, (evt) -> {
            kwDCC();
        });
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.deleteEvent, (evt) -> {
            croix();
        });
        timers = new Timer[timerDurations.length];
        for (int i = 0; i < timers.length; i++) {
            final int index = i;
            timers[i] = new Timer(timerDurations[i], (ActionEvent e) -> {
                timer(index);
            });
            timers[i].setRepeats(false);
        }
        goToState(FusionMotorState.IDLE);
    }

    public static void main(String[] args) {
        FusionMotor main = new FusionMotor();
    }

    private void creer(Forme f) {
        switch (state) {
            case IDLE:
                goToState(FusionMotorState.CREATION);
                majValueForme(f);

                break;
            default:
                break;

        }
    }
    
    private void croix(){
        switch (state){
            case IDLE:
                goToState(FusionMotorState.SUPPRESSION);
        }
    }

    private void kwPosition() {
        switch (state) {
            case CREATION:
                goToState(FusionMotorState.CREATION_WAIT_FOR_CLIC_POSITION);
                break;
            case CREATION_WAIT_FOR_KEYWORD:
                if (creationColor != null) {
                    majPos(creationXY);
                    create();
                    goToState(FusionMotorState.IDLE);
                } else {
                    goToState(FusionMotorState.CREATION);
                    majPos(creationXY);
                }
                break;
        }
    }

    private void clic(Point pt) {
        switch (state) {
            case CREATION:
                creationXY = pt;
                goToState(FusionMotorState.CREATION_WAIT_FOR_KEYWORD);
                break;
            case CREATION_WAIT_FOR_CLIC_POSITION:
                if (creationColor == null) {
                    goToState(FusionMotorState.CREATION);
                    majPos(pt);
                } else {
                    goToState(FusionMotorState.IDLE);
                    majPos(pt);
                    create();
                    
                }
                break;
            case CREATION_WAIT_FOR_KEYWORD:
                goToState(FusionMotorState.CREATION_WAIT_FOR_KEYWORD);
                creationXY = pt;
                break;
            case CREATION_WAIT_FOR_CLIC_COLOR:
                if (creationPosition == null) {
                    goToState(FusionMotorState.CREATION);
                    creationColor = ivyInterface.fsmColor(pt.x, pt.y);
                } else {
                    goToState(FusionMotorState.IDLE);
                    creationColor = ivyInterface.fsmColor(pt.x, pt.y);
                    create();
                }
        }
    }

    private void kwColor(Color c) {
        switch (state) {
            case CREATION:
                if (creationPosition == null) {
                    creationColor = c;
                    goToState(FusionMotorState.CREATION);
                } else {
                    creationColor = c;
                    create();
                    goToState(FusionMotorState.IDLE);
                }
        }
    }

    private void kwDCC() {
        switch (state) {
            case CREATION:
                goToState(FusionMotorState.CREATION_WAIT_FOR_CLIC_COLOR);
                break;
            case CREATION_WAIT_FOR_KEYWORD:
                if (creationPosition == null) {
                    creationColor = ivyInterface.fsmColor(creationXY.x, creationXY.y);
                    goToState(FusionMotorState.CREATION);
                } else {
                    creationColor = ivyInterface.fsmColor(creationXY.x, creationXY.y);
                    create();
                    goToState(FusionMotorState.IDLE);
                }
                break;
        }
    }

    private void timer(int i) {
        switch (i) {
            case 0:
                if (state == FusionMotorState.CREATION_WAIT_FOR_KEYWORD) {
                    goToState(FusionMotorState.CREATION);
                }
                break;
            case 1:
                if (state == FusionMotorState.CREATION_WAIT_FOR_CLIC_POSITION) {
                    goToState(FusionMotorState.CREATION);
                }
                break;
            case 2:
                if (state == FusionMotorState.CREATION_WAIT_FOR_CLIC_COLOR) {
                    goToState(FusionMotorState.CREATION);
                }
                break;
            case 3:
                if (state == FusionMotorState.CREATION && creationPosition == null && creationColor != null) {
                    goToState(FusionMotorState.IDLE);
                    create();
                }
                break;
            case 4:
                if (state == FusionMotorState.CREATION && creationPosition != null && creationColor == null) {
                    goToState(FusionMotorState.IDLE);
                    create();
                }
                break;
            case 5:
                if (state == FusionMotorState.CREATION && creationPosition == null && creationColor == null) {
                    goToState(FusionMotorState.IDLE);
                    create();
                }
                break;
        }
    }

    private void majPos(Point pt) {
        creationPosition = pt;
    }

    private void majValueForme(Forme f) {
        creationValueForme = f;
    }

    private void goToState(FusionMotorState state) {
        if (this.state != null) {
            System.out.println("FM from " + this.state + " to " + state);
            switch (this.state) {
                case IDLE:
                    break;
                case CREATION:
                    timers[3].stop();
                    timers[4].stop();
                    timers[5].stop();
                    break;
                case CREATION_WAIT_FOR_KEYWORD:
                    timers[0].stop();
                    break;
                case CREATION_WAIT_FOR_CLIC_POSITION:
                    timers[1].stop();
                    break;
                case CREATION_WAIT_FOR_CLIC_COLOR:
                    timers[2].stop();
                    break;
                case DEPLACEMENT:
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1:
                    break;
                case DEPLACEMENT_WAIT_FOR_KEYWORD:
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1:
                    break;
                case DEPLACEMENT_WAIT_NEXT_SHAPE:
                    break;
                case DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE:
                    break;
                case DEPLACEMENT_WAIT_NEXT_POSITION:
                    break;
                case DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE:
                    break;
                case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                    break;
                case DEPLACEMENT_WAIT_CLIC_SHAPE_2:
                    break;
                case DEPLACEMENT_WAIT_CLIC_POSITION_2:
                    break;
                case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                    break;
                case DEPLACEMENT_COLOR:
                    break;
                case SUPPRESSION:
                    break;
                case SUPPRESSION_WAIT_CLIC:
                    break;
                case SUPPRESSION_WAIT_KEYWORD_OBJECT:
                    break;
                case SUPPRESSION_WAIT_COLOR:
                    break;
                case SUPPRESSION_WAIT_CLIC_UPDATE:
                    break;
                case SUPPRESSION_WAIT_KEYWORD_OBJECT_UPDATE:
                    break;
                default:
                    throw new AssertionError(this.state.name());

            }
        }
        this.state = state;
        switch (state) {
            case IDLE:
                reset();
                break;
            case CREATION:
                timers[3].restart();
                timers[4].restart();
                timers[5].restart();
                break;
            case CREATION_WAIT_FOR_KEYWORD:
                timers[0].restart();
                break;
            case CREATION_WAIT_FOR_CLIC_POSITION:
                timers[1].restart();
                break;
            case CREATION_WAIT_FOR_CLIC_COLOR:
                timers[2].restart();
                break;
            case DEPLACEMENT:
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1:
                break;
            case DEPLACEMENT_WAIT_FOR_KEYWORD:
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1:
                break;
            case DEPLACEMENT_WAIT_NEXT_SHAPE:
                break;
            case DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE:
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                break;
            case DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE:
                break;
            case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                break;
            case DEPLACEMENT_WAIT_CLIC_SHAPE_2:
                break;
            case DEPLACEMENT_WAIT_CLIC_POSITION_2:
                break;
            case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                break;
            case DEPLACEMENT_COLOR:
                break;
            case SUPPRESSION:
                break;
            case SUPPRESSION_WAIT_CLIC:
                break;
            case SUPPRESSION_WAIT_KEYWORD_OBJECT:
                break;
            case SUPPRESSION_WAIT_COLOR:
                break;
            case SUPPRESSION_WAIT_CLIC_UPDATE:
                break;
            case SUPPRESSION_WAIT_KEYWORD_OBJECT_UPDATE:
                break;
            default:
                throw new AssertionError(state.name());

        }
    }
    
    private void create(){
        Integer x = (creationPosition == null ? null : creationPosition.x);
        Integer y = (creationPosition == null ? null : creationPosition.y);
        this.ivyInterface.create(x, y, creationColor, creationValueForme);
    }
    
    private void reset(){
        creationColor = null;
        creationPosition = null;
        creationValueForme = null;
        creationXY = null;
        suppressionForme = null;
    }

}
