package chatProj;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainServer extends Application{

	// ExecutorService�� ���� ���� �����带 ȿ�������� �����ϱ� ���� ���̺귯��
	// Thread Pool�� �����带 ó���ϰ� �Ǹ� �⺻���� ������ ������ ������ �α� ������ 
	// ���۽��� Client �������� �������� ������ ������ �־� ������ ���� ���ϸ� ������ �� �ִ�. 
	// ������ �ڿ��� '����������' �����ϱ� ���� ���å
	public static ExecutorService threadPool;
	
	public static LinkedList<IoMethod> clients = new LinkedList<IoMethod>();
	
	ServerSocket serverSocket;
	
	// ���� ���� ���� -> accept(Ŭ���̾�Ʈ ���� ���)
	public void startServer() {
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 5001));
			System.out.println("���ε��Ϸ�");
		} catch (IOException e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					Socket socket;
					try {
						socket = serverSocket.accept();
						clients.add(new IoMethod(socket));
						System.out.println("[�޽��� ���� ����] " 
						+ socket.getRemoteSocketAddress() 
						+ ": " + Thread.currentThread().getName());
					} catch (IOException e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
					
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	// ���� ����
	public void stopServer() {
		try {
			// ���� �۵� ���� ��� ���� �ݱ�
			Iterator<IoMethod> iterator = clients.iterator();
			while(iterator.hasNext()) {
					IoMethod client = iterator.next();
					client.socket.close();
					iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			// ������ Ǯ �����ϱ�
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
				System.out.println("���� ����");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// javafx ui
	@Override
	public void start(Stage arg0) throws Exception {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		InetAddress IP = InetAddress.getLocalHost();
		System.out.println(IP);
		int port = 5001;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);
		arg0.setTitle("[ ä�� ���� ]");
		arg0.setOnCloseRequest(event -> stopServer());
		arg0.setScene(scene);
		arg0.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
