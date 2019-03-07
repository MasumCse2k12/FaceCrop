package com.opencv.webcame;

import com.opencv.face.FaceAttribute;
import com.opencv.face.FrameData;
import com.opencv.face.model.Config;
import com.opencv.webcame.utils.Utils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.core.CvType.CV_8UC1;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

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
    @FXML
    private Button thumbnailBtnOne;
    @FXML
    private Button thumbnailBtnTwo;
    @FXML
    private Button thumbnailBtnThree;

    private List<Button> thumbnailButtons;
    private BufferedImage bufferedImageOne;
    private BufferedImage bufferedImageTwo;
    private BufferedImage bufferedImageThree;
    private Map<String, FrameData> capturedImages;

    private ScheduledExecutorService timer;
    private VideoCapture capture;
    private FrameData frameData;
    private boolean cameraActive = false;
    private int width = 640, height = 480;
    private BackgroundSubtractor backSub;

    private int cropX = 0;
    private int cropY = 0;
    private int cropWidth = 0;
    private int cropHeight = 0;
    private float mouth_score = 0;
    private float nose_score = 0;
    private float face_score = 0;
    private float eye_score = 0;
    // face cascade classifier
    String file = "C:/opencv/opencv/build/etc/haarcascades/haarcascade_frontalface_alt.xml";
    String face_file = "G:\\opencv\\build\\etc\\haarcascades\\haarcascade_frontalface_alt.xml";
    String eye_file = "G:\\opencv\\build\\etc\\haarcascades\\haarcascade_eye_tree_eyeglasses.xml";
    String eye_left = "G:\\opencv\\build\\etc\\haarcascades\\haarcascade_lefteye_2splits.xml";
    String eye_right = "G:\\opencv\\build\\etc\\haarcascades\\haarcascade_righteye_2splits.xml";
    String nose_file = "G:\\opencv\\build\\etc\\haarcascades\\haarcascade_mcs_nose.xml";
    String mouth_file = "G:/opencv/build/etc/haarcascades/haarcascade_mcs_mouth.xml";
    private CascadeClassifier faceCascade = new CascadeClassifier(face_file);
    private CascadeClassifier eyesCascade = new CascadeClassifier(eye_file);
    private CascadeClassifier eyesCascadeL = new CascadeClassifier(eye_left);
    private CascadeClassifier eyesCascadeR = new CascadeClassifier(eye_right);
    private CascadeClassifier noseCascade = new CascadeClassifier(nose_file);
    private CascadeClassifier mouthCascade = new CascadeClassifier(mouth_file);
    private int absoluteFaceSize;
    Mat oldFrame;
    boolean isCrop = false;
    int binSize = 16;
    int uniformity_score =0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        thumbnailButtons = Arrays.asList(this.thumbnailBtnOne, this.thumbnailBtnTwo, this.thumbnailBtnThree);

        for (Button nowBtn : this.thumbnailButtons) {
//            this.setButtonGraphic(nowBtn, "/com/opencv/face/resource/thumbnail.jpg");
            nowBtn.setOnAction(this::handleThumbnailPreview);
        }

        capturedImages = new HashMap<String, FrameData>();

        backSub = Video.createBackgroundSubtractorMOG2();

        backSub = Video.createBackgroundSubtractorKNN();

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

    private void setButtonGraphic(Button btn, String image) {
        Image graphic = new Image(image);
        this.setButtonGraphic(btn, graphic);
    }

    private void setButtonGraphic(Button btn, Image image) {
        ImageView iv = new ImageView();
        iv.setFitWidth(btn.getWidth());
        iv.setFitHeight(btn.getHeight());
        iv.setPreserveRatio(true);
        btn.setGraphic(new ImageView(image));
    }

    private void handleThumbnailPreview(ActionEvent event) {
        Button sourceBtn = null;
        FrameData frameData = null;
//        if(selectOne.isSelected())
//        {
//            frameData = this.capturedImages.get(selectOne.getId());
//        }
//        else if (selectTwo.isSelected()){
//            frameData = this.capturedImages.get(selectTwo.getId());
//        }
//        else if (selectTHree.isSelected()){
//            frameData = this.capturedImages.get(selectTHree.getId());
//        }

        if (event.getSource() instanceof Button) {
            sourceBtn = (Button) event.getSource();
            frameData = this.capturedImages.get(sourceBtn.getId());
        }
        if (frameData == null) {
            System.err.println("No frame data found.>>>>");
            return;
        }

        Stage s = new Stage(StageStyle.DECORATED);
        s.initModality(Modality.WINDOW_MODAL);
        s.setResizable(false);

        s.setTitle("Preview");
        Parent root = null;
        try {

            FXMLLoader loader = new FXMLLoader();
            root = loader.load(getClass().getResource("/com/opencv/face/view/ThumbnailPreview.fxml").openStream());
            Object controller = loader.getController();
            if (controller instanceof ThumbnailPreviewController) {
                ThumbnailPreviewController tc = (ThumbnailPreviewController) controller;
                tc.setFrameData(frameData);
                tc.setStage(s);
                tc.drawImage();
                tc.decorateICAOTable();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        s.setScene(scene);
        s.show();
    }

    @FXML
    protected void saveAction(ActionEvent event) {
        boolean flag = false;
        try {
            if (selectOne.isSelected()) {

//                this.capturedImages.put(selectOne.getId(), frameData);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageOne, "jpg", baos);
                baos.flush();
                byte[] image = baos.toByteArray();
                baos.close();

                flag = true;
                System.out.println(">>>W: " + bufferedImageOne.getWidth());
                System.out.println(">>>H: " + bufferedImageOne.getHeight());
                ImageIO.write(bufferedImageOne, "JPG", new File("output.JPG"));
            }

            if (selectTwo.isSelected()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageTwo, "jpg", baos);
                baos.flush();
                byte[] image = baos.toByteArray();
                baos.close();

                flag = true;
            }

            if (selectTHree.isSelected()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageThree, "jpg", baos);
                baos.flush();
                byte[] image = baos.toByteArray();
                baos.close();

                flag = true;
            }

            this.stopAcquisition();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    protected void captureAction(ActionEvent event) throws IOException {
        Mat mat = grabFrame();
        BufferedImage image = cropingIMage(mat);
        isCrop = true;
        if (image != null) {
            if (bufferedImageOne == null && bufferedImageThree == null && bufferedImageTwo == null) {
                bufferedImageOne = cropingIMage(mat);//camera.downloadLiveView();
                this.capturedImages.put(thumbnailBtnOne.getId(), frameData);
                //cropingIMage(bufferedImageToMat(camera.downloadLiveView()));
                image1.setImage(SwingFXUtils.toFXImage(bufferedImageOne, null));
            } else if (bufferedImageOne != null && bufferedImageThree == null && bufferedImageTwo == null) {
                bufferedImageTwo = cropingIMage(mat);
                this.capturedImages.put(thumbnailBtnTwo.getId(), frameData);
                image2.setImage(SwingFXUtils.toFXImage(bufferedImageTwo, null));
            } else if (bufferedImageOne != null && bufferedImageThree != null && bufferedImageTwo == null) {
                bufferedImageTwo = cropingIMage(mat);
                this.capturedImages.put(thumbnailBtnTwo.getId(), frameData);
                image2.setImage(SwingFXUtils.toFXImage(bufferedImageTwo, null));
            } else if (bufferedImageOne != null && bufferedImageThree == null && bufferedImageTwo != null) {
                bufferedImageThree = cropingIMage(mat);
                this.capturedImages.put(thumbnailBtnThree.getId(), frameData);
                image3.setImage(SwingFXUtils.toFXImage(bufferedImageThree, null));
            } else {
                bufferedImageOne = cropingIMage(mat);
                this.capturedImages.put(thumbnailBtnOne.getId(), frameData);
                image1.setImage(SwingFXUtils.toFXImage(bufferedImageOne, null));
            }
        } else {
            System.out.println("face not found");
        }
        isCrop = false;
    }

    @FXML
    protected void deleteOneAction(ActionEvent event) {
        image1.setImage(null);
        bufferedImageOne = null;
    }

    @FXML
    protected void deleteTwoAction(ActionEvent event) {
        image2.setImage(null);
        bufferedImageTwo = null;
    }

    @FXML
    protected void deleteThreeAction(ActionEvent event) {
        image3.setImage(null);
        bufferedImageThree = null;
    }

    @FXML
    protected void closeAction(ActionEvent event) {

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

//                frame = this.doBackgroundRemovalAbsDiff(frame);
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
        MatOfInt rejectLevels = new MatOfInt();
        MatOfDouble levelWeights = new MatOfDouble();
        float scale_factor = 1.1f;
        int min_neighbors = 3;

        int totalHeight = (int) Math.ceil(absoluteFaceSize / 0.65);
        int totalWidth = (int) Math.ceil(absoluteFaceSize / 0.65);
        double upperFacePart = 0.40;
        double lowerFacePart = 0.35;
        double leftFacePart = 0.20;
        double rightFacePart = 0.20;
        Rect rectCrop = null;
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
//        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
//                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
        this.faceCascade.detectMultiScale3(grayFrame, faces, rejectLevels, levelWeights, scale_factor, min_neighbors, 0, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size(), cameraActive);
        // System.out.println(String.format("Detected %s faces", faces.toArray().length));
        // each rectangle in faces is a face: draw them!
//        Rect rect_Crop = null;
        int[] scoreFace = new int[5];
        double[] weight = new double[5];
        if (!rejectLevels.empty()) {
            scoreFace = rejectLevels.toArray();
        }
        if (!levelWeights.empty()) {
            weight = levelWeights.toArray();
        }
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {

            if ((facesArray[i].x - (int) Math.ceil(leftFacePart * totalWidth)) >= 0 && (facesArray[i].y - (int) Math.ceil(upperFacePart * totalHeight)) >= 0 && (facesArray[i].width + 2 * ((int) Math.ceil(rightFacePart * totalWidth))) <= (frame.width() - facesArray[i].x + (int) Math.ceil(rightFacePart * totalWidth)) && (facesArray[i].height + (int) Math.ceil(lowerFacePart * totalHeight) + (int) Math.ceil(lowerFacePart * totalHeight)) <= (frame.height() - facesArray[i].y + (int) Math.ceil(lowerFacePart * totalHeight))) {
                this.cropX = facesArray[i].x - (int) Math.ceil(leftFacePart * totalWidth);
                this.cropY = facesArray[i].y - (int) Math.ceil(upperFacePart * totalHeight);
                this.cropWidth = facesArray[i].width + 2 * ((int) Math.ceil(rightFacePart * totalWidth));
                this.cropHeight = facesArray[i].height + (int) Math.ceil(lowerFacePart * totalHeight) + (int) Math.ceil(lowerFacePart * totalHeight);

            } else {
                setCroppedImagePoint(facesArray[i].x, facesArray[i].y, facesArray[i].width, facesArray[i].height);

            }
            rectCrop = new Rect(this.cropX, this.cropY, this.cropWidth, this.cropHeight);
            Mat markedImage = new Mat();
            if (rectCrop != null) {
                markedImage = new Mat(frame, rectCrop);
//                this.doBackgroundRemovalAbsDiff(markedImage);
//                backgroundSubtraction(markedImage);
                Mat edgeImage = doSobel(markedImage);
                Mat edgeHist = new Mat();
                Imgcodecs.imwrite("D://Sobel.jpg", edgeImage);
                Imgcodecs.imwrite("D://Canny.jpg", doCanny(markedImage));
                findContour(markedImage);
                Imgproc.equalizeHist(edgeImage, edgeHist);
                Imgcodecs.imwrite("D://Sobel_hist.jpg", edgeHist);
            }

            Point showScoreP = new Point(20, 20);
            String score = "Captured Face  Score : " + weight[i];
            Imgproc.putText(frame, score, showScoreP, 1, 1, new Scalar(0, 255, 0), 2);
//            System.err.println("rejectLevels Score > " + scoreFace[i] + " levelWeights > " + weight[i]);
//            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

            Mat faceROI = grayFrame.submat(facesArray[i]);

//            Mat cropFace = new Mat();
//            int p=0;
//            int q=0;
//            for (int r = facesArray[i].x; r < facesArray[i].x + facesArray[i].width; r++) {
//                q=0;
//                for (int c = facesArray[i].y; c < facesArray[i].y + facesArray[i].height; c++) {
//
//                    double[] pixelValue = grayFrame.get(r, c);
//                    
//                    cropFace.put(p, q++, pixelValue);
//
//                }
//                p++;
//            }
            Imgcodecs.imwrite("D:\\faceROI.jpg", faceROI);
            int scoreIndex = 0;

            // -- In each face, detect eyes
            MatOfRect eyes = new MatOfRect();

            int eye_distance = 0;
            Point oldP = new Point();
//            this.eyesCascade.detectMultiScale(faceROI, eyes, 1.1, 3, 0);
            this.eyesCascade.detectMultiScale3(faceROI, eyes, rejectLevels, levelWeights, 1.1, 3, 0, new Size(5, 5), new Size(), cameraActive);
            List<Rect> listOfEyes = eyes.toList();
            for (Rect eye : listOfEyes) {
                int leftEyeX = 0;
                int leftEyeY = 0;
                if (scoreIndex == 0) {
                    leftEyeX = eye.x;
                    leftEyeY = eye.y;
                }
                if (scoreIndex >= 1) {
                    if (eye.x - leftEyeX != 0) {
//                        System.err.println("Face rotation : " + Math.tan((leftEyeY - eye.y) / (leftEyeX - eye.x)));
                    }
                }

                Point eyeCenter = new Point(facesArray[i].x + eye.x + eye.width / 2, facesArray[i].y + eye.y + eye.height / 2);
                int radius = (int) Math.round((eye.width + eye.height) * 0.25);
                Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 2);

                if (oldP == null) {
                    oldP = eyeCenter;
                }
                if (oldP != null) {
//                   eye_distance =  (int) Math.hypot(eyeCenter.x - oldP.x, eyeCenter.y - oldP.y);
                    eye_distance = (int) Math.sqrt(Math.pow(eyeCenter.x - oldP.x, 2) + Math.pow(eyeCenter.y - oldP.y, 2) * 1.0);
                }

                oldP = eyeCenter;
                eye_score = rejectLevels.toArray()[scoreIndex++];
                String eyeScore = "Eye Distance : (" + oldP.x + "," + oldP.y + "),(" + eyeCenter.x + "," + eyeCenter.y + ") : " + eye_distance;
                Imgproc.putText(frame, eyeScore, new Point(20, 35), 1, 1, new Scalar(255, 0, 0), 2);

            }

            // Detect nose if classifier provided by the user circle(ROI, Point(n.x + n.width / 2, n.y + n.height / 2), 3, Scalar(0, 255, 0), -1, 8);
            MatOfRect nose = new MatOfRect();
            scoreIndex = 0;
//            this.noseCascade.detectMultiScale(faceROI, nose, 1.1, 3, 0);
            this.noseCascade.detectMultiScale3(faceROI, nose, rejectLevels, levelWeights, 1.1, 3, 0, new Size(5, 5), new Size(), cameraActive);
            List<Rect> listOfNose = nose.toList();
            double nose_center_height = 0.0;
            for (Rect n : listOfNose) {
                String noseScore = "Nose SCore : " + rejectLevels.toArray()[scoreIndex++];
                Imgproc.putText(frame, noseScore, new Point(20, 50), 1, 1, new Scalar(255, 0, 255), 2);
                Point noseCenter = new Point(facesArray[i].x + n.x + n.width / 2, facesArray[i].y + n.y + n.height / 2);
                nose_center_height = facesArray[i].y + n.y + n.height / 2;
                int radius = (int) Math.round((n.width + n.height) * 0.25);

//                Imgproc.circle(frame, noseCenter, radius, new Scalar(255, 0, 255), 4);
            }

            // Detect mouth if classifier provided by the user
            MatOfRect mouth = new MatOfRect();
//            this.mouthCascade.detectMultiScale(faceROI, mouth,1.1, 3, 0);
            this.mouthCascade.detectMultiScale3(faceROI, mouth, rejectLevels, levelWeights, 1.1, 3, 0, new Size(), new Size(), cameraActive);
            double mouth_center_height = 0.0;
            Rect[] listOfMouth = mouth.toArray();
            if (listOfMouth.length >= 1) {
                for (i = 0; i < 1; i++) {
//                    String mouthScore = "Mouth SCore : " + rejectLevels.toArray()[0];
//                    Imgproc.putText(frame, mouthScore, new Point(20, 65), 1, 1, new Scalar(0, 255, 255), 2);
                    mouth_center_height = facesArray[i].y + listOfMouth[i].y + listOfMouth[i].height / 2;
                    Point mouthCenter = new Point(facesArray[i].x + listOfMouth[i].x + listOfMouth[i].width / 2, facesArray[i].y + listOfMouth[i].y + listOfMouth[i].height / 2);
                    int radius = (int) Math.round((listOfMouth[i].width + listOfMouth[i].height) * 0.25);
//                    Imgproc.circle(frame, new Point(listOfMouth[i].x + listOfMouth[i].width / 2, listOfMouth[i].y + listOfMouth[i].height / 2), 3, new Scalar(0, 255, 0), -1, 8);
//                    Imgproc.circle(frame, mouthCenter, radius, new Scalar(0, 255, 255), 4);
                    if (mouth_center_height > nose_center_height) {
//                        Imgproc.rectangle(frame, new Point(facesArray[i].x + listOfMouth[i].x, facesArray[i].y + listOfMouth[i].y), new Point(facesArray[i].x + listOfMouth[i].x + listOfMouth[i].width, facesArray[i].y + listOfMouth[i].y + listOfMouth[i].height), new Scalar(0, 255, 255), 1, 4);
                        String mouthScore = "Mouth SCore : " + rejectLevels.toArray()[0];
                        Imgproc.putText(frame, mouthScore, new Point(20, 65), 1, 1, new Scalar(0, 255, 255), 2);
                    } else if (mouth_center_height <= nose_center_height) {
                        continue;
                    } else {
//                        Imgproc.rectangle(frame, new Point(facesArray[i].x + listOfMouth[i].x, facesArray[i].y + listOfMouth[i].y), new Point(facesArray[i].x + listOfMouth[i].x + listOfMouth[i].width, facesArray[i].y + listOfMouth[i].y + listOfMouth[i].height), new Scalar(0, 255, 255), 1, 4);
                        String mouthScore = "Mouth SCore : " + rejectLevels.toArray()[0];
                        Imgproc.putText(frame, mouthScore, new Point(20, 65), 1, 1, new Scalar(0, 255, 255), 2);
                    }
//                    Imgproc.rectangle(frame, new Point(mouthCenter.x - listOfMouth[i].width / 2, mouthCenter.y), new Point(mouthCenter.x + listOfMouth[i].width / 2, mouthCenter.y + listOfMouth[i].height), new Scalar(0, 255, 255), 1, 4);
                }
            }

        }
//        System.err.println("******************* >>>>>>>>>>>>>>>>>>>>>>>>");
//        Rect rect_Crop=null;
//            Rect[] facesArray = faces.toArray();
//            for (int i = 0; i < facesArray.length; i++) {
//                rect_Crop = new Rect(facesArray[i].x-50, facesArray[i].y-100, facesArray[i].width+100, facesArray[i].height+175);
//            }
    }
    boolean useMOG2 = true;

    private Mat doSobel(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        // reduce noise with a 3x3 kernel
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Gradient X
        // Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0, 3, scale,
        // this.threshold.getValue(), Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0);
        Core.convertScaleAbs(grad_x, abs_grad_x);

        // Gradient Y
        // Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1, 3, scale,
        // this.threshold.getValue(), Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1);
        Core.convertScaleAbs(grad_y, abs_grad_y);

        // Total Gradient (approximate)
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, detectedEdges);
        // Core.addWeighted(grad_x, 0.5, grad_y, 0.5, 0, detectedEdges);

        return detectedEdges;

    }

    private Mat doCanny(Mat frame) {

        // init
        Mat grayImage = new Mat();

        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, 60, 180);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();

        frame.copyTo(dest, detectedEdges);

        return dest;

    }

    private void findContour(Mat image) {
        double maxVal = 0;
        int maxValIdx = 0;
        Mat imagegrey = new Mat();
        Mat imageBlurr = new Mat();
        Mat imageThr = new Mat();
        MatOfPoint max_contour = new MatOfPoint();

        Imgproc.cvtColor(image, imagegrey, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("D:/imagegrey.png", imagegrey);
        Imgproc.GaussianBlur(imagegrey, imageBlurr, new Size(3, 3), 0);
        Imgcodecs.imwrite("D:/imageBlurr.png", imageBlurr);
//        Imgproc.threshold(imageBlurr, imageA,25, 255,Imgproc.THRESH_BINARY); //Threshold the gray
        Imgproc.adaptiveThreshold(imageBlurr, imageThr, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint contour = new MatOfPoint();
        Imgproc.findContours(imageThr, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contours.size(); i++) {

            double contourArea = Imgproc.contourArea(contours.get(i));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = i;
            }
//            if (Imgproc.contourArea(contours.get(i)) > 50) {
//                Rect rect = Imgproc.boundingRect(contours.get(i));
//                if ((rect.height > 35 && rect.height < 60) && (rect.width > 35 && rect.width < 60)) {
//                    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
//                }
//            }
        }
//        System.out.println(" ***************************************************");
//        Point[] pointC = contours.get(maxValIdx).toArray();
//        for(int i=0; i< pointC.length ; i++){
//            System.err.println(" contour > " + pointC[i]);
//        }
//        System.out.println(" ***************************************************");
//        double epsilon = 0.1 * Imgproc.arcLength(new MatOfPoint2f(max_contour.toArray()), true);
//        MatOfPoint2f approx = new MatOfPoint2f();
//        Imgproc.approxPolyDP(new MatOfPoint2f(max_contour.toArray()), approx, epsilon, true);
        Imgproc.drawContours(image, contours, maxValIdx, new Scalar(0, 255, 0), 2);

        Imgcodecs.imwrite("D:/Contour.png", image);
    }

    private Mat doBackgroundRemovalAbsDiff(Mat currFrame) {
        Mat greyImage = new Mat();
        Mat foregroundImage = new Mat();
        Mat backgroundImage = new Mat();

        if (backgroundImage == null) {
            backgroundImage = currFrame;
        }

        if (oldFrame == null) {
            oldFrame = currFrame;
        }
        if (oldFrame != null) {
            Core.absdiff(oldFrame, currFrame, foregroundImage);
        }

        if (foregroundImage != null) {
            Core.absdiff(currFrame, foregroundImage, backgroundImage);
        }
//        if (foregroundImage != null) {
        Imgproc.cvtColor(foregroundImage, greyImage, Imgproc.COLOR_BGR2GRAY);

        int thresh_type = Imgproc.THRESH_BINARY_INV;
//        if (this.inverse.isSelected()) {
        thresh_type = Imgproc.THRESH_BINARY;
//        }

// show the current frame and the fg masks
//        HighGui.imshow("Frame", currFrame);
//        HighGui.imshow("FG Mask", foregroundImage);
        Imgproc.adaptiveThreshold(greyImage, greyImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);
//            Imgproc.threshold(greyImage, greyImage, 10, 255, thresh_type);
        currFrame.copyTo(foregroundImage, greyImage);

        Imgcodecs.imwrite("D:\\currFrame.jpg", currFrame);
        Imgcodecs.imwrite("D:\\foregroundImage.jpg", foregroundImage);
        Imgcodecs.imwrite("D:\\backgroundImage.jpg", backgroundImage);

        oldFrame = currFrame;
//        }
        return foregroundImage;

    }

    private void backgroundSubtraction(Mat frame) {

        Mat fgMask = new Mat();
        // update the background model
        backSub.apply(frame, fgMask);
        // get the frame number and write it on the current frame
//        Imgproc.rectangle(frame, new Point(10, 2), new Point(100, 20), new Scalar(255, 255, 255), -1);
//        String frameNumberString = String.format("%d", (int) capture.get(Videoio.CAP_PROP_POS_FRAMES));
//        Imgproc.putText(frame, frameNumberString, new Point(15, 15), Core.FONT_HERSHEY_SIMPLEX, 0.5,
//                new Scalar(0, 0, 0));
        // show the current frame and the fg masks
        HighGui.imshow("Frame", frame);
        HighGui.imshow("FG Mask", fgMask);
        // get the input from the keyboard
        int keyboard = HighGui.waitKey(30);
        if (keyboard == 'q' || keyboard == 27) {
            System.exit(0);
        }

    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
//                        this.capture.
            this.cameraActive = false;
            System.exit(0);
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

    public void setCroppedImagePoint(int x, int y, int width, int height) {

        this.cropX = x;
        this.cropY = y;
        this.cropWidth = width;
        this.cropHeight = height;

    }

    private void calculateUniformity(Mat greyImage, int x, int y, int width, int height, Rect r) {
        int[] hist = new int[256];
        for (int i = 0; i < 256; i++) {
            hist[i] = 0;
        }
        boolean isTrue = true;
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {

                double[] val = greyImage.get(i, j);
                if (val != null) {
                    if ((i < r.x || i > r.x + r.width) && (j < r.y || j > r.y + r.height)) {

                        if (isTrue) {
                            System.err.println(" size > " + val.length);
                            isTrue = false;
                        }
//                        if (0 != val[0] && 0 != val[1] && 0 != val[2]) {
//                            System.err.println(val[0] + " ," + val[1] + " ," + val[2]);
                        hist[(int) val[0]]++;
//                        }
                    }
                }
            }
        }
        int[] bin_hist = new int[256 / binSize];
        int s = 0;
        int sumBin = 0;
        System.err.println("************* histogram  >>>>>>>>>>>");
        for (int i = 0; i < 256;) {

            for (int k = 0; k < binSize; k++) {
                sumBin += hist[i++];
            }
            bin_hist[s++] = sumBin;
            System.err.print(sumBin + "   ");
//            sumBin = 0;
        }
        System.err.println("///////////////////////////////");
        // draw the histograms
        int hist_w = 512;
        int hist_h = 400;
        int bin_w = (int) ((double) hist_w / 256);
        
        int bin_16 = (int) ((double) hist_w / 16);

        Mat histImage = new Mat(hist_h, hist_w, CV_8UC1, new Scalar(255, 255, 255));
        
        Mat binhistImage = new Mat(hist_h, hist_w, CV_8UC1, new Scalar(255, 255, 255));

        // find the maximum intensity element from histogram
        int max = hist[0];
        for (int i = 1; i < 256; i++) {
            if (max < hist[i]) {
                max = hist[i];
            }
        }
        
        // normalize the histogram between 0 and histImage.rows
        for (int i = 0; i < 256; i++) {
            hist[i] = (int) (((double) hist[i] / max) * histImage.rows());
        }
        
        // find the maximum intensity element from histogram
        max = bin_hist[0];
        for (int i = 1; i < binSize; i++) {
            if (max < bin_hist[i]) {
                max = bin_hist[i];
            }
        }
        //calculate uniform background
        
        uniformity_score = bin_hist[binSize -1] - bin_hist[0];
        
        // normalize the histogram between 0 and histImage.rows
        for (int i = 0; i < binSize; i++) {
            bin_hist[i] = (int) (((double) bin_hist[i] / max) * binhistImage.rows());
        }

        // draw the intensity line for histogram
        for (int i = 0; i < 256; i++) {
            Imgproc.line(histImage, new Point(bin_w * (i), hist_h),
                    new Point(bin_w * (i), hist_h - hist[i]),
                    new Scalar(0, 0, 0), 1, 8, 0);
        }
        
        // draw the intensity line for histogram
        for (int i = 0; i < binSize; i++) {
            Imgproc.line(binhistImage, new Point(bin_16 * (i), hist_h),
                    new Point(bin_16* (i), hist_h - bin_hist[i]),
                    new Scalar(0, 0, 0), 32, 8, 0);
        }

        // display histogram
        Imgcodecs.imwrite("D:/histIMage.jpg", histImage);
        
        // display histogram
        Imgcodecs.imwrite("D:/binhistIMage.jpg", binhistImage);
//        HighGui.imshow("Histogram", histImage);

    }

    public BufferedImage cropingIMage(Mat image) throws IOException {
        BufferedImage out = null;
        MatOfInt rejectLevels = new MatOfInt();
        MatOfDouble levelWeights = new MatOfDouble();
        float scale_factor = 1.1f;
        int min_neighbors = 3;
        //convert the frame in 

        //TODO:Masum
        int totalHeight = (int) Math.ceil(absoluteFaceSize / 0.65);
        int totalWidth = (int) Math.ceil(absoluteFaceSize / 0.65);
        double upperFacePart = 0.40;
        double lowerFacePart = 0.35;
        double leftFacePart = 0.20;
        double rightFacePart = 0.20;
        Rect rectCrop = null;

        boolean isBorder = false;

        if (image != null) {
            this.frameData = new FrameData();
            MatOfRect faces = new MatOfRect();
            MatOfRect facesOnly = new MatOfRect();
            Mat grayFrame = new Mat();
            // convert the frame in gray scale
            Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
            // equalize the frame histogram to improve the result
            Imgproc.equalizeHist(grayFrame, grayFrame);

            //    filtering 
            Imgproc.GaussianBlur(image, grayFrame, new org.opencv.core.Size(0, 0), 10);
            Core.addWeighted(image, 1.5, grayFrame, -0.5, 0, grayFrame);

            // compute minimum face size (20% of the frame height, in our case)
//            if (this.absoluteFaceSize == 0) {
//                int height = grayFrame.rows();
//                if (Math.round(height * 0.2f) > 0) {
//                    this.absoluteFaceSize = Math.round(height * 0.2f);
//                }
//            }
            //TODO:Masum
            // compute minimum face size (30% of the frame height, in our case)
            int height = grayFrame.rows();
            if (Math.round(height * 0.3f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.3f);
            }

            this.faceCascade.detectMultiScale3(grayFrame, faces, rejectLevels, levelWeights, scale_factor, min_neighbors, 0, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size(), cameraActive);
            this.faceCascade.detectMultiScale3(grayFrame, facesOnly, rejectLevels, levelWeights, scale_factor, min_neighbors, 0, new Size(Math.round(height * 0.3f) > 0 ? Math.round(height * 0.3f) : 0, Math.round(height * 0.3f) > 0 ? Math.round(height * 0.3f) : 0), new Size(), cameraActive);
            // System.out.println(String.format("Detected %s faces", faces.toArray().length));
            // each rectangle in faces is a face: draw them!
//        Rect rect_Crop = null;
            int[] scoreFace = new int[5];
            double[] weight = new double[5];
            if (!rejectLevels.empty()) {
                scoreFace = rejectLevels.toArray();
            }
            if (!levelWeights.empty()) {
                weight = levelWeights.toArray();
            }
            Rect[] facesArray = faces.toArray();
            Rect[] facesCrop = facesOnly.toArray();
            for (int i = 0; i < facesArray.length; i++) {

                if ((facesArray[i].x - (int) Math.ceil(leftFacePart * totalWidth)) >= 0 && (facesArray[i].y - (int) Math.ceil(upperFacePart * totalHeight)) >= 0 && (facesArray[i].width + 2 * ((int) Math.ceil(rightFacePart * totalWidth))) <= (image.width() - facesArray[i].x + (int) Math.ceil(rightFacePart * totalWidth)) && (facesArray[i].height + (int) Math.ceil(lowerFacePart * totalHeight) + (int) Math.ceil(lowerFacePart * totalHeight)) <= (image.height() - facesArray[i].y + (int) Math.ceil(lowerFacePart * totalHeight))) {
                    this.cropX = facesArray[i].x - (int) Math.ceil(leftFacePart * totalWidth);
                    this.cropY = facesArray[i].y - (int) Math.ceil(upperFacePart * totalHeight);
                    this.cropWidth = facesArray[i].width + 2 * ((int) Math.ceil(rightFacePart * totalWidth));
                    this.cropHeight = facesArray[i].height + (int) Math.ceil(lowerFacePart * totalHeight) + (int) Math.ceil(lowerFacePart * totalHeight);

                } else {
                    setCroppedImagePoint(facesArray[i].x, facesArray[i].y, facesArray[i].width, facesArray[i].height);
                    isBorder = true;
                }
                rectCrop = new Rect(this.cropX, this.cropY, this.cropWidth, this.cropHeight);

                Point showScoreP = new Point(20, 20);
                face_score = (float) weight[i];
                String score = "Captured Face  Score : " + face_score;

                Imgproc.putText(grayFrame, score, showScoreP, 1, 1, new Scalar(0, 255, 0), 2);
//            System.err.println("rejectLevels Score > " + scoreFace[i] + " levelWeights > " + weight[i]);
                Imgproc.rectangle(grayFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

                Mat faceROI = grayFrame.submat(facesCrop[i]);

                if (!isCrop) {
                    calculateUniformity(grayFrame, this.cropX, this.cropY, this.cropWidth, this.cropHeight, facesCrop[i]);
                }

                Imgcodecs.imwrite("D:\\faceROI.jpg", faceROI);
                int scoreIndex = 0;

                // -- In each face, detect eyes
                MatOfRect eyes = new MatOfRect();
//            this.eyesCascade.detectMultiScale(faceROI, eyes, 1.1, 3, 0);
                this.eyesCascade.detectMultiScale3(faceROI, eyes, rejectLevels, levelWeights, 1.1, 3, 0, new Size(5, 5), new Size(), cameraActive);
                List<Rect> listOfEyes = eyes.toList();
                for (Rect eye : listOfEyes) {
                    int leftEyeX = 0;
                    int leftEyeY = 0;
                    if (scoreIndex == 0) {
                        leftEyeX = eye.x;
                        leftEyeY = eye.y;
                    }
                    if (scoreIndex >= 1) {
                        if (eye.x - leftEyeX != 0) {
//                        System.err.println("Face rotation : " + Math.tan((leftEyeY - eye.y) / (leftEyeX - eye.x)));
                        }
                    }
                    eye_score = rejectLevels.toArray()[scoreIndex++];
                    String eyeScore = "Eye SCore : " + eye_score;

                }

                // Detect nose if classifier provided by the user circle(ROI, Point(n.x + n.width / 2, n.y + n.height / 2), 3, Scalar(0, 255, 0), -1, 8);
                MatOfRect nose = new MatOfRect();
                scoreIndex = 0;
//            this.noseCascade.detectMultiScale(faceROI, nose, 1.1, 3, 0);
                this.noseCascade.detectMultiScale3(faceROI, nose, rejectLevels, levelWeights, 1.1, 3, 0, new Size(5, 5), new Size(), cameraActive);
                List<Rect> listOfNose = nose.toList();
                double nose_center_height = 0.0;
                for (Rect n : listOfNose) {
                    nose_score = rejectLevels.toArray()[scoreIndex++];
                    String noseScore = "Nose SCore : " + nose_score;

                }

                // Detect mouth if classifier provided by the user
                MatOfRect mouth = new MatOfRect();
//            this.mouthCascade.detectMultiScale(faceROI, mouth,1.1, 3, 0);
                this.mouthCascade.detectMultiScale3(faceROI, mouth, rejectLevels, levelWeights, 1.1, 3, 0, new Size(), new Size(), cameraActive);
                double mouth_center_height = 0.0;
                Rect[] listOfMouth = mouth.toArray();
                if (listOfMouth.length >= 1) {
                    for (i = 0; i < 1; i++) {
                        if (mouth_center_height > nose_center_height) {
                            String mouthScore = "Mouth SCore : " + rejectLevels.toArray()[0];
                            mouth_score = rejectLevels.toArray()[0];
                        } else if (mouth_center_height <= nose_center_height) {
                            continue;
                        } else {
                            String mouthScore = "Mouth SCore : " + rejectLevels.toArray()[0];
                            mouth_score = rejectLevels.toArray()[0];
                        }
                    }
                }

            }

            // Saving the output image
            String filename = "Ouput4.jpg";
            Mat dst_lap = new Mat();

            Imgcodecs.imwrite("D:\\" + filename, image);
            Mat markedImage = null;
            try {
                if (rectCrop != null) {
                    markedImage = new Mat(image, rectCrop);
                    int depth = 0;
                    Imgproc.Laplacian(grayFrame, dst_lap, CV_16S, 3, 1, 0, Core.BORDER_DEFAULT);
                    System.err.println("+++++++++ Blurred image >>>>>>>>>> " + calcBlurriness(markedImage));
                    Mat resizeimage = new Mat();
//            Size sz = new Size(240, 320);
                    Imgproc.resize(markedImage, resizeimage, new Size(260, 320));
                    Mat dst = markedImage;
//                Core.copyMakeBorder()
                    if (isBorder) {
                        Core.copyMakeBorder(markedImage, dst, (int) Math.round(0.1 * image.rows()), (int) Math.round(0.1 * image.rows()), (int) Math.round(0.1 * image.cols()), (int) Math.round(0.1 * image.cols()), 2, new Scalar(255, 255, 255));
                        Imgcodecs.imwrite("D:\\cropimage_border.jpg", dst);
                    } else {
                        Imgcodecs.imwrite("D:\\cropimage_111.jpg", markedImage);
                    }
                    Imgcodecs.imwrite("D:\\cropimage_resize.jpg", resizeimage);
                    out = getBufferedImageFromMat(markedImage);
                    this.frameData.setCroppedImage(out);

                    List<FaceAttribute> attrList = new ArrayList();
                    Config config = new Config();

                    List<FaceAttribute> icaoAttributes = config.icaoAttributes;
                    for (FaceAttribute curAttribute : icaoAttributes) {
                        FaceAttribute nowAttributeClone = (FaceAttribute) curAttribute.clone();
                        switch (curAttribute.getId()) {
                            case MOUTH_STATUS:
                                nowAttributeClone.setScore(mouth_score);
                                break;
                            case FACE_CONFIDENCE:
                                nowAttributeClone.setScore(face_score);
                                break;
                            case EYE_STATUS_L:
                                nowAttributeClone.setScore(eye_score);
                                break;
                            case EYE_STATUS_R:
                                nowAttributeClone.setScore(eye_score);
                                break;
                            case NOSE:
                                nowAttributeClone.setScore(nose_score);
                                break;
                            case BACKGROUND_UNIFORMITY:
                                nowAttributeClone.setScore(uniformity_score);
                                break;
                            case CONTRAST:
                                //calculate contrast
                                double contrast_score = getContrast(image, rectCrop);
                                System.err.println("+++++++++ contrast >>>>>>>>>> " + contrast_score);
                                nowAttributeClone.setScore((float) contrast_score);
                                break;
                            case BLURNESS:
                                //blurness 
                                double blur_score = blurNess(image);
                                System.err.println("blurrness before Gaussian : " + blur_score);
                                Mat blurred = new Mat();
                                for (int i = 3; i < 80; i += 2) {

                                    Imgproc.GaussianBlur(image, blurred, new Size(i, i), 50);

                                }
                                blur_score = getContrast(blurred, rectCrop) / 1e3;
                                System.err.println("blurred score after Gaussian  : " + blur_score);
                                nowAttributeClone.setScore((float) blur_score);
                                break;
                            case SHARPNESS:
                                //calculate sharpness
                                double sharp_score = getSharpNess(image, rectCrop);
                                System.err.println("+++++++++ SharpNess >>>>>>>>>> " + sharp_score);
                                nowAttributeClone.setScore((float) sharp_score);
                                break;
                            case BRIGHTNESS:
                                BufferedImage tmp = getBufferedImageFromMat(image);
                                double bright_score = 0;
                                //Calculate brightness of image
                                Mat bright_mat = new Mat();
                                Imgproc.cvtColor(image, bright_mat, Imgproc.COLOR_BGR2HSV);
                                int sumR = 0,
                                 sumG = 0,
                                 sumB = 0;
                                Color c = null;
                                int pixelNo = 0;
                                double value = 0;
                                for (int i = rectCrop.x; i < rectCrop.x + rectCrop.width; i++) {
                                    for (int j = rectCrop.y; j < rectCrop.y + rectCrop.height; j++) {
                                        c = new Color(tmp.getRGB(i, j));
                                        sumR += c.getRed() >> 16 & 0xFF;
                                        sumG += c.getGreen() >> 8 & 0xFF;
                                        sumB += c.getBlue() & 0xFF;

                                        pixelNo++;
                                        if (bright_mat.get(i, j) != null) {
                                            value += bright_mat.get(i, j)[2];
                                        }

                                    }

                                }
//                    System.err.println("+++++++++ Red >>>>>>>>>> " + c.getRed() + " Green > " + c.getGreen() + " Blue > " + c.getBlue());
                                float totalColor = sumR + sumG + sumB;
//                    System.err.println("+++++++++ average Red >>>>>>>>>> " + sumR / totalColor + " Green > " + sumG / totalColor + " Blue > " + sumB / totalColor);
//                    System.err.println(" brightness > " + (sumR / totalColor + sumG / totalColor + sumB / totalColor) / 3);
                                bright_score = value / (pixelNo * 255);
                                System.err.println("brightness from HSV model >>>>>>> " + bright_score);
                                nowAttributeClone.setScore((float) bright_score);
                                break;
//                    System.err.println(" #Standard > " + (0.2126 * sumR / totalColor + 0.7152 * sumG / totalColor + 0.0722 * sumB / totalColor));
                            default:
                                break;

                        }

                        attrList.add(nowAttributeClone);

                    }
                    this.frameData.setIcaoAttributes(attrList);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return out;
        }
        return out;
    }

    private int blurNess(Mat src) {

        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(src, matImageGrey, Imgproc.COLOR_BGR2GRAY);
        int l = CvType.CV_8UC1; //8-bit grey scale image

        Mat dst2 = new Mat();
        dst2 = src;
        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
        Mat laplacianImage8bit = new Mat();
        laplacianImage.convertTo(laplacianImage8bit, l);

        int[] pixels = new int[laplacianImage8bit.cols() * laplacianImage8bit.rows()];
        int p = 0;
        for (int r = 0; r < laplacianImage8bit.rows(); r++) {
            for (int c = 0; c < laplacianImage8bit.cols(); c++) {
                pixels[p++] = (int) laplacianImage8bit.get(r, c)[0];
            }
        }
        int maxLap = -16777216;

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] > maxLap) {
                maxLap = pixels[i];
            }
        }

        int soglia = -6118750;

        if (maxLap < soglia || maxLap == soglia) {
            System.err.println("****** blur image +++++++++");;
        }

        return maxLap;
    }

    float getSharpNess(Mat image, Rect rectCrop) throws IOException {
        Mat laplacian = new Mat();
        Mat average = new Mat();
        Mat grayScale = new Mat();

//        grayScale.
//        Imgproc.Laplacian(image, laplacian, width, 3);
        Imgproc.cvtColor(image, grayScale, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("D://grayScale.jpg", grayScale);
        Imgproc.GaussianBlur(image, grayScale, new org.opencv.core.Size(0, 0), 10);
//        Core.addWeighted(image, 1.5, grayScale, -0.5, 0, grayScale);

        Imgproc.Laplacian(grayScale, laplacian, CV_16S, 3, 1, 0, Core.BORDER_DEFAULT);
        Imgproc.boxFilter(grayScale, average, CV_16S, new Size(3, 3));
//        Core.addWeighted(image, 1.5, average, -0.5, 0, average);

        double sum = 0;
        int lowPixel = 0;
        int highPixel = 0;
        for (int i = rectCrop.x; i < rectCrop.x + rectCrop.width; i++) {
            for (int j = rectCrop.y; j < rectCrop.y + rectCrop.height; j++) {

                double[] pixelValue = grayScale.get(i, j);
//                System.err.println("+++++++++ pixelValue[0] in getSharpNess >>>>>>>>>> " + pixelValue[0]);
                if (pixelValue != null) {
                    if ((int) pixelValue[0] < lowPixel) {
                        lowPixel = (int) pixelValue[0];
                    }
                    if ((int) pixelValue[0] > highPixel) {
                        highPixel = (int) pixelValue[0];
                    }
                }

//                if (pixelValue[0] != 0 && pixelValue[0] == 0) {
//                    continue;
//                }
//
//                if (pixelValue[0] != 0) {
//                    sum += Math.abs(laplacian.get(i, j)[0] / average.get(i, j)[0]);
//                }
            }

        }
        int maxdiff = highPixel - lowPixel;
        int offset = 15;
        int range = (highPixel + lowPixel) / 2;
        // sharpness = (maxdiff / (offset + range)) * (1.0 + offset / 255) * 100%
        float sharpness = ((float) maxdiff / (offset + range)) * (float) (1.0 + offset / 255) * 100;

        return sharpness;
//        return (float) sum / (rectCrop.height * rectCrop.width);
    }

    double getContrast(Mat image, Rect rectCrop) throws IOException {
//        float maxLum = 0;
//        float minLum = 10000;
//        BufferedImage tmp = getBufferedImageFromMat(image);
//        float brightness = 0;
//        for (int i = rectCrop.x; i < rectCrop.x + rectCrop.width; i++) {
//            for (int j = rectCrop.y; j < rectCrop.y + rectCrop.height; j++) {
//
//                float r = tmp.getRGB(i, j) >> 16 & 0xFF;
//                float g = tmp.getRGB(i, j) >> 8 & 0xFF;
//                float b = tmp.getRGB(i, j) & 0xFF;
//                brightness = (float) ((0.2126 * r) + (0.7152 * g) + (0.0722 * b));
//                if (brightness > maxLum) {
//                    maxLum = brightness;
//                }
//                if (brightness < minLum) {
//                    minLum = brightness;
//                }
//
//            }
//
//        }

        Mat src = new Mat(image, rectCrop);
        Mat Gx = new Mat(), Gy = new Mat();
        Imgproc.Sobel(src, Gx, CV_32F, 1, 0, 3);
        Imgproc.Sobel(src, Gy, CV_32F, 0, 1, 3);

//        Core.mean(src);
        Core.magnitude(Gx, Gy, Gx);
        Scalar scl = Core.sumElems(Gx);
//        System.err.println(" contrast field 0 >  " + scl.val[0] + " 1 > " + scl.val[1] + " 2 > " + scl.val[2]);

        return scl.val[0] / 1e3;
//        return sum(Gx)[0];
//        System.err.println("+++++++++ Brightness in getContrast >>>>>>>>>> " + brightness);

//        return (maxLum - minLum) / (maxLum + minLum);
    }

    float calcBlurriness(Mat src) {
        Mat Gx = new Mat(), Gy = new Mat();
        Imgproc.Sobel(src, Gx, CV_32F, 1, 0, 3);
        Imgproc.Sobel(src, Gy, CV_32F, 0, 1, 3);

        Core.magnitude(Gx, Gy, Gy);

        double normGx = Core.norm(Gx);
        double normGy = Core.norm(Gy);
        double sumSq = normGx * normGx + normGy * normGy;
        return (float) (1.0 / (sumSq / src.size().area() + 1e-6));
//        return 0;
    }

    public static BufferedImage getBufferedImageFromMat(Mat mat) throws IOException {
        MatOfByte mbyt = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mbyt);
        byte ba[] = mbyt.toArray();
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

}
