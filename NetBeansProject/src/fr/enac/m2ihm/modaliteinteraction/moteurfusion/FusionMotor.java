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
                                            4500, 4500, 4500, 3000,     // Timer 10, end supression
                                            4500, 4500, 4500, 4500, 5000, 5000, 4500, 4500, //Timer 18
                                          4500, 4500, 3000, 4500, 4500 //Timer 23 end deplacement
    };

    private final FusionMotorIvyInterface ivyInterface;
    private FusionMotorState state;

    private Forme creationValueForme;
    private Point creationPosition;
    private Point creationXY;
    private Color creationColor;
    
    private Forme suppressionValueForme;
    private Point suppressionXY;
    private Color suppressionColor;
    
    private Point moveXY; //Used to store temporarly position (when a clic occurs but dunno for what)
    private Forme moveValueForme;
    private Forme moveTempForme;
    private Point movePosForme;
    private Point moveDestPoint;
    private Color moveColor;
    
    private final Timer[] timers;
    
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
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.keywordObjectEvent, (evt) -> {
            kwObject((Forme) evt.getNewValue());
        });
        ivyInterface.addPropertyChangeListener(FusionMotorIvyInterface.moveEvent, (evt) -> {
            trait();
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
                majValueFormeCrea(f);

                break;
            default:
                break;

        }
    }
    
    private void croix(){
        switch (state){
            case IDLE:
                goToState(FusionMotorState.SUPPRESSION);
                break;
        }
    }
    
    private void trait(){
        switch (state){
            case IDLE:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
        }
    }

    private void kwPosition() {
        switch (state) {
            case CREATION:
                goToState(FusionMotorState.CREATION_WAIT_FOR_CLIC_POSITION);
                break;
            case CREATION_WAIT_FOR_KEYWORD:
                if (creationColor != null) {
                    majPosCrea(creationXY);
                    create();
                    goToState(FusionMotorState.IDLE);
                } else {
                    goToState(FusionMotorState.CREATION);
                    majPosCrea(creationXY);
                }
                break;
            case DEPLACEMENT:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1);
                break;
            case DEPLACEMENT_WAIT_FOR_KEYWORD:
                moveDestPoint = moveXY;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case DEPLACEMENT_WAIT_NEXT_SHAPE:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE);
                break;
            case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                moveDestPoint = moveXY;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_FOR_CLIC_POSITION_2);
                break;
            case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                move();
                goToState(FusionMotorState.IDLE);
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
                    majPosCrea(pt);
                } else {
                    majPosCrea(pt);
                    create();                    
                    goToState(FusionMotorState.IDLE);
                    
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
                    creationColor = ivyInterface.fsmColor(pt.x, pt.y);
                    create();
                    goToState(FusionMotorState.IDLE);
                }
                break;
            case SUPPRESSION:
                suppressionXY = pt;
                goToState(FusionMotorState.SUPPRESSION_WAIT_KEYWORD_OBJECT);
                break;
            case SUPPRESSION_WAIT_CLIC:
                suppressionXY = pt;
                goToState(FusionMotorState.SUPPRESSION_WAIT_COLOR);
                break;
            case SUPPRESSION_WAIT_COLOR:
                suppressionXY = pt;
                goToState(FusionMotorState.SUPPRESSION_WAIT_KEYWORD_OBJECT_UPDATE);
                break;
            case SUPPRESSION_WAIT_CLIC_UPDATE:
                suppressionXY = pt;
                goToState(FusionMotorState.SUPPRESSION_WAIT_COLOR);
                break;
            case DEPLACEMENT:
                moveXY = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_FOR_KEYWORD);
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1:
                moveDestPoint = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1:
                moveValueForme = moveTempForme;
                movePosForme = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE:
                moveDestPoint = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case DEPLACEMENT_WAIT_NEXT_SHAPE:
                moveXY = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_KEYWORD_OBJECT);
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                moveXY = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_KEYWORD_POSITION);
                break;
            case DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE:
                moveValueForme = moveTempForme;
                movePosForme = pt;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_2:
                moveValueForme = moveTempForme;
                movePosForme = pt;
                goToState(FusionMotorState.DEPLACEMENT_COLOR);
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_2:
                moveDestPoint = pt;
                move();
                goToState(FusionMotorState.IDLE);
                break;
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
                break;
            case SUPPRESSION_WAIT_COLOR:
                suppressionColor = c;
                suppression();
                goToState(FusionMotorState.IDLE);
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                moveColor = c;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case DEPLACEMENT_COLOR:
                moveColor = c;
                move();
                goToState(FusionMotorState.IDLE);
                break;
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
    
    private void kwObject(Forme f){
        switch (state){
            case SUPPRESSION:
                majValueFormeSuppr(f);
                goToState(FusionMotorState.SUPPRESSION_WAIT_CLIC);
                break;
            case SUPPRESSION_WAIT_KEYWORD_OBJECT:
                majValueFormeSuppr(f);
                goToState(FusionMotorState.SUPPRESSION_WAIT_COLOR);
                break;
            case SUPPRESSION_WAIT_COLOR:
                majValueFormeSuppr(f);
                goToState(FusionMotorState.SUPPRESSION_WAIT_CLIC_UPDATE);
                break;
            case SUPPRESSION_WAIT_KEYWORD_OBJECT_UPDATE:
                majValueFormeSuppr(f);
                goToState(FusionMotorState.SUPPRESSION_WAIT_COLOR);
                break;
            case DEPLACEMENT:
                moveTempForme = f;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1);
                break;
            case DEPLACEMENT_WAIT_FOR_KEYWORD:
                moveValueForme = f;
                movePosForme = moveXY;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case DEPLACEMENT_WAIT_NEXT_SHAPE:
                moveTempForme = f;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_2);
                break;
            case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                moveValueForme = f;
                movePosForme = moveXY;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                moveTempForme = f;
                goToState(FusionMotorState.DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE);
                break;
            case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                moveValueForme = f;
                movePosForme = moveXY;
                goToState(FusionMotorState.DEPLACEMENT_COLOR);
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
            case 6: //TODO : For all timers checks states !!!!
                goToState(FusionMotorState.IDLE);
                break;
            case 7:
                goToState(FusionMotorState.SUPPRESSION);
                break;
            case 8:
                goToState(FusionMotorState.SUPPRESSION);
                break;
            case 9:
                suppression();
                goToState(FusionMotorState.IDLE);
                break;
            case 10:
                goToState(FusionMotorState.IDLE);
                break;
            case 11:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
            case 12:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
            case 13:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
            case 14:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
            case 15:
                goToState(FusionMotorState.DEPLACEMENT);
                break;
            case 16:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case 17:
                if (state == FusionMotorState.DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE){
                    goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                }
                break;
            case 18:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case 19:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_SHAPE);
                break;
            case 20:
                moveColor = null;
                move();
                goToState(FusionMotorState.IDLE);
                break;
            case 21:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
            case 22:
                goToState(FusionMotorState.DEPLACEMENT_WAIT_NEXT_POSITION);
                break;
        }
    }

    private void majPosCrea(Point pt) {
        creationPosition = pt;
    }


    private void majValueFormeCrea(Forme f) {
        creationValueForme = f;
    }
    private void majValueFormeSuppr(Forme f){
        suppressionValueForme = f;
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
                    timers[10].stop();
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1:
                    timers[11].stop();
                    break;
                case DEPLACEMENT_WAIT_FOR_KEYWORD:
                    timers[12].stop();
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1:
                    timers[13].stop();
                    break;
                case DEPLACEMENT_WAIT_NEXT_SHAPE:
                    timers[14].stop();
                    break;
                case DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE:
                    timers[16].stop();
                    break;
                case DEPLACEMENT_WAIT_NEXT_POSITION:
                    timers[15].stop();
                    break;
                case DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE:
                    timers[17].stop();
                    break;
                case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                    timers[18].stop();
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_2:
                    timers[19].stop();
                    break;
                case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_2:
                    timers[21].stop();
                    break;
                case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                    timers[22].stop();
                    break;
                case DEPLACEMENT_COLOR:
                    timers[20].stop();
                    break;
                case SUPPRESSION:
                    timers[6].stop();
                    break;
                case SUPPRESSION_WAIT_CLIC:
                    timers[7].stop();
                    break;
                case SUPPRESSION_WAIT_KEYWORD_OBJECT:
                    timers[8].stop();
                    break;
                case SUPPRESSION_WAIT_COLOR:
                    timers[9].stop();
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
                timers[10].restart();
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_1:
                timers[11].restart();
                break;
            case DEPLACEMENT_WAIT_FOR_KEYWORD:
                timers[12].restart();
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_1:
                timers[13].restart();
                break;
            case DEPLACEMENT_WAIT_NEXT_SHAPE:
                timers[14].restart();
                break;
            case DEPLACEMENT_WAIT_CLIC_POSITION_UPDATE:
                timers[16].restart();
                break;
            case DEPLACEMENT_WAIT_NEXT_POSITION:
                timers[15].restart();
                break;
            case DEPLACEMENT_WAIT_CLIC_SHAPE_UPDATE:
                timers[17].restart();
                break;
            case DEPLACEMENT_WAIT_KEYWORD_OBJECT:
                timers[18].restart();
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_SHAPE_2:
                timers[19].restart();
                break;
            case DEPLACEMENT_WAIT_FOR_CLIC_POSITION_2:
                timers[21].restart();
                break;
            case DEPLACEMENT_WAIT_KEYWORD_POSITION:
                timers[22].restart();
                break;
            case DEPLACEMENT_COLOR:
                timers[20].restart();
                break;
            case SUPPRESSION:
                timers[6].restart();
                break;
            case SUPPRESSION_WAIT_CLIC:
                timers[7].restart();
                break;
            case SUPPRESSION_WAIT_KEYWORD_OBJECT:
                timers[8].restart();
                break;
            case SUPPRESSION_WAIT_COLOR:
                timers[9].restart();
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
    
    private void suppression(){
        this.ivyInterface.delete(suppressionXY.x, suppressionXY.y, suppressionValueForme, suppressionColor);
    }
    
    private void move(){
        
    }
    
    private void reset(){
        creationColor = null;
        creationPosition = null;
        creationValueForme = null;
        creationXY = null;
        suppressionValueForme = null;
        suppressionColor = null;
        suppressionXY = null;
        moveColor = null;
        moveDestPoint = null;
        movePosForme = null;
        moveValueForme = null;
        moveXY = null;
        moveTempForme = null;
    }

}
