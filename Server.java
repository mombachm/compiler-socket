// Autor: Matheus Mombach, Gabriel C., José Augusto Acorsi

package classes;

        import org.omg.SendingContext.RunTime;

        import java.io.*;
        import java.net.ServerSocket;
        import java.net.Socket;

public class Server {

    public final static int SOCKET_PORT = 13267;

    public final static String OUTPUT_FILE = "a.out";

    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream outputStream = null;
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            //instancia o socket do servidor
            serverSocket = new ServerSocket(SOCKET_PORT);
            while (true) {
                System.out.println("Waiting connection...");
                try {
                    //aguarda um cliente se conectar
                    socket = serverSocket.accept();
                    System.out.println("Accepted connection : " + socket);

                    //instancia o buffer e o inputStream para ler do cliente a partir do socket conectado
                    BufferedReader fromCliente = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //instancia o DataOutputStream para escrever no socket conectado e enviar o buffer para o cliente
                    DataOutputStream toCliente = new DataOutputStream(socket.getOutputStream());

                    //aguarda o cliente digitar o nome do arquivo a ser enviado
                    String fileToSend = "";
                    do {
                        fileToSend = fromCliente.readLine();
                    }while(fileToSend.isEmpty());

                    //ao receber o nome do arquivo, o servidor envia o nome de volta para confirmar a recepção
                    toCliente.writeBytes(fileToSend + '\n');

                    // Get runtime
                    Runtime rt = Runtime.getRuntime();
                    // Start a new process: UNIX command ls
                    Process p = rt.exec("gcc " + fileToSend);
                    // Show exit code of process
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Compile error!");
                    }
                    System.out.println("Process exited with code = " + p.exitValue());
                    if(p.exitValue() == 0)
                        System.out.println("File compiled with succes.");



                    //chamada da função de envio do arquivo passando por parâmetro o path do arquivo
                    System.out.println("C File to send: " + OUTPUT_FILE);
                    try {
                        sendFile(fileInputStream, bufferedInputStream, outputStream, socket, OUTPUT_FILE);
                    }catch (IOException e) {
                        System.out.print(e.getMessage());
                    }
                } finally {
                    //finaliza os streams e o socket
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    private static void sendFile(FileInputStream fileInputStream, BufferedInputStream buffereInputStream,
                                 OutputStream outputStream, Socket socket, String fileToSend) throws IOException {
        //abre o arquivo a ser enviado
        File file = new File(fileToSend);
        //testa se o arquivo existe, se não, dispara uma exception
        if(!file.exists()) {
            throw new IOException("File not found.\n");
        }
        //instancia um byte array com o tamanho do arquivo
        byte[] fileOnBytes = new byte[(int) file.length()];
        //isntancia um FileInputStream para ler do arquivo
        fileInputStream = new FileInputStream(file);
        buffereInputStream = new BufferedInputStream(fileInputStream);
        //lê o arquivo e armazena no byte array
        buffereInputStream.read(fileOnBytes, 0, fileOnBytes.length);
        //utiliza a OutputStream do socket para enviar o byte array pelo socket
        outputStream = socket.getOutputStream();
        System.out.println("Sending " + fileToSend + "...");
        //Escreve o byte array com o conteúdo do arquivo no output stream
        outputStream.write(fileOnBytes, 0, fileOnBytes.length);
        outputStream.flush();
        System.out.println("Done.");
    }
}
