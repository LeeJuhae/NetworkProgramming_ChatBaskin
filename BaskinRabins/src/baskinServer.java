//import java.net.*;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLException;
//import javax.net.ssl.SSLServerSocket;
//import javax.net.ssl.SSLServerSocketFactory;
//import javax.net.ssl.SSLSocket;
//
//import java.io.*; 
//
//public class baskinServer implements Runnable {
//
//	private baskinServerRunnable clients[] = new baskinServerRunnable[3];
//	public int clientCount = 0;
//	private int ePort = -1;
//	int baskinCount = 1;
//	
//	static String server1;
//	static String servname1;
//	
//	public baskinServer(int port) {
//		this.ePort = port;
//	}
//	public baskinServer(){
//		try{
//			PlayBaskin pb = new PlayBaskinImpl();
//			Naming.rebind("rmi://"+server1+":1099/"+servname1,pb);
//		}catch(Exception e){
//			System.out.println("Trouble : "+e);
//		}
//	}
//
//	public void run() {
//		SSLServerSocket serverSocket = null;
//		try {
//			serverSocket = (SSLServerSocket)new ServerSocket(ePort);
//			System.out.println ("Server started: socket created on " + ePort);
//			while (true) {
//				addClient(serverSocket);
//			}
//		} catch (BindException b) {
//			System.out.println("Can't bind on: "+ePort);
//		} catch (IOException i) {
//			System.out.println(i);
//		} finally {
//			try {
//				if (serverSocket != null) serverSocket.close();
//			} catch (IOException i) {
//				System.out.println(i);
//			}
//		}
//	}
//	
//	public int whoClient(int clientID) {
//		for (int i = 0; i < clientCount; i++)
//			if (clients[i].getClientID() == clientID)
//				return i;
//		return -1;
//	}
//	public void putClient(int clientID, String inputLine) {
//		try{
//		String testSentence;
//		PlayBaskin pb = (PlayBaskin)Naming.lookup("rmi://"+server1+"/"+servname1);
//		testSentence = inputLine.split(":")[1];
//		if(pb.isValidate(baskinCount, testSentence) != -1){
//			for (int i = 0; i < clientCount; i++)
//				if (clients[i].getClientID() == clientID) {
//					System.out.println("writer: "+clientID);
//				} else {
//					System.out.println("write: "+clients[i].getClientID());
//					clients[i].out.println(inputLine);
//				}
//			baskinCount = pb.isValidate(baskinCount, testSentence);
//		}
//		else{
//			for (int i = 0; i < clientCount; i++)
//				if (clients[i].getClientID() == clientID) {
//					clients[i].out.println("다시 입력하세요");
//				}
//		}
//		System.out.println(baskinCount);
//		}catch(MalformedURLException | RemoteException | NotBoundException mue){
//			System.out.println(mue);
//		}
//	}
//	public void addClient(SSLServerSocket serverSocket) {
//		SSLSocket clientSocket = null;
//		if (clientCount < clients.length) { 
//			try {
//				clientSocket = (SSLSocket)serverSocket.accept();
//				clientSocket.setSoTimeout(40000); // 1000/sec
//			} catch (IOException i) {
//				System.out.println ("Accept() fail: "+i);
//			}
//			clients[clientCount] = new baskinServerRunnable(this, clientSocket);
//			new Thread(clients[clientCount]).start();
//			clientCount++;
//			System.out.println ("Client connected: " + clientSocket.getPort()
//					+", CurrentClient: " + clientCount);
//		} else {
//			try {
//				SSLSocket dummySocket = (SSLSocket)serverSocket.accept();
//				baskinServerRunnable dummyRunnable = new baskinServerRunnable(this, dummySocket);
//				new Thread(dummyRunnable);
//				dummyRunnable.out.println(dummySocket.getPort()
//						+ " < Sorry maximum user connected now");
//				System.out.println("Client refused: maximum connection "
//						+ clients.length + " reached.");
//				dummyRunnable.close();
//			} catch (IOException i) {
//				System.out.println(i);
//			}	
//		}
//	}
//	public synchronized void delClient(int clientID) {
//		int pos = whoClient(clientID);
//		baskinServerRunnable endClient = null;
//	      if (pos >= 0) {
//	    	   endClient = clients[pos];
//	    	  if (pos < clientCount-1)
//	    		  for (int i = pos+1; i < clientCount; i++)
//	    			  clients[i-1] = clients[i];
//	    	  clientCount--;
//	    	  System.out.println("Client removed: " + clientID
//	    			  + " at clients[" + pos +"], CurrentClient: " + clientCount);
//	    	  endClient.close();
//	      }
//	}
//	
//	public static void main(String[] args) throws IOException {
//		if (args.length != 3) {
//			System.out.println("Usage: Classname ServerName ServName ServerPort ");
//			System.exit(1);
//		}
//		server1 = args[0];
//		servname1 = args[1];
//		int ePort = Integer.parseInt(args[2]);
//		
//		final KeyStore ks;
//		final KeyManagerFactory kmf;
//		final SSLContext sc;
//		
//		final String runRoot = "C://Users//samsung//workspace//BaskinRabins//bin//";
//		SSLServerSocketFactory ssf = null;
//		SSLServerSocket s = null;
//		SSLSocket c = null;
//		
//		String ksName = runRoot +".keystore/SSLSocketServerKey";
//		
//		char keyStorePass[] = "123456".toCharArray();
//		char keyPass[] = "123456".toCharArray();
//		
//		try {
//			ks = KeyStore.getInstance("JKS");
//			ks.load(new FileInputStream(ksName), keyStorePass);
//			
//			kmf= KeyManagerFactory.getInstance("SunX509");
//			kmf.init(ks,keyPass);
//			
//			sc = SSLContext.getInstance("TLS");
//			sc.init(kmf.getKeyManagers(), null,null);
//			
//			ssf = sc.getServerSocketFactory();
//			s = (SSLServerSocket)ssf.createServerSocket(ePort);
//			//printServerSocketInfo(s);
//			c = (SSLSocket)s.accept();
//		}  catch (SSLException se) {
//			System.out.println("SSL problem, exit~");
//		} catch (Exception e) {
//			System.out.println("What?? exit~");
//		}
//		new baskinServer();
//		new Thread(new baskinServer(ePort)).start();
//	}
//}
//
//class baskinServerRunnable implements Runnable {
//	protected baskinServer chatServer = null;
//	protected Socket clientSocket = null;
//	protected PrintWriter out = null;
//	protected BufferedReader in = null;
//	public int clientID = -1;
//	
//	public baskinServerRunnable (baskinServer server, Socket socket){
//		this.chatServer = server;
//		this.clientSocket = socket;
//		clientID = clientSocket.getPort();
//		try{
//			out = new PrintWriter(clientSocket.getOutputStream(),true);
//			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//		}catch(IOException i){
//		}
//	}
//	public void run(){
//		try{
//			String inputLine;
//			while((inputLine = in.readLine())!= null){
//				chatServer.putClient(getClientID(), getClientID()+":"+inputLine);
//				if(inputLine.equalsIgnoreCase("Bye."))
//					break;
//			}
//			chatServer.delClient(getClientID());
//		}catch(SocketTimeoutException ste){
//			System.out.println("Socket timeout Occurred, force close() : "+getClientID());
//			chatServer.delClient(getClientID());
//		}catch(IOException e){
//			chatServer.delClient(getClientID());
//		}
//	}
//	public int getClientID(){
//		return clientID;
//	}
//	public void close(){
//		try{
//			if(in != null) in.close();
//			if(out != null) out.close();
//			if(clientSocket != null)clientSocket.close();
//		}catch(IOException i){
//		}
//	}
//}
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.*; 

