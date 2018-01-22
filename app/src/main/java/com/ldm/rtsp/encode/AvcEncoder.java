package com.ldm.rtsp.encode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;


public class AvcEncoder
{
	private final static String TAG = "MeidaCodec";
	
	private int TIMEOUT_USEC = 10000;

	private MediaCodec mediaCodec;
	int m_width;
	int m_height;
	int m_framerate;
	byte[] m_info = null;
	 
	public byte[] configbyte; 


	@SuppressLint("NewApi")
	public AvcEncoder(int width, int height, int framerate, int bitrate) { 
		
		m_width  = width;
		m_height = height;
		m_framerate = framerate;
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		getMediaCodecList();
	
	    MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

	    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    mediaCodec.start();
	    createfile();
	}



	@SuppressLint("NewApi")
	public void getMediaCodecList(){
		//获取解码器列表
		int numCodecs = MediaCodecList.getCodecCount();
		MediaCodecInfo codecInfo = null;
		for(int i = 0; i < numCodecs && codecInfo == null ; i++){
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if(!info.isEncoder()){
				continue;
			}
			String[] types = info.getSupportedTypes();
			boolean found = false;
			//轮训所要的解码器
			for(int j=0; j<types.length && !found; j++){
				if(types[j].equals("video/avc")){
					System.out.println("found");
					found = true;
				}
			}
			if(!found){
				continue;
			}
			codecInfo = info;
		}
		Log.d(TAG, "found"+codecInfo.getName() + "supporting" +" video/avc");



		//检查所支持的colorspace
		int colorFormat = 0;
		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
		System.out.println("length-"+capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));
		for(int i = 0; i < capabilities.colorFormats.length && colorFormat == 0 ; i++){
			int format = capabilities.colorFormats[i];
			switch (format) {
				case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
					Log.e(TAG,"COLOR_FormatYUV420Planar");
					break;
				case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
					Log.e(TAG,"COLOR_FormatYUV420PackedPlanar");
					break;
				case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
					Log.e(TAG,"COLOR_FormatYUV420SemiPlanar");
					break;
				case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
					Log.e(TAG,"COLOR_FormatYUV420PackedSemiPlanar");
					break;
				case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
					colorFormat = format;
					Log.e(TAG,"COLOR_TI_FormatYUV420PackedSemiPlanar");
					break;
				default:
					Log.e(TAG, "Skipping unsupported color format "+format);
					break;
			}
		}
		Log.e(TAG, "color format "+colorFormat);
	}

	
	private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/encode.h264";
	private BufferedOutputStream outputStream;
	FileOutputStream outStream;
	private void createfile(){
		File file = new File(path);
		if(file.exists()){
			file.delete();
		}
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(file));
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	}

	@SuppressLint("NewApi")
	private void StopEncoder() {
	    try {
	        mediaCodec.stop();
	        mediaCodec.release();
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	
	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	public boolean isRuning = false;
	
	public void StopThread(){
		isRuning = false;
		StopEncoder();
//		try {
//			outputStream.flush();
//	        outputStream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	int count = 0;

	public void StartEncoderThread(){
		Thread EncoderThread = new Thread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				isRuning = true;
				byte[] input = null;
				long pts =  0;
				long generateIndex = 0;

				while (isRuning) {
//					NV21ToNV12(input,yuv420sp,m_width,m_height);

//					Log.i("AvcEncoder", "EncodeActivity.YUVQueue.size() " + EncodeActivity.YUVQueue.size());

					if (EncodeActivity.YUVQueue.size() >0){
						input = EncodeActivity.YUVQueue.poll();
//						byte[] yuv420sp = new byte[m_width*m_height*3/2];
//						NV21ToNV12(input,yuv420sp,m_width,m_height);
//						swapYV12toI420(input,yuv420sp,m_width,m_height);
//						input = yuv420sp;
					}
					else{
						input = null;
					}

					if (input != null) {
//						Log.i("AvcEncoder", "got input " + input.length );
						try {
							long startMs = System.currentTimeMillis();
							ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
							ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
							int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);

//							Log.i("AvcEncoder", "inputBufferIndex = " + inputBufferIndex );

							if (inputBufferIndex >= 0) {
								pts = computePresentationTime(generateIndex);
								ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(input);
								mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
								generateIndex += 1;
							}
							
							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);

//							Log.i("AvcEncoder", "outputBufferIndex = " + outputBufferIndex );

							while (outputBufferIndex >= 0) {
//								Log.e("AvcEncoder",
//										"Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
								ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								byte[] outData = new byte[bufferInfo.size];
								outputBuffer.get(outData);
								if(bufferInfo.flags == 2){
									configbyte = new byte[bufferInfo.size];
									configbyte = outData;
								}else if(bufferInfo.flags == 1){
									byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
									System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
									System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
									EncodeActivity.putDecodeData(keyframe);

									outputStream.write(keyframe, 0, keyframe.length);
								}else{

									EncodeActivity.putDecodeData(outData);

									outputStream.write(outData, 0, outData.length);
								}

								mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
								outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							}

						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		EncoderThread.start();
		
	}
	
	private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
		if(nv21 == null || nv12 == null)return;
		int framesize = width*height;
		int i = 0,j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
//		for(i = 0; i < framesize; i++){
//			nv12[i] = nv21[i];
//		}
		for (j = 0; j < framesize/2; j+=2)
		{
		  nv12[framesize + j-1] = nv21[j+framesize];
            nv12[framesize + j] = nv21[j+framesize-1];
		}

	}
	private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height)
	{
		System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
		System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
		System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);
	}
	
    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }
}
