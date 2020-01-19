
package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.vision.VisionThread;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;

import java.util.*;
    public class VisionController{
    Thread m_visionThread;
   public static  CvSink cvSink; // publicstatic so that it can be accessed by all the classes

   public double[] findCenter(Mat img) {
    //[x,y]
    double[] centerCoor = {-1, -1}; // set in case of exception

    GripPipeline pipeline = new GripPipeline();
    pipeline.process(img);

    if(pipeline.filterContoursOutput().size() == 1){
        Moments moments = Imgproc.moments(pipeline.filterContoursOutput().get(0));
        centerCoor[0] = moments.get_m10() / moments.get_m00();
        centerCoor[1] = moments.get_m01() / moments.get_m00();
        return centerCoor;
    }
    return centerCoor;
}


    public VisionController() {
        m_visionThread = new Thread(() -> {
        // Get the UsbCamera from CameraServer
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        // Set the resolution
        camera.setResolution(640, 480);

        // Get a CvSink. This will capture Mats from the camera
        cvSink = CameraServer.getInstance().getVideo();
        // Setup a CvSource. This will send images back to the Dashboard
        CvSource outputStream
            = CameraServer.getInstance().putVideo("Rectangle", 640, 480);

        // Mats are very memory expensive. Lets reuse this Mat.
        Mat img = new Mat();        
        // This cannot be 'true'. The program will never exit if it is. This
        // lets the robot stop this thread when restarting robot code or
        // deploying.
        while (!Thread.interrupted()) {
            // Tell the CvSink to grab a frame from the camera and put it
            // in the source mat.  If there is an error notify the output.
            if (cvSink.grabFrame(input) == 0) {
            // Send the output the error.
            outputStream.notifyError(cvSink.getError());
            // skip the rest of the current iteration
            continue;
            }
            // Put a rectangle on the image
            Imgproc.rectangle(input, new Point(100, 100), new Point(400, 400),
                new Scalar(255, 255, 255), 5);
            // Give the output stream a new image to display
            outputStream.putFrame(input);
        }
        });
        m_visionThread.setDaemon(true);
        m_visionThread.start();
    }
}