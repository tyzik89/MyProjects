package ru.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileManager {

    public static void main(String[] args) {
        //Интерфейс отрисовывается независимо от главного потока
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new UI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
