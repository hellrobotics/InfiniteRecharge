/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ],
               "stream": {                              // optional
                   "properties": [
                       {
                           "name": <stream property name>
                           "value": <stream property value>
                       }
                   ]
               }
           }
       ]
       "switched cameras": [
           {
               "name": <virtual camera name>
               "key": <network table key used for selection>
               // if NT value is a string, it's treated as a name
               // if NT value is a double, it's treated as an integer index
           }
       ]
   }
 */

public final class Main {
  private static String configFile = "/boot/frc.json";

  private static double centerX = 0.0;
  private static double centerY = 0.0;
  private static NetworkTableEntry centerXEntry;
  private static NetworkTableEntry centerYEntry;

  @SuppressWarnings("MemberName")
  public static class CameraConfig {
    public String name;
    public String path;
    public JsonObject config;
    public JsonElement streamConfig;
  }

  @SuppressWarnings("MemberName")
  public static class SwitchedCameraConfig {
    public String name;
    public String key;
  };

  public static int team;
  public static boolean server;
  public static List<CameraConfig> cameraConfigs = new ArrayList<>();
  public static List<SwitchedCameraConfig> switchedCameraConfigs = new ArrayList<>();
  public static List<VideoSource> cameras = new ArrayList<>();

  private Main() {
  }

  /**
   * Report parse error.
   */
  public static void parseError(String str) {
    System.err.println("config error in '" + configFile + "': " + str);
  }

  /**
   * Read single camera configuration.
   */
  public static boolean readCameraConfig(JsonObject config) {
    CameraConfig cam = new CameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read camera name");
      return false;
    }
    cam.name = nameElement.getAsString();

    // path
    JsonElement pathElement = config.get("path");
    if (pathElement == null) {
      parseError("camera '" + cam.name + "': could not read path");
      return false;
    }
    cam.path = pathElement.getAsString();

    // stream properties
    cam.streamConfig = config.get("stream");

    cam.config = config;

