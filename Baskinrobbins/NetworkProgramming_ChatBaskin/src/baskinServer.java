import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
		//추가
		try{
			PlayBaskin pb = new PlayBaskinImpl();
			Naming.rebind("rmi://"+server1+":1099/"+servname1,pb);
		}catch(Exception e){
			System.out.println("Trouble : "+e);
		}
		//여기까지
	}

	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(ePort);
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
		//System.out.println(testSentence);
		//System.out.println(pb.isValidate(baskinCount, inputLine));
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
	public void addClient(ServerSocket serverSocket) {
		Socket clientSocket = null;
		
		if (clientCount < clients.length) { 
			try {
				clientSocket = serverSocket.accept();
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
				Socket dummySocket = serverSocket.accept();
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
//		if (args.length != 1) {
//			System.out.println("Usage: Classname ServerPort");
//			System.exit(1);
//		}
//		int ePort = Integer.parseInt(args[0]);
//		
//		new Thread(new baskinServer(ePort)).start();
		
		
		if (args.length != 3) {
			System.out.println("Usage: Classname ServerName ServName ServerPort ");
			System.exit(1);
		}
//		String mServer = args[0];
//		String mServName = args[1];
		server1 = args[0];
		servname1 = args[1];
		int ePort = Integer.parseInt(args[2]);
		new baskinServer();
		new Thread(new baskinServer(ePort)).start();
	}
}

class baskinServerRunnable implements Runnable {
	protected baskinServer chatServer = null;
	protected Socket clientSocket = null;
	protected PrintWriter out = null;
	protected BufferedReader in = null;
	public int clientID = -1;
	
	public baskinServerRunnable (baskinServer server, Socket socket){
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