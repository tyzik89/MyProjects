package models.image;

import constants.NotifyConstants;
import javafx.scene.image.Image;
import models.algorithms.*;
import models.notification.Observable;
import models.notification.Observer;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import utils.ImageUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Получает запрос на образобку от контроллеров
 * вызывает нужный алгоритм
 * получает результат работы
 * отправляет уведомление контроллерам о готовности
 *
 * Работает с хранилищем изображений {@link StorageImages}
 */
public class ImagesHandler implements Observable {

    private StorageImages storageImages;
    private List<Observer> observers;

    public ImagesHandler() {
        this.storageImages = StorageImages.getInstance();
        //Список наблюдателей о результатах работы
        this.observers = new LinkedList<>();
    }

    public void load() {
        //fixme исправить загрузку изображения
        // An image file on the hard drive.
        /*File file = new File("C:/MyImages/myphoto.jpg");
        // --> file:/C:/MyImages/myphoto.jpg
        String localUrl = file.toURI().toURL().toString();
        Image image = new Image(localUrl);*/
        Mat mat = Imgcodecs.imread("src/main/resources/img/image1.png", Imgcodecs.IMREAD_UNCHANGED);
        Image image = ImageUtils.matToImageFX(mat);
        storageImages.init(image);

        notifyObservers(NotifyConstants.IMAGE_LOADED);
    }

    /**
     * Отмена алгоритма, т.е. возврат предыдущего состояния объекта Image
     * Возврат алгоритма, т.е. возврат обработанного состояния объекта Image
     */
    public void doCancelRedo() {
        Image temp = storageImages.getCurrentImage();
        storageImages.setCurrentImage(storageImages.getPreviousImage());
        storageImages.setPreviousImage(temp);
        notifyObservers(NotifyConstants.IMAGE_READY);
    }

    public void doMakeBinary(int threshold, boolean isOtsu) {
        doMakeAlgorithm(new BinaryImageAlgorithm(threshold, isOtsu));
    }

    public void doCannyEdgeDetection(int sizeGaussFilter, int threshold, int sizeSobelKernel, boolean isUseL2Gradient) {
        doMakeAlgorithm(new CannyEdgeDetectorAlgorithm(sizeGaussFilter, threshold, sizeSobelKernel, isUseL2Gradient));
    }

    public void doHoughConversion(boolean typeHoughMethodClassic, double distance,  double angle, int threshold, double ... params) {
        doMakeAlgorithm(new HoughConversionAlgorithm(typeHoughMethodClassic, distance, angle, threshold, params));
    }

    public void doWatershedSegmentation() {
        doMakeAlgorithm(new WatershedSegmentation());
    }

    public void doMakeBlur(int sizeGaussFilter) {
        doMakeAlgorithm(new GaussBlurAlgorithm(sizeGaussFilter));
    }

    /**
     * Общий метод запуска алгоритмов
     * @param algorithm нужныйы алгоритм
     */
    private void doMakeAlgorithm(Algorithm algorithm){
        new Thread(() -> {
            Mat mat = ImageUtils.imageFXToMat(storageImages.getCurrentImage());
            Mat result = algorithm.doAlgorithm(mat);
            switchImagesOnNextStep(ImageUtils.matToImageFX(result));
        }).start();

    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.notification(message);
        }
    }

    public Image getCurrentImage() {
        return storageImages.getCurrentImage();
    }

    public Image getSourceImage() {
        return storageImages.getSourceImage();
    }

    private synchronized void switchImagesOnNextStep(Image newImage) {
        storageImages.switchImagesOnNextStep(newImage);
        notifyObservers(NotifyConstants.IMAGE_READY);
    }
}