    cameraConfigs.add(cam);
    return true;
  }

  /**
   * Read single switched camera configuration.
   */
  public static boolean readSwitchedCameraConfig(JsonObject config) {
    SwitchedCameraConfig cam = new SwitchedCameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read switched camera name");
      return false;
    }
    cam.name = nameElement.getAsString();

    // path
    JsonElement keyElement = config.get("key");
    if (keyElement == null) {
      parseError("switched camera '" + cam.name + "': could not read key");
      return false;
    }
    cam.key = keyElement.getAsString();

    switchedCameraConfigs.add(cam);
    return true;
  }

  /**
   * Read configuration file.
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static boolean readConfig() {
    // parse file
    JsonElement top;
    try {
      top = new JsonParser().parse(Files.newBufferedReader(Paths.get(configFile)));
    } catch (IOException ex) {
      System.err.println("could not open '" + configFile + "': " + ex);
      return false;
    }

    // top level must be an object
    if (!top.isJsonObject()) {
      parseError("must be JSON object");
      return false;
    }
    JsonObject obj = top.getAsJsonObject();

    // team number
    JsonElement teamElement = obj.get("team");
    if (teamElement == null) {
      parseError("could not read team number");
      return false;
    }
    team = teamElement.getAsInt();

    // ntmode (optional)
    if (obj.has("ntmode")) {
      String str = obj.get("ntmode").getAsString();
      if ("client".equalsIgnoreCase(str)) {
        server = false;
      } else if ("server".equalsIgnoreCase(str)) {
        server = true;
      } else {
        parseError("could not understand ntmode value '" + str + "'");
      }
    }

    // cameras
    JsonElement camerasElement = obj.get("cameras");
    if (camerasElement == null) {
      parseError("could not read cameras");
      return false;
    }
    JsonArray cameras = camerasElement.getAsJsonArray();
    for (JsonElement camera : cameras) {
      if (!readCameraConfig(camera.getAsJsonObject())) {
        return false;
      }
    }

    if (obj.has("switched cameras")) {
      JsonArray switchedCameras = obj.get("switched cameras").getAsJsonArray();
      for (JsonElement camera : switchedCameras) {
        if (!readSwitchedCameraConfig(camera.getAsJsonObject())) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Start running the camera.
   */
  public static VideoSource startCamera(CameraConfig config) {
    System.out.println("Starting camera '" + config.name + "' on " + config.path);
    CameraServer inst = CameraServer.getInstance();
    UsbCamera camera = new UsbCamera(config.name, config.path);
    MjpegServer server = inst.startAutomaticCapture(camera);

    Gson gson = new GsonBuilder().create();

    camera.setConfigJson(gson.toJson(config.config));
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

    if (config.streamConfig != null) {
      server.setConfigJson(gson.toJson(config.streamConfig));
    }

    return camera;
  }

  /**
   * Start running the switched camera.
   */
  public static MjpegServer startSwitchedCamera(SwitchedCameraConfig config) {
    System.out.println("Starting switched camera '" + config.name + "' on " + config.key);
    MjpegServer server = CameraServer.getInstance().addSwitchedCamera(config.name);

    NetworkTableInstance.getDefault()
        .getEntry(config.key)
        .addListener(event -> {
              if (event.value.isDouble()) {
                int i = (int) event.value.getDouble();
                if (i >= 0 && i < cameras.size()) {
                  server.setSource(cameras.get(i));
                }
              } else if (event.value.isString()) {
                String str = event.value.getString();
                for (int i = 0; i < cameraConfigs.size(); i++) {
                  if (str.equals(cameraConfigs.get(i).name)) {
                    server.setSource(cameras.get(i));
                    break;
                  }
                }
              }
            },
            EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

    return server;
  }

  /**
   * Example pipeline.
   */
  public static class MyPipeline implements VisionPipeline {
    public int val;

    @Override
    public void process(Mat mat) {
      val += 1;
    }
  }

  /**
   * Main.
   */
  public static void main(String... args) {
    if (args.length > 0) {
      configFile = args[0];
    }

    // read configuration
    if (!readConfig()) {
      return;
    }

    // start NetworkTables
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    if (server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + team);
      ntinst.startClientTeam(team);
    }
    NetworkTable table = ntinst.getTable("visionTable");
    centerXEntry = table.getEntry("centerX");
    centerYEntry = table.getEntry("centerY");

    // start cameras
    for (CameraConfig config : cameraConfigs) {
      cameras.add(startCamera(config));
    }

    // start switched cameras
    for (SwitchedCameraConfig config : switchedCameraConfigs) {
      startSwitchedCamera(config);
    }

    // start image processing on camera 0 if present if ronald gay= gay
    if (cameras.size() >= 1) {
      VisionThread visionThread = new VisionThread(cameras.get(0), new VisionTracking(), pipeline -> {
        System.out.println("Here 1");
        /*
        CvSink cvSink = new CvSink("tmp");
        cvSink.setSource(cameras.get(0));
        Mat mat = new Mat();                
        CvSource outputStream = CameraServer.getInstance().putVideo("TargetVision", 320, 200);
        System.out.println("Here 2");
        */
        //Rect bb = new Rect();
        //Rect nb = new Rect();

          if (!pipeline.convexHullsOutput().isEmpty()) {
            MatOfPoint biggestContour = pipeline.convexHullsOutput().get(0);
        
            for(int i = 1; i < pipeline.convexHullsOutput().size(); i++) {
              final MatOfPoint contour = pipeline.convexHullsOutput().get(i);
              if (Imgproc.contourArea(contour) > Imgproc.contourArea(biggestContour)) {
                biggestContour = contour;
              }
            }
            if (biggestContour != null) {
              final Rect bbx = Imgproc.boundingRect(biggestContour);
              centerX = bbx.x + bbx.width/2;
            } 
          } else {
            System.out.println("no targets");
            centerX = -1;
          }
          System.out.println("CenterX = " + centerX);
          centerXEntry.setDouble(centerX);
          /*
            MatOfPoint nextBiggestContour = null;
            if (!pipeline.convexHullsOutput().isEmpty()) {
              nextBiggestContour = pipeline.convexHullsOutput().get(0);
              for(int i = 0; i < pipeline.convexHullsOutput().size(); i++) {
                final MatOfPoint contour = pipeline.convexHullsOutput().get(i);
                double area = Imgproc.contourArea(contour);
                double biggestArea = Imgproc.contourArea(biggestContour);
                if (area > biggestArea) {
                  nextBiggestContour = contour;
                }
              }
            }
            if (nextBiggestContour != null) {
              if (Imgproc.contourArea(biggestContour) > 20.0 && Imgproc.contourArea(nextBiggestContour) > 20.0) {
                final Rect bbx = Imgproc.boundingRect(biggestContour);
                final Rect nbx = Imgproc.boundingRect(nextBiggestContour);
                //System.out.println("bb: " + bb.x + " nb: " +nb.x);
                centerX = (bbx.x + (bbx.width/2.0) + nbx.x + (nbx.width/2.0))/2.0;
              } else {
                centerX = -1;
                System.out.println("TOO SMALL!!!");
              }
            }
          } else {
            System.out.println("no targets");
            centerX = -1;
          }*/
          
          /*
          double[] hsvThresholdHue = {40.71454700646263, 103.20819112627986};
		      double[] hsvThresholdSaturation = {43.57014388489208, 255.0};
		      double[] hsvThresholdValue = {227.02338129496403, 255.0};
		      */
        /*while (!Thread.interrupted()) {

          System.out.println("Here 3");
          // Tell the CvSink to grab a frame from the camera and put it
          // in the source mat.  If there is an error notify the output.
          if (cvSink.grabFrame(mat) == 0) {
            System.out.println("Here 00");
            // Send the output the error.
            outputStream.notifyError(cvSink.getError());
            // skip the rest of the current iteration
            //continue;
          }
          System.out.println("Here 5");
          Imgproc.rectangle(mat, new Point(0, 0), new Point(320, 10),new Scalar(0, 255, 0), 1);
          
          Imgproc.rectangle(mat, new Point((int)centerX, 0), new Point((int)centerX, 10),new Scalar(0, 0, 255), 5);

          Imgproc.rectangle(mat, new Point(bb.x ,bb.y), new Point(bb.x + bb.width, bb.y + bb.height),new Scalar(0, 0, 255), 2);
          //Imgproc.rectangle(mat, new Point(nb.x ,nb.y), new Point(nb.x + nb.width, nb.y + nb.height),new Scalar(0, 0, 255), 2);  
          outputStream.putFrame(mat);
          mat.release();
          //cvSink.close();
          //outputStream.close();
          System.out.println("Here 6");
        }
        System.out.println("Here 7");
      */});
      /* something like this for GRIP:
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new GripPipeline(), pipeline -> {
        ...
      });
       */
      VisionThread visionThreadY = new VisionThread(cameras.get(0), new VisionTrackingY(), pipeline -> {

        if (!pipeline.convexHullsOutput().isEmpty()) {
          MatOfPoint biggestContour = pipeline.convexHullsOutput().get(0);
      
          for(int i = 1; i < pipeline.convexHullsOutput().size(); i++) {
            final MatOfPoint contour = pipeline.convexHullsOutput().get(i);
            if (Imgproc.contourArea(contour) > Imgproc.contourArea(biggestContour)) {
              biggestContour = contour;
            }
          }
          if (biggestContour != null) {
            final Rect bbx = Imgproc.boundingRect(biggestContour);
            centerY = bbx.y + bbx.height/2;
          } 
        } else {
          System.out.println("no targets");
          centerY = -1;
        }
        System.out.println("CenterY = " + centerY);
        //centerXEntry.setDouble(centerX);
        centerYEntry.setDouble(centerY);
      });
 
      visionThreadY.start();
      visionThread.start();
    }


    // loop forever
    for (;;) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }
}
