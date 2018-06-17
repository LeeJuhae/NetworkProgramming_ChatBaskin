import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PlayBaskinImpl extends UnicastRemoteObject implements PlayBaskin{

	private static final long serialVersionUID = 1L;
	public PlayBaskinImpl() throws RemoteException{
		super();
	}
	public int isValidate(int rightNum, String inputLine)throws RemoteException{
		if(inputLine.split(" ").length >3)
			return -1;
		for(int i = 0 ; i < inputLine.length() ; i++){
			System.out.println(inputLine.charAt(i));
			if((inputLine.charAt(i)>'9' || inputLine.charAt(i)< '0')&& (inputLine.charAt(i) != ' '))
				return -1;
		}
		for(int i = 0 ; i < inputLine.split(" ").length ; i++){
			if(rightNum == Integer.parseInt(inputLine.split(" ")[i])){
				if(rightNum > 31)
					return -1;
				else rightNum++;
			} else
				return -1;
		}
		return rightNum;
	}
}