
public class Client {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ClientOperations clientOperations = new ClientOperations(Integer.parseInt(args[0]));
	}

}
