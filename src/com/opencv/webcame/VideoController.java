package com.opencv.webcame;

import com.opencv.webcame.utils.Utils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;


public class VideoController implements Initializable {

    @FXML
    private ImageView liveImage;
    @FXML
    private ImageView image1;
    @FXML
    private ImageView image2;
    @FXML
    private ImageView image3;
    @FXML
    private Button save;
    @FXML
    private RadioButton selectOne;
    @FXML
    private RadioButton selectTwo;
    @FXML
    private RadioButton selectTHree;
    @FXML
    private Label message;
    
     private BufferedImage bufferedImageOne;
    private BufferedImage bufferedImageTwo;
    private BufferedImage bufferedImageThree;
    
    private ScheduledExecutorService timer;
    private VideoCapture capture;
    private boolean cameraActive = false;
    private int width = 640, height = 480;
    // face cascade classifier
    String file = "C:/opencv/opencv/build/etc/haarcascades/haarcascade_frontalface_alt.xml";
    private CascadeClassifier faceCascade = new CascadeClassifier(file);
    private int absoluteFaceSize;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
         if (!this.cameraActive) {
            capture = new VideoCapture(0);
            if (capture.isOpened()) {
                this.cameraActive = true;
                System.out.println("Camera is open");
                //grab a frame every 33ms(30 frames/sec)
                Runnable frameGrabber;
                frameGrabber = new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println(">>>>>>>>>>>run>>>>>>>>>");
                        try {
                            Mat frame = grabFrame();
                            if (!frame.empty()) {
                                detectAndDisplay(frame);
                            }
                            Image imageToShow = Utils.mat2Image(frame);
                            updateImageView(liveImage, imageToShow);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            } else {
                System.err.println("Failed to open camra connection.....");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            this.stopAcquisition();
        }
        
    }
    
    
    
     @FXML
    protected void saveAction(ActionEvent event) {
         boolean flag=false;
        try {
            if(selectOne.isSelected()){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageOne, "jpg", baos );
                baos.flush();
                byte[] image= baos.toByteArray();
                baos.close();
               
                flag=true;
                System.out.println(">>>W: "+bufferedImageOne.getWidth());
                System.out.println(">>>H: "+bufferedImageOne.getHeight());
                ImageIO.write(bufferedImageOne, "JPG",new File("output.JPG"));
            }
            
            if(selectTwo.isSelected()){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageTwo, "jpg", baos );
                baos.flush();
                byte[] image= baos.toByteArray();
                baos.close();
               
                flag=true;
            }
            
            if(selectTHree.isSelected()){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write( bufferedImageThree, "jpg", baos );
                baos.flush();
                byte[] image= baos.toByteArray();
                baos.close();
              
                flag=true;
            }
           
            
          this.stopAcquisition();
            

           
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
   
      
     @FXML
   protected void captureAction(ActionEvent event) throws IOException{
       Mat mat=grabFrame();
       BufferedImage image=cropingIMage(mat);
        if(image!=null){
             if(bufferedImageOne==null && bufferedImageThree==null && bufferedImageTwo==null){
            bufferedImageOne=cropingIMage(mat);//camera.downloadLiveView();
            //cropingIMage(bufferedImageToMat(camera.downloadLiveView()));
            image1.setImage(SwingFXUtils.toFXImage(bufferedImageOne, null));
        }else if(bufferedImageOne!=null && bufferedImageThree==null && bufferedImageTwo==null){
            bufferedImageTwo=cropingIMage(mat);
            image2.setImage(SwingFXUtils.toFXImage(bufferedImageTwo, null));
        }else if(bufferedImageOne!=null && bufferedImageThree!=null && bufferedImageTwo==null){
            bufferedImageTwo=cropingIMage(mat);
            image2.setImage(SwingFXUtils.toFXImage(bufferedImageTwo, null));
        }else if(bufferedImageOne!=null && bufferedImageThree==null && bufferedImageTwo!=null){
            bufferedImageThree=cropingIMage(mat);
            image3.setImage(SwingFXUtils.toFXImage(bufferedImageThree, null));
        }else{
             bufferedImageOne=cropingIMage(mat);
            image1.setImage(SwingFXUtils.toFXImage(bufferedImageOne, null));
        }
        }else{
            System.out.println("face not found");
        }
        
       
   }

    
     @FXML
   protected void deleteOneAction(ActionEvent event){
       image1.setImage(null);
       bufferedImageOne=null;
   }
   
   @FXML
   protected void deleteTwoAction(ActionEvent event){
       image2.setImage(null);
       bufferedImageTwo=null;
   }
   
   @FXML
   protected void deleteThreeAction(ActionEvent event){
       image3.setImage(null);
       bufferedImageThree=null;
   }
   
   
   @FXML
   protected void closeAction(ActionEvent event){
       
       try {
         
           this.stopAcquisition();
           Stage stage = (Stage) save.getScene().getWindow();
         stage.close();
         
          
       } catch (Exception e) {
           e.printStackTrace();
       }
   
   }



 /**
     * Get a frame from the opened video stream if any
     *
     * @return @linked Image to show
     *
     *
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                //read the current frame
                this.capture.read(frame);

            } catch (Exception exc) {
                System.err.println("Exception during the image elaboration" + exc);
            }
        }
        
//        System.out.println("height :"+frame.height());
//        System.out.println("weight :"+frame.width());

        return frame;
    }

    /**
     * Method for face detection and tracking
     */
    private void detectAndDisplay(Mat frame) {

        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        //convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        //equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

       // System.out.println(String.format("Detected %s faces", faces.toArray().length));
        

        // each rectangle in faces is a face: draw them!
        Rect rect_Crop=null;
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            rect_Crop = new Rect(facesArray[i].x-50, facesArray[i].y-100, facesArray[i].width+100, facesArray[i].height+175);
            Size s= new Size();
            s=rect_Crop.size();
            System.out.println(">>>"+s);
            System.out.println(">height>>"+s.height);
            System.out.println(">width>>"+s.width);
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }
        
//        Rect rect_Crop=null;
//            Rect[] facesArray = faces.toArray();
//            for (int i = 0; i < facesArray.length; i++) {
//                rect_Crop = new Rect(facesArray[i].x-50, facesArray[i].y-100, facesArray[i].width+100, facesArray[i].height+175);
//            }

    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
//                        this.capture.
                        this.cameraActive=false ;
                        System.out.println(">>>>close>>>");
		}
        
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    public void setClosed() {
        this.stopAcquisition();
    }
    
    
     public BufferedImage cropingIMage(Mat image) throws IOException {
        BufferedImage out=null;
        
        if(image!=null){
            MatOfRect faces = new MatOfRect();
            Mat grayFrame = new Mat();
            // convert the frame in gray scale
            Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
            // equalize the frame histogram to improve the result
            Imgproc.equalizeHist(grayFrame, grayFrame);
            
                //    filtering 
         Imgproc.GaussianBlur(image, grayFrame, new org.opencv.core.Size(0, 0), 10); 
         Core.addWeighted(image, 1.5, grayFrame, -0.5, 0, grayFrame); 

            // compute minimum face size (20% of the frame height, in our case)
            if (this.absoluteFaceSize == 0) {
                int height = grayFrame.rows();
                if (Math.round(height * 0.2f) > 0) {
                    this.absoluteFaceSize = Math.round(height * 0.2f);
                }
            }

            // detect faces
            this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                    new org.opencv.core.Size(this.absoluteFaceSize, this.absoluteFaceSize), new org.opencv.core.Size());
            // each rectangle in faces is a face: draw them!
            Rect rect_Crop=null;
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                rect_Crop = new Rect(facesArray[i].x-50, facesArray[i].y-100, facesArray[i].width+100, facesArray[i].height+175);
            }
            try {
                System.out.println(">>>"+faces.dataAddr());
                
                if(faces.dataAddr()!=0){
                    System.out.println(">>>image>>"+image.size());
                    System.out.println(">>>rect_crop>>"+rect_Crop.area());
                    System.out.println(">>>rect_crop>>"+rect_Crop.size());
                    Mat image_roi = new Mat(image,rect_Crop);
                    out=getBufferedImageFromMat(image_roi);
            }else{
                System.out.println("face not found");
                System.out.println(">>>image>>"+image.size());
                    System.out.println(">>>rect_crop>>"+rect_Crop.area());
                    System.out.println(">>>rect_crop>>"+rect_Crop.size());
            }
            } catch (Exception e) {
                System.out.println("face not found");
            }
            System.out.println(">>>return");
            return  out;
        }
        return out;
    }
    
    
    public static BufferedImage getBufferedImageFromMat(Mat mat) throws IOException{
        MatOfByte mbyt= new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mbyt);
        byte ba[]=mbyt.toArray();
        BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }
    
}
