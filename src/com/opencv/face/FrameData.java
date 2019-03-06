/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opencv.face;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author User
 */
public class FrameData {
     private BufferedImage croppedImage;
     
     private List<FaceAttribute> icaoAttributes;

    public FrameData() {
    }
     

    public BufferedImage getCroppedImage() {
        return croppedImage;
    }

    public void setCroppedImage(BufferedImage croppedImage) {
        this.croppedImage = croppedImage;
    }

    public List<FaceAttribute> getIcaoAttributes() {
        return icaoAttributes;
    }

    public void setIcaoAttributes(List<FaceAttribute> icaoAttributes) {
        this.icaoAttributes = icaoAttributes;
    }
    
    
}
