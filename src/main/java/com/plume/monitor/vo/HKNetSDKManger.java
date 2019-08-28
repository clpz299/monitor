package com.plume.monitor.vo;

import com.sun.jna.NativeLong;

public class HKNetSDKManger {
    private static HKNetSDKManger manager = null;

    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    static PlayCtrl playControl = PlayCtrl.INSTANCE;

    // 海康威视sdk path
    private String hkNetSDKPath;
    // play Path
    private String playCtrlPath;

    // 初始化结果
    private boolean initSuc = false;

    public HKNetSDKManger() {
        // 初始化SDK
        initSuc = hCNetSDK.NET_DVR_Init();
    }

    public static HKNetSDKManger getManager() {
        if (manager == null)
            manager = new HKNetSDKManger();
        return manager;
    }

    /**
     * 获取HCNetSDK.dll位置
     *
     * @return
     */
    public String getHCNETSDKPath() {
        if (hkNetSDKPath == null)
            hkNetSDKPath = getClass().getResource("HCNetSDK.dll").getPath()
                    .substring(1);
        return hkNetSDKPath;
    }

    /**
     * 获取PlayCtrl.dll位置
     *
     * @return
     */
    public String getPlayCtrlPath() {
        if (playCtrlPath == null)
            playCtrlPath = getClass().getResource("PlayCtrl.dll").getPath()
                    .substring(1);
        return playCtrlPath;
    }

    /**
     * 登陆设备
     *
     * @param ip
     * @param port
     * @param userName
     * @param password
     * @return
     */
    public NativeLong regedit(String ip, int port, String userName,
                              String password) {
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = port;
        NativeLong lUserID = hCNetSDK.NET_DVR_Login_V30(ip, (short) iPort,
                userName, password, m_strDeviceInfo);
        return lUserID;
    }

    /**
     * 获取图片
     *
     * @param channelNumber
     * @param targetPath
     * @return
     */
    public boolean catchPicture(NativeLong lUserID, long channelNumber,
                                String targetPath) {
        if (hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, new NativeLong(
                channelNumber), new HCNetSDK.NET_DVR_JPEGPARA(), targetPath)) {
            return true;

        }
        return false;
    }

    public String getHkNetSDKPath() {
        return hkNetSDKPath;
    }

    public void setHkNetSDKPath(String hkNetSDKPath) {
        this.hkNetSDKPath = hkNetSDKPath;
    }

    public void setPlayCtrlPath(String playCtrlPath) {
        this.playCtrlPath = playCtrlPath;
    }
}
