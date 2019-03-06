/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opencv.face;

import com.opencv.webcame.utils.FaceAttributeId;
import java.awt.image.BufferedImage;

/**
 *
 * @author User
 */
public class FaceAttribute implements Cloneable {

    private FaceAttributeId id;
    private String name;
    private float score;
   

    public FaceAttribute(FaceAttributeId id, String name) {
        this.id = id;
        this.name = name;
    }

    public FaceAttributeId getId() {
        return id;
    }

    public void setId(FaceAttributeId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

   
    @Override
    public Object clone() throws CloneNotSupportedException {
        FaceAttribute attribute = new FaceAttribute(this.id, this.name);
        attribute.setScore(0);

        return attribute;
    }

}
