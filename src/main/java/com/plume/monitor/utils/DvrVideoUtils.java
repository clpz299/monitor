package com.plume.monitor.utils;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.plume.monitor.service.impl.MonitorServiceImpl;
import com.plume.monitor.vo.Dvr;
import com.plume.monitor.vo.HCNetSDK;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import ws.schild.jave.*;

/**
 * @author AC
 */
public class DvrVideoUtils {

    @Value("${web.properity.file-path}")
    private String filePath;

    private static HCNetSDK hcNetSDK = HCNetSDK.INSTANCE;
    private NativeLong userId;// 用户句柄
    private NativeLong loadHandle;// 下载句柄

    private Logger logger = LoggerFactory.getLogger(DvrVideoUtils.class);

    /**
     * 按时间下载视频
     *
     * @param dvr
     * @param startTime
     * @param endTime
     * @param filePath
     * @param channel
     * @return
     */
    public String downloadVideo(Dvr dvr, Date startTime, Date endTime, String filePath, int channel)
            throws EncoderException {
        String fileName = filePath;
        boolean initFlag = hcNetSDK.NET_DVR_Init();
        if (!initFlag) { // 返回值为布尔值 fasle初始化失败
            logger.info("hcNetSDK : " + "init fail!");
            return "400011";
        }
        HCNetSDK.NET_DVR_DEVICEINFO_V30 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        userId = hcNetSDK.NET_DVR_Login_V30(dvr.getIp(), dvr.getPort().shortValue(), dvr.getUsername(), dvr.getPwd(),
                deviceInfo);
        long lUserId = userId.longValue();
        if (lUserId == -1) {
            logger.info("hcNetSDK : " + "login fail!");
            return "400013";
        }
        loadHandle = new NativeLong(-1);
        if (loadHandle.intValue() == -1) {
            loadHandle = hcNetSDK.NET_DVR_GetFileByTime(userId, new NativeLong((channel + 32)), getHkTime(startTime),
                    getHkTime(endTime), filePath);
            if (loadHandle.intValue() >= 0) {
                boolean downloadFlag = hcNetSDK.NET_DVR_PlayBackControl(loadHandle, hcNetSDK.NET_DVR_PLAYSTART, 0,
                        null);
                int tmp = -1;
                IntByReference pos = new IntByReference();
                while (true) {
                    boolean backFlag = hcNetSDK.NET_DVR_PlayBackControl(loadHandle, hcNetSDK.NET_DVR_PLAYGETPOS, 0,
                            pos);
                    if (!backFlag) {// 防止单个线程死循环
                        return downloadFlag ? fileName : "300032";
                    }
                    int produce = pos.getValue();
                    if ((produce % 10) == 0 && tmp != produce) {// 输出进度
                        tmp = produce;
                    }
                    if (produce == 100) {// 下载成功
                        hcNetSDK.NET_DVR_StopGetFile(loadHandle);
                        loadHandle.setValue(-1);
                        hcNetSDK.NET_DVR_Logout(userId);// 退出录像机
                        File source = new File(filePath);
                        String targetFileName = SavePath.getSavePath().getSaveRecordFilePath(dvr.getIp() + channel
                                + startTime.getTime() + endTime.getTime() + "trans" + ".mp4");
                        File target = new File(targetFileName);
                        MultimediaObject multimediaObject = new MultimediaObject(source);
                        AudioAttributes audio = new AudioAttributes();
                        audio.setCodec("libmp3lame");
                        audio.setBitRate(128000);
                        audio.setChannels(2);
                        audio.setSamplingRate(44100);
                        VideoAttributes video = new VideoAttributes();
                        video.setCodec("libx264");
                        video.setBitRate(800000);
                        video.setFrameRate(15);
                        EncodingAttributes attrs = new EncodingAttributes();
                        attrs.setFormat("mp4");
                        attrs.setAudioAttributes(audio);
                        attrs.setVideoAttributes(video);
                        Encoder encoder = new Encoder();
                        encoder.encode(multimediaObject, target, attrs);
                        return targetFileName;
                    }
                    if (produce > 100) {
                        hcNetSDK.NET_DVR_StopGetFile(loadHandle);
                        loadHandle.setValue(-1);
                        throw new RuntimeException("300032");
                    }
                }
            } else {
                System.out.println(fileName);
                logger.info("hksdk(视频)-下载失败" + hcNetSDK.NET_DVR_GetLastError());
                throw new RuntimeException("300032");
            }
        }
        throw new RuntimeException("300032");
    }

    /**
     * 获取海康录像机格式的时间
     *
     * @param time
     * @return
     */
    public HCNetSDK.NET_DVR_TIME getHkTime(Date time) {
        HCNetSDK.NET_DVR_TIME structTime = new HCNetSDK.NET_DVR_TIME();
        String str = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(time);
        String[] times = str.split("-");
        structTime.dwYear = Integer.parseInt(times[0]);
        structTime.dwMonth = Integer.parseInt(times[1]);
        structTime.dwDay = Integer.parseInt(times[2]);
        structTime.dwHour = Integer.parseInt(times[3]);
        structTime.dwMinute = Integer.parseInt(times[4]);
        structTime.dwSecond = Integer.parseInt(times[5]);
        return structTime;
    }


}
