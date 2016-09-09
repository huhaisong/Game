package com.example.a111.game.model;

import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;


public class ModelEvent extends EventObject {

    public ModelEvent(Object source) {
        super(source);
    }

    private Vector repository = new Vector();
    private ModelListener dl;

    public void addListener(ModelListener dl) {
        repository.addElement(dl);//这步要注意同步问题
    }

    public void removeListener(ModelListener dl) {
        repository.remove(dl);//这步要注意同步问题
    }

    public void notifyEvent(ModelEvent event) {
        Enumeration aenum = repository.elements();//这步要注意同步问题
        while (aenum.hasMoreElements()) {
            dl = (ModelListener) aenum.nextElement();
            dl.onClick(event);
        }
    }


}