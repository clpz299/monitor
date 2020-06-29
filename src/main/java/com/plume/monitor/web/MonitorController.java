package com.plume.monitor.web;

import com.plume.monitor.service.IMonitorService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author AC
 */
@RestController
public class MonitorController {

    @Value("${web.properity.file-path}")
    private String filePath;

    @Autowired
    private IMonitorService service;

    /**
     * dvr back play video download by time video back play.
     *
     * @param brandName
     * @param ip
     * @param port
     * @param account
     * @param password
     * @param starTime
     * @param endTime
     * @param routes
     * @param channel
     * @return
     */
    @PostMapping("/catch_video/by_time")
    @ApiOperation(value = "POST获取网络录像机影像", notes = "POST获取网络录像机影像，参数以 form-data 格式进行传输", httpMethod = "POST")
    public String fetchVideoByTime(
            @ApiParam(name = "brandName", value = "设备品牌名称", required = true) @RequestParam(value = "brandName") String brandName,
            @ApiParam(name = "ip", value = "设备IP地址", required = true) @RequestParam(value = "ip") String ip,
            @ApiParam(name = "port", value = "设备端口号", required = true) @RequestParam(value = "port") int port,
            @ApiParam(name = "account", value = "登录设备账号", required = true) @RequestParam(value = "account") String account,
            @ApiParam(name = "password", value = "登录设备密码", required = true) @RequestParam(value = "password") String password,
            @ApiParam(name = "starTime", value = "视频开始时间 (毫秒时间戳)", required = true) @RequestParam(value = "starTime") long starTime,
            @ApiParam(name = "endTime", value = "视频结束时间  (毫秒时间戳)", required = true) @RequestParam(value = "endTime") long endTime,
            @ApiParam(name = "routes", value = "硬盘机路数", required = true) @RequestParam(value = "routes") int routes,
            @ApiParam(name = "channel", value = "通道", required = true) @RequestParam(value = "channel") int channel) {
        if (brandName == null || brandName.isEmpty()) {
            return null;
        }
        switch (brandName) {
            case "HK":
                return service.fetchHKVideo(ip, port, account, password, starTime, endTime, routes, (short) channel);
            case "DH":
                return service.fetchDHVideo(ip, port, account, password, starTime, endTime, routes, (short) channel);
            default:
                return "BrandName  Error";
        }

    }

    /**
     * dvr catch picture return bytes.
     *
     * @param brandName
     * @param ip
     * @param port
     * @param routes
     * @param channel
     * @param account
     * @param password
     * @return
     */
    @PostMapping("/catch_picture")
    @ApiOperation(value = "POST获取摄像头抓图(响应 bate流)", notes = "POST获取摄像头抓图，参数以 form-data 格式进行传输", httpMethod = "POST")
    public byte[] fetchCatchPicture(
            @ApiParam(name = "brandName", value = "设备品牌名称", required = true) @RequestParam(value = "brandName") String brandName,
            @ApiParam(name = "ip", value = "设备IP地址", required = true) @RequestParam(value = "ip") String ip,
            @ApiParam(name = "port", value = "设备端口号", required = true) @RequestParam(value = "port") int port,
            @ApiParam(name = "routes", value = "硬盘机路数", required = true) @RequestParam(value = "routes") int routes,
            @ApiParam(name = "channel", value = "通道", required = true) @RequestParam(value = "channel") int channel,
            @ApiParam(name = "account", value = "登录设备账号", required = true) @RequestParam(value = "account") String account,
            @ApiParam(name = "password", value = "登录设备密码", required = true) @RequestParam(value = "password") String password) {
        try {
            FileInputStream in = getPictureStream(brandName, ip, port, routes, channel, account, password);
            byte[] bytes = new byte[in.available()];
            in.read(bytes, 0, in.available());
            return bytes;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * dvr catch picture return bytes.
     *
     * @param brandName
     * @param ip
     * @param port
     * @param routes
     * @param channel
     * @param account
     * @param password
     * @return
     */
    @PostMapping("/catch_picture_base64")
    @ApiOperation(value = "POST获取摄像头抓图(响应 Base64编码图片，前端使用 [data:img/jpeg;base64,] 接收)", notes = "POST获取摄像头抓图，参数以 form-data 格式进行传输", httpMethod = "POST")
    public String fetchCatchPictureByBase64(
            @ApiParam(name = "brandName", value = "设备品牌名称", required = true) @RequestParam(value = "brandName") String brandName,
            @ApiParam(name = "ip", value = "设备IP地址", required = true) @RequestParam(value = "ip") String ip,
            @ApiParam(name = "port", value = "设备端口号", required = true) @RequestParam(value = "port") int port,
            @ApiParam(name = "routes", value = "硬盘机路数", required = true) @RequestParam(value = "routes") int routes,
            @ApiParam(name = "channel", value = "通道", required = true) @RequestParam(value = "channel") int channel,
            @ApiParam(name = "account", value = "登录设备账号", required = true) @RequestParam(value = "account") String account,
            @ApiParam(name = "password", value = "登录设备密码", required = true) @RequestParam(value = "password") String password) {
        byte[] data = null;
        try {
            FileInputStream in = getPictureStream(brandName, ip, port, routes, channel, account, password);
            data = new byte[in.available()];
            in.read(data);
            in.close();
            // 对字节数组Base64编码
            BASE64Encoder encoder = new BASE64Encoder();
            // 返回Base64编码过的字节数组字符串
            return encoder.encode(data);
        } catch (Exception e) {
            return "brand or params error";
        }

    }

    private FileInputStream getPictureStream(String brandName, String ip, int port, int routes, int channel,
                                             String account, String password) {
        String fileName = null;
        FileInputStream in = null;

        try {
            if (brandName == null || brandName.isEmpty()) {
                return null;
            }
            if (brandName.equals("HK")) {
                fileName = service.fetchHKCatchPicture(ip, port, routes, channel, account, password);
            }

            if (brandName.equals("DH")) {
                fileName = service.fetchDHCatchPicture(ip, port, account, password, channel);
            }

            if (fileName == null || fileName.isEmpty()) {
                throw new RuntimeException("生成文件失败！");
            }
            File file = new File(fileName);
            in = new FileInputStream(file);
        } catch (Exception e) {

        }
        return in;
    }
}