public class baskinServer implements Runnable {

	private baskinServerRunnable clients[] = new baskinServerRunnable[3];
	public int clientCount = 0;
	private int ePort = -1;
	int baskinCount = 1;
	
	static String server1;
	static String servname1;
	
	public baskinServer(int port) {
		this.ePort = port;
	}
	public baskinServer(){
		try{
			PlayBaskin pb = new PlayBaskinImpl();
			Naming.rebind("rmi://"+server1+":1099/"+servname1,pb);
		}catch(Exception e){
			System.out.println("Trouble : "+e);
		}
	}

	public void run() {
		SSLServerSocket serverSocket = null;
	
		try {
			SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			serverSocket = (SSLServerSocket)factory.createServerSocket(ePort);
			//serverSocket = (SSLServerSocket)new ServerSocket(ePort);
			//SSLSocket acceptedSocket = (SSLSocket)serverSocket.accept();
			System.out.println ("Server started: socket created on " + ePort);
			
			while (true) {
				addClient(serverSocket);
			}
		} catch (BindException b) {
			System.out.println("Can't bind on: "+ePort);
			
		} catch (IOException i) {
			System.out.println(i);
		} finally {
			try {
				if (serverSocket != null) serverSocket.close();
			} catch (IOException i) {
				System.out.println(i);
			}
		}
	}
	
