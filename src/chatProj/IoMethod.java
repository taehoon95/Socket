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

	// Ŭ���̾�Ʈ�κ��� �޽����� �޴� �޼ҵ�
	public void receive() {
		Runnable thread = new Runnable() {

			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						if (length == -1)
							throw new IOException();
						System.out.println("[�޽��� ���� ����] " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName());

						String message = new String(buffer, 0, length, "UTF-8");
						for (IoMethod clientServer : MainServer.clients) {
							clientServer.send(message);
						}
					}
				} catch (IOException e) {
					try {
						System.out.println("[�޽��� ���� ����] " + socket.getRemoteSocketAddress() + ": "
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

	
	// Ŭ���̾�Ʈ�κ��� �޽����� �����ϴ� �޼ҵ�
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
						System.out.println("[�޽��� �۽� ����] " 
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
