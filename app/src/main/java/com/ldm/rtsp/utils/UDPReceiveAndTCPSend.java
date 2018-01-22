package com.ldm.rtsp.utils;

/**
 * Created by 八心戈 on 2018/1/16.
 */
import android.util.Log;



import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


/*接收udp多播 并 发送tcp 连接*/
public class UDPReceiveAndTCPSend extends  Thread {
    UDPResponseCallback mUDPResponseCallback;

    public DatagramSocket datagramSocket=null;

    public DatagramPacket dp;

//    public UDPDataParser mUDPParser;

//    public UDPAccept mUDPAccept;

    public String mIPaddress;

    public String mPort;

    public int DEFAULT_PORT=12426;

    public StringBuffer sb;

    public UDPReceiveAndTCPSend(UDPResponseCallback callback){
        this.mUDPResponseCallback=callback;
//        mUDPParser=new UDPDataParser();

    }
    @Override
    public void run() {
        byte[] data = new byte[1024];
        try {
            if(datagramSocket==null) {
                datagramSocket=new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(DEFAULT_PORT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                dp = new DatagramPacket(data, data.length);
                if (datagramSocket != null) {
                    datagramSocket.receive(dp);
                }
            } catch (Exception e) {
                Log.i("Data","UDPReceeive 已经挂啦");
                e.printStackTrace();
            }
            if(dp != null){
//                String response= ByteUtils.bytesToHexString(dp.getData());
//                Log.i("Data", "UDP response = " + response);
                byte[] data1=dp.getData();
//                mUDPAccept=mUDPParser.analysis(data1);
//                if(mUDPAccept != null) {
//                    mIPaddress = mUDPParser.setIPAddress(mUDPAccept.getIPAddress());
//                    mPort =mUDPParser.setPort(mUDPAccept.getPort());
//                }
                Log.i("Data", "The Ip=" + mIPaddress);
                Log.i("Data","The Port = "+mPort);
                if (mIPaddress != null) {
                    final String quest_ip = dp.getAddress().toString().substring(1);   //从UDP包中解析出IP地址
                    String host_ip = getLocalHostIp();   //若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)
                    if ((!host_ip.equals("")) && host_ip.equals(quest_ip.substring(1))) {
                        continue;
                    }
                    if(mIPaddress.equals("0.0.0.0")){
                        mUDPResponseCallback.onResponse(null,false);
                    }
                    sb=new StringBuffer();
                    sb.append(mIPaddress);
                    sb.append(",");
                    sb.append(mPort);
                    Log.i("Data","UDP result="+sb.toString());
                    mUDPResponseCallback.onResponse(sb.toString(), true);     //将IP地址传递回去
                    datagramSocket.close();
                    break;
                }

            }

        }
    }



    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
//                    if (!ip.isLoopbackAddress()
//                            && InetAddressUtils.isIPv4Address(ip
//                            .getHostAddress())) {
//                        return ip.getHostAddress();
//                    }
                }
            }
        }
        catch(SocketException e)
        {
            Log.e("Data", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }


}