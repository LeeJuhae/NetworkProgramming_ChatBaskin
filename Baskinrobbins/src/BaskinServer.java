import java.net.*;

import java.security.KeyStore;
import java.rmi.*;
import javax.net.ssl.*;
import java.io.*;

public class BaskinServer implements Runnable {

   private BaskinServerRunnable clients[] = new BaskinServerRunnable[3];
   public int clientCount = 0;
   private int ePort = -1;
   int baskinCount = 1;
   int turnCount = 0;

   static String server1;
   static String servname1;

   SSLServerSocketFactory factory;
   SSLServerSocket server;
   
   public BaskinServer(int port) {
      this.ePort = port;
   }

   public BaskinServer() {
      try {
         PlayBaskin pb = new PlayBaskinImpl();
         Naming.rebind("rmi://" + server1 + ":1099/" + servname1, pb);
      } catch (Exception e) {
         System.out.println("Trouble : " + e);
      }
   }

   public void run() {
      try {
         final KeyStore ks;
         final KeyManagerFactory kmf;
         final SSLContext sc;
         final String runRoot = "C://Users//samsung//Desktop//Baskinrobbins//bin//";      
         char keyStorePass[] = "123456".toCharArray();
         char keyPass[]= "123456".toCharArray();
         
         String ksName = runRoot+".keystore/SSLSocketServerKey";
         
         ks = KeyStore.getInstance("JKS");
         ks.load(new FileInputStream(ksName),keyStorePass);
         
         kmf= KeyManagerFactory.getInstance("SunX509");
         kmf.init(ks,keyPass);
         
         sc = SSLContext.getInstance("TLS");
         sc.init(kmf.getKeyManagers(), null,null);
         
         System.out.println("Server started: socket created on " + ePort);
         
         factory = sc.getServerSocketFactory();
         server = (SSLServerSocket)factory.createServerSocket(ePort);

         while (true) {
            addClient(server);
         }
      }catch(SSLException se){
         System.out.println(se);
      }catch(Exception e){
         System.out.println(e);
      }
      finally {
         try {
            if (server != null)
               server.close();
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
         PlayBaskin pb = (PlayBaskin)Naming.lookup("rmi://"+server1+"/"+servname1);
            if(pb.isValidate(baskinCount, inputLine) != -1){
               if(turnCount == clients.length)
                  turnCount = 0;
               String str[] = inputLine.split(" ");
               
               for (int i = 0; i < clientCount; i++){
                  if (clients[i].getClientID() == clientID) {
                     System.out.println("writer: "+clientID);
                     for(int j = 0 ; j < str.length ; j++) {
                        clients[i].out.println(str[j]);
                     }
                     if(str[str.length - 1].equals("31"))
                        clients[i].out.println("You Failed");
                  } else {
                     System.out.println("write: "+clients[i].getClientID());
                     for(int j = 0 ; j < str.length ; j++) {
                        clients[i].out.println(str[j]);
                     }
                     if(str[str.length - 1].equals("31"))
                        clients[i].out.println("Game Finished");
                  }
               }
               baskinCount = pb.isValidate(baskinCount, inputLine);
               if(baskinCount == 32)
                  return;
               clients[turnCount].out.println("Your Turn");
               for(int i = 0 ; i < clientCount ; i++) {
                  clients[i].out.println("Who" + turnCount);
               }
               turnCount++;
            }
            else{
               for (int i = 0; i < clientCount; i++)
                  if (clients[i].getClientID() == clientID) {
                     clients[i].out.println("다시 입력하세요");
                     inputLine ="";
                  }
            }
         }catch(MalformedURLException | RemoteException | NotBoundException mue){
            System.out.println(mue);
         }
   }

   public void addClient(SSLServerSocket serverSocket) {
      SSLSocket clientSocket = null;
      
      if (clientCount < clients.length) {
         try {
            clientSocket = (SSLSocket)serverSocket.accept();
         } catch (IOException i) {
            System.out.println("Accept() fail: " + i);
         }
         clients[clientCount] = new BaskinServerRunnable(this, clientSocket);
         new Thread(clients[clientCount]).start();
         clientCount++;
         System.out.println("Client connected: " + clientSocket.getPort() + ", CurrentClient: " + clientCount);

         for (int i = 0; i < clientCount; i++) {
            clients[i].out.println("Connected" + clientCount);
         }
         
         System.out.println(clientCount);
         if(clientCount == clients.length) {
            for(int i = 0 ; i < clientCount ; i++) {
               clients[i].out.println("Game Start");
            }
            clients[turnCount].out.println("Your Turn");
            for(int i = 0 ; i < clientCount ; i++) {
               clients[i].out.println("Who" + turnCount);
            }
            turnCount++;
         }
      } 
      else {
         try {
            SSLSocket dummySocket = (SSLSocket)serverSocket.accept();
            BaskinServerRunnable dummyRunnable = new BaskinServerRunnable(this, dummySocket);
            new Thread(dummyRunnable);
            dummyRunnable.out.println(dummySocket.getPort() + " < Sorry maximum user connected now");
            System.out.println("Client refused: maximum connection " + clients.length + " reached");
            dummyRunnable.close();
         } catch (IOException i) {
            System.out.println(i);
         }
      }
   }

   public synchronized void delClient(int clientID) {
      int pos = whoClient(clientID);
      BaskinServerRunnable endClient = null;
      if (pos >= 0) {
         endClient = clients[pos];
         if (pos < clientCount - 1)
            for (int i = pos + 1; i < clientCount; i++)
               clients[i - 1] = clients[i];
         clientCount--;
         System.out
               .println("Client removed: " + clientID + " at clients[" + pos + "], CurrentClient: " + clientCount);
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
      new BaskinServer();
      new Thread(new BaskinServer(ePort)).start();
   }
}

class BaskinServerRunnable implements Runnable {
   protected BaskinServer baskinServer = null;
   protected Socket clientSocket = null;
   protected PrintWriter out = null;
   protected BufferedReader in = null;
   public int clientID = -1;

   public BaskinServerRunnable(BaskinServer server, SSLSocket socket) {
      this.baskinServer = server;
      this.clientSocket = socket;
      clientID = clientSocket.getPort();
      try {
         out = new PrintWriter(clientSocket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      } catch (IOException i) {

      }
   }

   public void run() {
      try {
         String inputLine;
         while ((inputLine = in.readLine()) != null) {
            baskinServer.putClient(getClientID(), inputLine);
            if (inputLine.equalsIgnoreCase("Bye."))
               break;
         }
         baskinServer.delClient(getClientID());
      } catch (SocketTimeoutException ste) {
         System.out.println("Socket timeout Occured, force close() :" + getClientID());
         baskinServer.delClient(getClientID());
      } catch (IOException e) {
         baskinServer.delClient(getClientID());
      }
   }

   public int getClientID() {
      return clientID;
   }

   public void close() {
      try {
         if (in != null)
            in.close();
         if (out != null)
            out.close();
         if (clientSocket != null)
            clientSocket.close();
      } catch (IOException i) {

      }
   }
}