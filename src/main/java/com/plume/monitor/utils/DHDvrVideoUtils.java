package com.plume.monitor.utils;

import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.plume.monitor.model.DownLoadRecordModule;
import com.plume.monitor.model.LoginModule;
import com.plume.monitor.service.impl.MonitorServiceImpl;
import com.plume.monitor.vo.Dvr;
import com.plume.monitor.vo.HCNetSDK;
import com.plume.monitor.vo.NetSDKLib;
import com.plume.monitor.vo.ToolKits;
import ws.schild.jave.*;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 
 * /**
 * 
 * @author chan
 *
 */
public class DHDvrVideoUtils {

	private String sourceFileName;

	private String targetFileName;

	private Logger logger = LoggerFactory.getLogger(DHDvrVideoUtils.class);

	private NetSDKLib.LLong m_hDownLoadByTimeHandle = new NetSDKLib.LLong(0); // 按时间下载句柄
	// 回调函数中使用的表示下载完成变量
	private boolean b_downloadByTime = false;

	private CountDownLatch latch = null;

	public String dvrDownloadByTimeEx(Dvr dvr, Date startTime, Date endTime, String filePath, int channel)
			throws InterruptedException {

		NetSDKLib.NET_TIME stTimeStart = getDHTime(startTime);
		NetSDKLib.NET_TIME stTimeEnd = getDHTime(endTime);

		// 设备断线通知回调
		DisConnect disConnect = new DisConnect();

		// 网络连接恢复
		HaveReConnect haveReConnect = new HaveReConnect();

		latch = new CountDownLatch(1);

		LoginModule.init(disConnect, haveReConnect); // 打开工程，初始化
		Native.setCallbackThreadInitializer(m_DownLoadPosByTime,
				new CallbackThreadInitializer(false, false, "downloadbytime callback thread"));
		if (LoginModule.login(dvr.getIp(), dvr.getPort(), dvr.getUsername(), dvr.getPwd())) {
			// 默认设置主辅码流
			DownLoadRecordModule.setStreamType(0);
		} else {
			logger.info("dhNetSDK : " + "login fail!");
			return "400013";
		}

		// 记录时间相差
		int time = 0;

		if (stTimeEnd.dwDay - stTimeStart.dwDay == 1) {
			time = (24 + stTimeEnd.dwHour) * 60 * 60 + stTimeEnd.dwMinute * 60 + stTimeEnd.dwSecond
					- stTimeStart.dwHour * 60 * 60 - stTimeStart.dwMinute * 60 - stTimeStart.dwSecond;
		} else {
			time = stTimeEnd.dwHour * 60 * 60 + stTimeEnd.dwMinute * 60 + stTimeEnd.dwSecond
					- stTimeStart.dwHour * 60 * 60 - stTimeStart.dwMinute * 60 - stTimeStart.dwSecond;
		}
		System.out.println("time :" + time);
		if (time > 6 * 60 * 60 || time <= 0) {
			logger.info("dhNetSDK : " + "time fail!");
			return "300032";
		}

		if (!b_downloadByTime) {

			sourceFileName = SavePath.getSavePath()
					.getSaveRecordFilePath(dvr.getIp() + channel + startTime.getTime() + endTime.getTime());

			m_hDownLoadByTimeHandle = DownLoadRecordModule.downloadRecordFile(1, 0, stTimeStart, stTimeEnd,
					sourceFileName, m_DownLoadPosByTime);
			if (m_hDownLoadByTimeHandle.longValue() != 0) {
				b_downloadByTime = true;
			} else {
				System.out.println(ToolKits.getErrorCodeShow() + ":" + Res.string().getErrorMessage());
			}
		} else {
			DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
			b_downloadByTime = false;
		}

		latch.await(60, TimeUnit.SECONDS);
		LoginModule.logout();
		LoginModule.cleanup(); // 关闭工程，释放资源
		return targetFileName;
	}

	/*
	 * 按时间下载回调
	 */
	private DownLoadPosCallBackByTime m_DownLoadPosByTime = new DownLoadPosCallBackByTime(); // 录像下载进度

	class DownLoadPosCallBackByTime implements NetSDKLib.fTimeDownLoadPosCallBack {
		public void invoke(NetSDKLib.LLong lLoginID, final int dwTotalSize, final int dwDownLoadSize, int index,
				NetSDKLib.NET_RECORDFILE_INFO.ByValue recordfileinfo, Pointer dwUser) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (dwDownLoadSize == -1) {
						DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
						b_downloadByTime = false;
						MultimediaObject multimediaObject = new MultimediaObject(new File(sourceFileName));
						targetFileName = sourceFileName.substring(0, sourceFileName.lastIndexOf(".") + 1);
						targetFileName = targetFileName + ".mp4";
						File target = new File(targetFileName);
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
						try {
							encoder.encode(multimediaObject, target, attrs);
						} catch (EncoderException e) {
							e.printStackTrace();
						}
						latch.countDown();
					}
				}

			});
		}
	}

	// 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
	private class DisConnect implements NetSDKLib.fDisConnect {
		public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
			// 断线提示
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					DownLoadRecordModule.stopDownLoadRecordFile(m_hDownLoadByTimeHandle);
				}
			});
		}
	}

	// 网络连接恢复，设备重连成功回调
	// 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
	private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
		public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

			// 重连提示
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					System.out.printf(Res.string().getDownloadRecord() + " : " + Res.string().getOnline());
				}
			});
		}
	}

	/**
	 * 获取海康录像机格式的时间
	 *
	 * @param time
	 * @return
	 */
	public NetSDKLib.NET_TIME getDHTime(Date time) {
		NetSDKLib.NET_TIME structTime = new NetSDKLib.NET_TIME();
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
