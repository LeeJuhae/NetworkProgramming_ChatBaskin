//import java.io.*;
//import java.net.*;
//import java.util.Scanner;
//
//import javax.net.ssl.SSLSocket;
//import javax.net.ssl.SSLSocketFactory;
//
//public class baskinClient {
//	
//	static String eServer = "";
//	static int ePort = 0000;
//	static String eServName = "";
//	
//	//static Socket chatSocket = null;
//	static SSLSocket chatSocket = null;
//	static SSLSocketFactory f = null;
//	
//	public static void main(String[] args) {
//
//		if (args.length != 3) {
//			System.out.println("Usage: Classname ServerName ServiceName ServerPort");
//			System.exit(1);
//		}
//		
//		eServer = args[0];
//		eServName = args[1];
//		ePort = Integer.parseInt(args[2]);
//		
//		try {
//			System.setProperty("javax.net.ssl.trustStore", "C://Users//samsung//Desktop//네트워크 프로그래밍//07-EX//07주차//bin//trustedcerts");
//			System.setProperty("javax.net.ssl.trustPassword","123456");
//			//chatSocket = new SSLSocket(eServer, ePort);
//			f = (SSLSocketFactory)SSLSocketFactory.getDefault();
//			chatSocket = (SSLSocket)f.createSocket(eServer, ePort);
//			String[] supported  = chatSocket.getSupportedCipherSuites();
//			chatSocket.setEnabledCipherSuites(supported);
//			chatSocket.startHandshake();
//			
//		} catch (BindException b) {
//			System.out.println("Can't bind on: "+ePort);
//			System.exit(1);
//		} catch (IOException i) {
//			System.out.println(i);
//			System.exit(1);
//		}
//		new Thread(new ClientReceiver(chatSocket)).start();
//		new Thread(new ClientSender(chatSocket)).start();
//	}
//}
//
//class ClientSender implements Runnable {
//	private SSLSocket chatSocket = null;
//	baskinClient c = new baskinClient();
//	ClientSender(SSLSocket socket){
//		this.chatSocket = socket;
//	}
//
//	public void run(){
//		Scanner KeyIn = null;
//		PrintWriter out = null;
//		try{
//			KeyIn = new Scanner(System.in);
//			out = new PrintWriter(chatSocket.getOutputStream(),true);
//			
//			String userInput = "";
//			System.out.println("Your are "+chatSocket.getLocalPort()+", Type Message (\"Bye.\" to leave)\n");
//			while((userInput=KeyIn.nextLine())!= null){
//				out.println(userInput);
//				out.flush();
//				if(userInput.equalsIgnoreCase("Bye."))
//					break;
//			}
//			KeyIn.close();
//			out.close();
//			chatSocket.close();
//		}catch (IOException i){
//			try{
//				if(out != null) out.close();
//				if(KeyIn != null)KeyIn.close();
//				if(chatSocket != null) chatSocket.close();
//			} catch(IOException e){
//			}
//			System.exit(1);
//		}
//	}
//}
//class ClientReceiver implements Runnable{
//	private SSLSocket chatSocket = null;
//	
//	ClientReceiver(SSLSocket socket){
//		this.chatSocket = socket;
//	}
//	public void run(){
//		while(chatSocket.isConnected()){
//			BufferedReader in = null;
//			try{
//				in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
//				String readSome = null;
//				while((readSome = in.readLine())!= null){
//					System.out.println(readSome);
//				}
//				in.close();
//				chatSocket.close();
//			} catch(IOException i){
//				try{
//					if (in != null) 
//						in.close();
//					if(chatSocket != null) chatSocket.close();
//				}catch(IOException e){
//				}
//				System.out.println("Leave.");
//				System.exit(1);
//			}
//		}
//	}
//}
import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class baskinClient {
	
	static String eServer = "";
	static int ePort = 0000;
	static String eServName = "";
	
	static SSLSocket chatSocket = null;
	//static SSLServerSocket
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Usage: Classname ServerName ServiceName ServerPort");
			System.exit(1);
		}
		
		eServer = args[0];
		eServName = args[1];
		ePort = Integer.parseInt(args[2]);
		
		try {
			System.setProperty("javax.net.ssl.trustStore", "C://Users//samsung//Desktop//네트워크 프로그래밍//07-EX//07주차//bin//trustedcerts");
			System.setProperty("javax.net.ssl.trustPassword","123456");
			SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
			//server = (SSLServerSocket)
			//chatSocket = (SSLSocket)new Socket(eServer, ePort);
			chatSocket = (SSLSocket)factory.createSocket(eServer, ePort);
			
		} catch (BindException b) {
			System.out.println("Can't bind on: "+ePort);
			System.exit(1);
		} catch (IOException i) {
			System.out.println(i);
			System.exit(1);
		}
		new Thread(new ClientReceiver(chatSocket)).start();
		new Thread(new ClientSender(chatSocket)).start();
	}
}

class ClientSender implements Runnable {
	private SSLSocket chatSocket = null;
	baskinClient c = new baskinClient();
	ClientSender(SSLSocket socket){
		this.chatSocket = socket;
	}

	public void run(){
		Scanner KeyIn = null;
		PrintWriter out = null;
		try{
			KeyIn = new Scanner(System.in);
			out = new PrintWriter(chatSocket.getOutputStream(),true);
			
			String userInput = "";
			System.out.println("Your are "+chatSocket.getLocalPort()+", Type Message (\"Bye.\" to leave)\n");
			while((userInput=KeyIn.nextLine())!= null){
				out.println(userInput);
				out.flush();
				if(userInput.equalsIgnoreCase("Bye."))
					break;
			}
			KeyIn.close();
			out.close();
			chatSocket.close();
		}catch (IOException i){
			try{
				if(out != null) out.close();
				if(KeyIn != null)KeyIn.close();
				if(chatSocket != null) chatSocket.close();
			} catch(IOException e){
			}
			System.exit(1);
		}
	}
}
class ClientReceiver implements Runnable{
	private SSLSocket chatSocket = null;
	
	ClientReceiver(SSLSocket socket){
		this.chatSocket = socket;
	}
	public void run(){
		while(chatSocket.isConnected()){
			BufferedReader in = null;
			try{
				in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
				String readSome = null;
				while((readSome = in.readLine())!= null){
					System.out.println(readSome);
				}
				in.close();
				chatSocket.close();
			} catch(IOException i){
				try{
					if (in != null) 
						in.close();
					if(chatSocket != null) chatSocket.close();
				}catch(IOException e){
				}
				System.out.println("Leave.");
				System.exit(1);
			}
		}
	}
}