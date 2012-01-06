package tw.edu.ntu.mobile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LanConnectionServer {
	private final Handler mainHandler;
	private ServerSocket server;
	public DatagramSocket broadServer;
	private Thread broadcastThread;
	private final int DiscoveryPort = 9877;
	private List<Socket> client = new ArrayList<Socket>();
	private String nickname;

	public LanConnectionServer(Handler mainHandler,String nickname) {
		this.mainHandler = mainHandler;
		this.nickname = nickname;
		try {
			server = new ServerSocket(DiscoveryPort);
			server.setReuseAddress(true);
			broadServer = new DatagramSocket(DiscoveryPort);
			broadServer.setReuseAddress(true);
		} catch (java.io.IOException e) {
			System.out.println("Socket啟動有問題 !");
			System.out.println("IOException :" + e.toString());
		}

	}

	public void startHandleBroadcastPacket(final String raceName) {
		broadcastThread = new Thread(new Runnable() {
			public void run() {
				try {
					byte[] buffer = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buffer,
							buffer.length);
					while (true) {
						Log.d("test", "start Hanlder response");
						broadServer.receive(packet);
						Log.d("test", packet.getAddress().getHostName());
						byte[] sendBuf = raceName.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendBuf,
								sendBuf.length, packet.getAddress(),
								packet.getPort());
						broadServer.send(sendPacket);

						packet.setLength(buffer.length);
					}
				} catch (IOException e) {
					Log.d("test", "broadcast thread terminated");
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
		});
		broadcastThread.start();
		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				waitTCPconnection();
			}
		}).start();
	}

	public void sendStartSignal() {
		// cause the exception to terminate the broadcast thread
		try {
			broadServer.close();
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("test", String.valueOf(client.size()) + " clients");

	}

	private void waitTCPconnection() {
		// waiting socket connection
		try {
			while (true) {
				Log.d("test", "start wait tcp connection");
				final Socket cliSocket = server.accept();
				synchronized (server) {
					client.add(cliSocket);
					sendData("name:"+nickname);
					Log.d("test", "client add");
				}

				new Thread(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						try {
							BufferedInputStream in = new BufferedInputStream(
									cliSocket.getInputStream());
							byte[] b = new byte[1024];
							String data;
							int length;
							Pattern pattern = Pattern.compile("name:(.*)");
							while ((length = in.read(b)) > 0)// <=0的話就是結束了
							{
								data = new String(b, 0, length);
								Matcher matcher = pattern.matcher(data);
								if(matcher.matches()) {
									Message message = new Message();
									Bundle bundle = new Bundle();
									bundle.putString("name", matcher.group(1));
									message.setData(bundle);
									mainHandler.sendMessage(message);
								}
								Log.d("test", "Data:" + data);
							}

						}catch(IOException e) {
							Log.d("test","Terminate server tcp connection thread");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (Exception e) {
			Log.d("test", "waittcp thread terminated");
		}
	}

	public void sendData(String data) {
		try {
			synchronized (client) {
				BufferedOutputStream out = new BufferedOutputStream(client.get(
						0).getOutputStream());
				out.write(data.getBytes());
				out.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopServer() {
		try {
			client.get(0).close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