	public int whoClient(int clientID) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getClientID() == clientID)
				return i;
		return -1;
	}
	public void putClient(int clientID, String inputLine) {
		try{
		String testSentence;
		PlayBaskin pb = (PlayBaskin)Naming.lookup("rmi://"+server1+"/"+servname1);
		testSentence = inputLine.split(":")[1];
			if(pb.isValidate(baskinCount, testSentence) != -1){
				for (int i = 0; i < clientCount; i++)
					if (clients[i].getClientID() == clientID) {
						System.out.println("writer: "+clientID);
					} else {
						System.out.println("write: "+clients[i].getClientID());
						clients[i].out.println(inputLine);
					}
				baskinCount = pb.isValidate(baskinCount, testSentence);
			}
			else{
				for (int i = 0; i < clientCount; i++)
					if (clients[i].getClientID() == clientID) {
						clients[i].out.println("다시 입력하세요");
					}
			}
			System.out.println(baskinCount);
				
		}catch(MalformedURLException | RemoteException | NotBoundException mue){
			System.out.println(mue);
		}
	}
	public void addClient(SSLServerSocket serverSocket) {
		SSLSocket clientSocket = null;
		
		if (clientCount < clients.length) { 
			try {
				clientSocket = (SSLSocket)serverSocket.accept();
				clientSocket.setSoTimeout(40000); // 1000/sec
				
			} catch (IOException i) {
				System.out.println ("Accept() fail: "+i);
			}
			clients[clientCount] = new baskinServerRunnable(this, clientSocket);
			new Thread(clients[clientCount]).start();
			
			clientCount++;
			System.out.println ("Client connected: " + clientSocket.getPort()
					+", CurrentClient: " + clientCount);
		} else {
			try {
				SSLSocket dummySocket = (SSLSocket)serverSocket.accept();
				baskinServerRunnable dummyRunnable = new baskinServerRunnable(this, dummySocket);
				new Thread(dummyRunnable);
				dummyRunnable.out.println(dummySocket.getPort()
						+ " < Sorry maximum user connected now");
				System.out.println("Client refused: maximum connection "
						+ clients.length + " reached.");
				dummyRunnable.close();
			} catch (IOException i) {
				System.out.println(i);
			}	
		}
	}
	public synchronized void delClient(int clientID) {
		int pos = whoClient(clientID);
		baskinServerRunnable endClient = null;
	      if (pos >= 0) {
	    	   endClient = clients[pos];
	    	  if (pos < clientCount-1)
	    		  for (int i = pos+1; i < clientCount; i++)
	    			  clients[i-1] = clients[i];
	    	  clientCount--;
	    	  System.out.println("Client removed: " + clientID
	    			  + " at clients[" + pos +"], CurrentClient: " + clientCount);
	    	  endClient.close();
	      }
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage: Classname ServerName ServName ServerPort ");
			System.exit(1);
		}
		server1 = args[0];
		servname1 = args[1];
		int ePort = Integer.parseInt(args[2]);
		new baskinServer();
		new Thread(new baskinServer(ePort)).start();
	}
}

class baskinServerRunnable implements Runnable {
	protected baskinServer chatServer = null;
	protected SSLSocket clientSocket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;
	public int clientID = -1;
	
	public baskinServerRunnable (baskinServer server, SSLSocket socket){
		this.chatServer = server;
		this.clientSocket = socket;
		clientID = clientSocket.getPort();
		try{
			out = new PrintWriter(clientSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}catch(IOException i){
		}
	}
	public void run(){
		try{
			String inputLine;
			while((inputLine = in.readLine())!= null){
				chatServer.putClient(getClientID(), getClientID()+":"+inputLine);
				if(inputLine.equalsIgnoreCase("Bye."))
					break;
			}
			chatServer.delClient(getClientID());
		}catch(SocketTimeoutException ste){
			System.out.println("Socket timeout Occurred, force close() : "+getClientID());
			chatServer.delClient(getClientID());
		}catch(IOException e){
			chatServer.delClient(getClientID());
		}
	}
	public int getClientID(){
		return clientID;
	}
	public void close(){
		try{
			if(in != null) in.close();
			if(out != null) out.close();
			if(clientSocket != null)clientSocket.close();
		}catch(IOException i){
		}
	}
}