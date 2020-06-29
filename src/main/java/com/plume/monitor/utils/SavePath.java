package com.plume.monitor.utils;

import com.plume.monitor.vo.ToolKits;

import java.io.File;

/**
 * @author AC
 */
public class SavePath {
    private SavePath() {
    }

    private static class SavePathHolder {
        private static SavePath instance = new SavePath();
    }

    public static SavePath getSavePath() {
        return SavePathHolder.instance;
    }

    String s_captureSavePath = "./Capture/" + ToolKits.getDay() + "/"; // 抓拍图片保存路径
    String s_imageSavePath = "./Image/" + ToolKits.getDay() + "/"; // 图片保存路径
    String s_recordFileSavePath = "./RecordFile/" + ToolKits.getDay() + "/"; // 录像保存路径

    /*
     * 设置抓图保存路径
     */
    public String getSaveCapturePath() {
        File path1 = new File("./Capture/");
        if (!path1.exists()) {
            path1.mkdir();
        }

        File path2 = new File(s_captureSavePath);
        if (!path2.exists()) {
            path2.mkdir();
        }

        String strFileName = path2.getAbsolutePath() + "/" + ToolKits.getDate() + System.currentTimeMillis() + ".jpg";

        return strFileName;
    }

    /*
     * 设置智能交通图片保存路径
     */
    public String getSaveTrafficImagePath() {
        File path1 = new File("./Image/");
        if (!path1.exists()) {
            path1.mkdir();
        }

        File path = new File(s_imageSavePath);
        if (!path.exists()) {
            path.mkdir();
        }

        return s_imageSavePath;
    }

    /*
     * 设置录像保存路径
     */
    public String getSaveRecordFilePath(String fileName) {
        File path1 = new File("./RecordFile/");
        if (!path1.exists()) {
            path1.mkdir();
        }

        File path2 = new File(s_recordFileSavePath);
        if (!path2.exists()) {
            path2.mkdir();
        }

        String SavedFileName = s_recordFileSavePath + ToolKits.getDate() + fileName + ".dav"; // 默认保存路径

        if (fileName.indexOf("mp4") != -1) {
            SavedFileName = s_recordFileSavePath + ToolKits.getDate() + fileName; // if file is mp4 fix
        }

        return SavedFileName;
    }

}
