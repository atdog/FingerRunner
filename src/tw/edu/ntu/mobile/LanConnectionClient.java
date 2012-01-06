package tw.edu.ntu.mobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.util.Log;

public class LanConnectionClient {
	private DatagramSocket broadClient;
	private Socket client;
	private final int DiscoveryPort = 9877;
	private Context context;
	private List<host> hostsList = new ArrayList<host>();
	private String nickname;
	private Handler mainHandler;
	private Handler handler;
	private Handler socketHandler;

	public LanConnectionClient(Context context,String nickname, Handler mainHandler) {
		this.context = context;
		this.nickname = nickname;
		this.mainHandler = mainHandler;
		try {
			broadClient = new DatagramSocket(DiscoveryPort);
			broadClient.setBroadcast(true);
			broadClient.setReuseAddress(true);

			byte[] buffer = "discovery".getBytes();

			// Log.d("test",getBroadcastAddress().toString());
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
					getBroadcastAddress(), DiscoveryPort);
			broadClient.send(packet);

			new Thread(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					try {
						while (true) {
							byte[] recBuf = new byte[1024];
							DatagramPacket recPacket = new DatagramPacket(
									recBuf, recBuf.length);
							broadClient.receive(recPacket);
							if(!recPacket.getAddress().getHostName().equals(getMyIP())) {
								hostsList.add(new host(recPacket.getAddress()
										.getHostName(), new String(recBuf, 0,
												recPacket.getLength())));
								Log.d("test", "Discover Server: "
										+ recPacket.getAddress().getHostName());
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.d("test", "Terminated broadcast receive thread ");
					}
				}
			}).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	public host[] showHosts() {
		return hostsList.toArray(new host[0]);
	}

	public void startTCPconnection(InetAddress address) {
		broadClient.close();
		client = new Socket();
		try {
			client.setReuseAddress(true);
			InetSocketAddress isa = new InetSocketAddress(address,
					DiscoveryPort);
			client.connect(isa, 10000);
			sendData("name:"+nickname);

			new Thread(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					try {
						BufferedInputStream in = new BufferedInputStream(
								client.getInputStream());
						byte[] b = new byte[1024];
						String data;
						int length;
						Pattern pattern = Pattern.compile("name:(.*)");
						Pattern locPattern = Pattern.compile("start:(.*),end:(.*)");
						while ((length = in.read(b)) > 0)// <=0的話就是結束了
						{
							data = new String(b, 0, length);
							Matcher matcher = pattern.matcher(data);
							Matcher locMatcher = locPattern.matcher(data);
							if(matcher.matches()) {
								Message msg = new Message();
								Bundle bundle = new Bundle();
								bundle.putString("name", matcher.group(1));
								msg.setData(bundle);
								mainHandler.sendMessage(msg);
							}
							if(locMatcher.matches()) {
								Message msg = new Message();
								Bundle bundle = new Bundle();
								bundle.putString("start", locMatcher.group(1));
								bundle.putString("end", locMatcher.group(2));
								msg.setData(bundle);
								handler.sendMessage(msg);
							}
							Log.d("test", "Data:" + data);
						}

					} catch (IOException e) {
						Log.d("test", "Terminate client tcp connection");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendData(String data) {
		try {
			synchronized (client) {
				BufferedOutputStream out = new BufferedOutputStream(
						client.getOutputStream());
				out.write(data.getBytes());
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	} 

	public void stopClient() {
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getMyIP() {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	}
	
	public void setSocketHandler(Handler handler) {
		socketHandler = handler;
	}
}
