package chatProj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IoMethod {
	Socket socket;

	public IoMethod(Socket socket) {
		this.socket = socket;
		receive();
	}

	// 클라이언트로부터 메시지를 받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {

			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						
						if (length == -1) throw new IOException();
						
						System.out.println("[메시지 수신 성공] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());

						String message = new String(buffer, 0, length, "UTF-8");
						for (IoMethod clientServer : MainServer.clients) {
							clientServer.send(message);
						}
					}
				} catch (IOException e) {
					try {
						System.out.println("[메시지 수신 오류] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());
						MainServer.clients.remove(IoMethod.this);
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}// end receive()

	
	// 클라이언트로부터 메시지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (IOException e) {
					try {
						System.out.println("[메시지 송신 오류] " 
								+ socket.getRemoteSocketAddress() 
								+ ": " + Thread.currentThread().getName());
						MainServer.clients.remove(IoMethod.this);
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		MainServer.threadPool.submit(thread);
	}
}
