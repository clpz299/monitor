package com.plume.monitor.service.impl;

import com.sun.jna.Pointer;
import com.plume.monitor.model.CapturePictureModule;
import com.plume.monitor.model.LoginModule;
import com.plume.monitor.service.IMonitorService;
import com.plume.monitor.utils.DHDvrVideoUtils;
import com.plume.monitor.utils.DvrVideoUtils;
import com.plume.monitor.utils.SavePath;
import com.plume.monitor.vo.Dvr;
import com.plume.monitor.vo.HKNetSDKManger;
import com.plume.monitor.vo.NetSDKLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ws.schild.jave.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author AC
 */
@Service
public class MonitorServiceImpl implements IMonitorService {

	@Value("${web.properity.file-path}")
	private String filePath;

	private Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

	private int m_iChanShowNum;

	private DisConnect disConnect = new DisConnect();
	private HaveReConnect haveReConnect = new HaveReConnect();
	private CountDownLatch latch = null;

	private String savePicturefileName;

	@Override
	public String fetchDHCatchPicture(String ip, int port, String userName, String password, int channel)
			throws InterruptedException {
		latch = new CountDownLatch(1);

		LoginModule.init(disConnect, haveReConnect);

		boolean result = LoginModule.login(ip, port, userName, password);
		logger.info("登录结果：" + result);

		CapturePictureModule.setSnapRevCallBack(m_CaptureReceiveCB);

		NetSDKLib.LLong m_hPlayHandle = LoginModule.netsdk.CLIENT_RealPlayEx(LoginModule.m_hLoginHandle, 0, null, 0);
		CapturePictureModule.remoteCapturePicture(channel - 1);
		latch.await(60, TimeUnit.SECONDS);

		LoginModule.netsdk.CLIENT_StopRealPlayEx(m_hPlayHandle);
		LoginModule.logout();
		LoginModule.cleanup();

		return savePicturefileName;
	}

	@Override
	public String fetchHKCatchPicture(String ip, int port, int routes, int channel, String userName, String password) {
		String fileName = null;
		try {
			fileName = SavePath.getSavePath().getSaveCapturePath();
			HKNetSDKManger.getManager().catchPicture(HKNetSDKManger.getManager().regedit(ip, port, userName, password),
					routes >= 64 ? channel : (channel + 32), fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}

	public fCaptureReceiveCB m_CaptureReceiveCB = new fCaptureReceiveCB();

	public class fCaptureReceiveCB implements NetSDKLib.fSnapRev {
		BufferedImage bufferedImage = null;

		public void invoke(NetSDKLib.LLong lLoginID, Pointer pBuf, int RevLen, int EncodeType, int CmdSerial,
				Pointer dwUser) {
			if (pBuf != null && RevLen > 0) {
				String strFileName = SavePath.getSavePath().getSaveCapturePath();
				byte[] buf = pBuf.getByteArray(0, RevLen);
				ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buf);
				try {
					bufferedImage = ImageIO.read(byteArrInput);
					if (bufferedImage == null) {
						return;
					}
					ImageIO.write(bufferedImage, "jpg", new File(strFileName));
					savePicturefileName = strFileName;
					System.out.print(savePicturefileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			latch.countDown();
		}
	}

	private class DisConnect implements NetSDKLib.fDisConnect {
		public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			logger.info("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
		}
	}

	private class HaveReConnect implements NetSDKLib.fHaveReConnect {
		public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
			logger.info("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
		}
	}

	@Override
	public String fetchHKVideo(String ip, int port, String userName, String password, long starTime, long endTime,
			int routes, short channel) throws RuntimeException {
		DvrVideoUtils dvd = new DvrVideoUtils();
		Dvr dvr = new Dvr(0, ip, port, userName, password, null, 0);

		try {
			String sFileName = SavePath.getSavePath()
					.getSaveRecordFilePath(ip + routes + channel + m_iChanShowNum + starTime + endTime + ".mp4");
			String targetTransFileName = dvd.downloadVideo(dvr, new Date(starTime), new Date(endTime), sFileName,
					channel);
			if (null != targetTransFileName) {
				File file = new File(sFileName);
				if (file.exists()) {
					if (file.delete()) {
						logger.info("1-删除成功 {}", targetTransFileName);
					} else {
						logger.info("0-删除失败 {}", targetTransFileName);
					}
				} else {
					logger.info("文件不存在！ {}", targetTransFileName);
				}

			}
			return targetTransFileName;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String fetchDHVideo(String ip, int port, String userName, String password, long starTime, long endTime,
			int routes, short channel) {
		Dvr dvr = new Dvr(0, ip, port, userName, password, null, 0);
		try {
			return new DHDvrVideoUtils().dvrDownloadByTimeEx(dvr, new Date(starTime), new Date(endTime), null, channel);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
