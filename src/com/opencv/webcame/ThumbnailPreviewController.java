package com.opencv.webcame;



import com.opencv.face.FaceAttribute;
import com.opencv.face.FrameData;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ThumbnailPreviewController implements Initializable{
    @FXML
    private ImageView capturedImage;
    
    @FXML
    private TableView icaoTableView;
    
    @FXML
    private Button okBtn;
    
    private Stage stage;
    
    private FrameData frameData;

    public FrameData getFrameData() {
        return frameData;
    }

    public void setFrameData(FrameData frameData) {
        this.frameData = frameData;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    
    
    public void decorateICAOTable(){
        if(this.frameData!=null && this.frameData.getIcaoAttributes()!=null){
            List<FaceAttribute> attrList = this.frameData.getIcaoAttributes();
            if(this.icaoTableView!=null){
                ObservableList<TableColumn> tcList = icaoTableView.getColumns();
                for(TableColumn tc:tcList){
                    switch(tc.getId()){
                        case "idProperty":
                            tc.setCellFactory(new Callback<TableColumn, TableCell>() {
                                @Override
                                public TableCell call(TableColumn param) {
                                    return new TableCell<FaceAttribute, String>() 
                                    {
                                        @Override
                                        public void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            if(isEmpty()){
                                                setText("");
                                            }
                                            else{

                                                setTextFill(Color.BLUE);
                                                setFont(Font.font ("Verdana",FontWeight.BOLD,20));
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });
                            
                            tc.setCellValueFactory(new PropertyValueFactory<FaceAttribute,String>("name"));
                        break;
//                        case "idStatus":
//                            Callback<TableColumn,TableCell> cellFactory = new Callback<TableColumn,TableCell>(){
//                                @Override
//                                public TableCell call(TableColumn param) {
//                                    TableCell cell = new TableCell() {
//                                                @Override
//                                                public void updateItem(Object item, boolean empty) {                                    
//                                                        super.updateItem(item, empty);
//                                                        final TableCell thisCell = this;                                        
//                                                        if(!empty && (item instanceof String)){
//                                                            ImageView statView = null;
//                                                            if( ((String)item).equalsIgnoreCase("OK") ){
//                                                                statView = new ImageView("/com/istlbd/face/resource/ok.png");
//                                                            }else{
//                                                                statView = new ImageView("/com/istlbd/face/resource/not_ok.png");
//                                                            }      
//                                                            statView.setFitHeight(20);
//                                                            statView.setFitWidth(20);
//                                                            
//                                                            this.setText(null);                                                            
//                                                            this.setGraphic(statView);
//                                                        }
//                                                }
//                                        };
//
//                                        return cell;
//                                }
//
//                            };
//                            tc.setCellFactory(cellFactory);
//                            tc.setCellValueFactory(new PropertyValueFactory<FaceAttribute,String>("status"));
//                        break;    
                        case "idScore":
                            tc.setCellValueFactory(new PropertyValueFactory<FaceAttribute,Integer>("score"));
                        break;
                    }
                }
                      
                this.icaoTableView.setItems(FXCollections.observableList(this.frameData.getIcaoAttributes()));
            }
        }
         
    }
    
    public void drawImage() {
        
        if(this.frameData!=null && this.frameData.getCroppedImage()!=null){
            
            if(this.capturedImage!=null)
            {
                this.capturedImage.setPreserveRatio(true);                
                this.capturedImage.setImage(SwingFXUtils.toFXImage(this.frameData.getCroppedImage(),null));
            }
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okBtn.setOnAction(this::handleCloseAction);
    }

    
    public void handleCloseAction(ActionEvent event) {
        if(stage !=null){
            stage.close();
        }
    }
    
    
    
}
