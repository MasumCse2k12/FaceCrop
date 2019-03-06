/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opencv.face.model;

import com.opencv.face.FaceAttribute;
import com.opencv.webcame.utils.FaceAttributeId;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author User
 */
public class Config {
    public List<FaceAttribute> icaoAttributes = Arrays.asList(
        new FaceAttribute(FaceAttributeId.BLURNESS, "Blurness score"),
        new FaceAttribute(FaceAttributeId.SHARPNESS, "Sharpness score"),
        new FaceAttribute(FaceAttributeId.BRIGHTNESS, "Brightness score"),
        new FaceAttribute(FaceAttributeId.CONTRAST, "Contrast score"),
        new FaceAttribute(FaceAttributeId.NOSE, "Nose score"),
        new FaceAttribute(FaceAttributeId.EYE_STATUS_L, "Left eye status score"),
        new FaceAttribute(FaceAttributeId.EYE_STATUS_R, "Right eye status score"),  
        new FaceAttribute(FaceAttributeId.MOUTH_STATUS, "Mouth status score"),
        new FaceAttribute(FaceAttributeId.FACE_CONFIDENCE, "Face Score")    
        );  
    
}
