import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Which simulation do you want to run?\n 1. Single queue\n 2. Double queue with block model\n 3. Double queue with delete model");
        int SimType = sc.nextInt();
        switch (SimType) {
            case 1:
                new Sim().main();
                sc.close();
                break;
            case 2:
                new BlockSim().main();
                sc.close();
                break;
            case 3:
                new DropSim().main();
                sc.close();
                break;
            default:
                break;
        }
    }
}
