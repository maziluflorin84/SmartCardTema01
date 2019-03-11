import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket {
	private String connectionTo = null;
	private int connectionPort;
	private Socket clientSocket = null;
	
	public ClientSocket(String connectionTo, int connectionPort) {
		this.setConnectionTo(connectionTo);
		this.setConnectionPort(connectionPort);
		
		try {
			clientSocket = new Socket("localhost", connectionPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Connection to " + this.connectionTo + " Established");
	}

	public String getConnectionTo() {
		return connectionTo;
	}

	public void setConnectionTo(String connectionTo) {
		this.connectionTo = connectionTo;
	}

	public int getConnectionPort() {
		return connectionPort;
	}

	public void setConnectionPort(int connectionPort) {
		this.connectionPort = connectionPort;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
}
