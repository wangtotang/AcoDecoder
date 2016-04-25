package com.tong;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {

    @FXML
    Button btn_select_file;

    @FXML
    ProgressIndicator pi_decode_file;

    @FXML
    TextField tf_prefix_name;

    @FXML
    TextField tf_save_name;

    final FileChooser chooser = new FileChooser();
    String path = null;

    @FXML
    void selectFile(ActionEvent event){
        configureFileChooser(chooser);
        File file = chooser.showOpenDialog(Main.window);
        if (file != null) {
            btn_select_file.setVisible(false);
            pi_decode_file.setVisible(true);
            path = file.getParent();
            Decoder.readFile(path,file.getName());
            String prefix = tf_prefix_name.getText();
            Decoder.decode(prefix);
            Timer timer = new Timer();
            timer.schedule(new MyTask(0.6),600);
            String name = tf_save_name.getText();
            Decoder.write(name);
            timer.schedule(new MyTask(1.0),1200);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    btn_select_file.setVisible(true);
                    pi_decode_file.setVisible(false);
                    timer.cancel();
                }
            }, 1800);
        }

    }

    private class MyTask extends TimerTask{

        double progress;

        MyTask(double progress){
            this.progress = progress;
        }

        @Override
        public void run() {
            pi_decode_file.setProgress(progress);
        }

    }

    private void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("选择aco文件");
        if(path!=null){
            fileChooser.setInitialDirectory(new File(path));
        }else{
            fileChooser.setInitialDirectory(new File("./"));
        }
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("aco file", "*.aco"));
    }

}
