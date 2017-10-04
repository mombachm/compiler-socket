// Autor: Matheus Mombach, Gabriel C., José Augusto Acorsi

package classes;

import java.io.*;
import java.net.Socket;

public class Client {

    public final static String FILE_OUTPUT = "received-file.txt";
    public final static int TAM_ARQ = 5000000;

    public static void main(String[] args) throws IOException {
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        Socket socket = null;
        try {
            //instancia o socket do cliente
            socket = new Socket("127.0.0.1", 13267);
            System.out.println("Connecting...");

            //chama função para receber o arquivo do servidor. O path do arquivo é inserido pelo cliente
            receiveFile(socket, fileOutputStream, bufferedOutputStream);

        } finally {
            //finaliza streams e socket
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void receiveFile(Socket socket, FileOutputStream fileOutputStream,
                                    BufferedOutputStream bufferedOutputStream) throws IOException {
        //Instancia um byteArray setando um tamanho máximo de leitura
        byte[] byteArray = new byte[TAM_ARQ];

        BufferedReader doUsuario = new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream paraServidor = new DataOutputStream(socket.getOutputStream());
        BufferedReader doServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Aguarda o cliente digitar o arquivo desejado para download
        System.out.print("Enter the path of the file which you want to receive: ");
        //le a inputStream do cliente
        String fileToReceive = doUsuario.readLine();
        //envia o path do arquivo para o servidor
        paraServidor.writeBytes(fileToReceive + '\n');

        //Aguarda o servidor responder com o nome do arquivo passado para iniciar a leitura do arquivo
        //Obs: O servidor responderá com o path do arquivo se o mesmo foi recebido corretamente
        String serverResponse = "";
        do {
            serverResponse = doServidor.readLine();
        }while(!serverResponse.equals(fileToReceive));

        //Utiliza o inputStream do socket para ler de forma bufferizada
        InputStream inputStream = socket.getInputStream();
        //instancia uma OutputStream para passar os dados do buffer para o arquivo
        fileOutputStream = new FileOutputStream(FILE_OUTPUT);
        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        try {
            //lê do socket a quantidade de bytes definida no tamanho do byte array
            int bytesRead = inputStream.read(byteArray, 0, byteArray.length);
            int auxBytes = bytesRead;

            //lê a inputStream até que que não a nada mais a ser lido no socket (bytesRead = -1)
            //bytesRead guarda o próximo byte a ser lido e o auxBytes guarda o byte atual
            while (bytesRead > -1) {
                bytesRead = inputStream.read(byteArray, auxBytes, (byteArray.length - auxBytes));
                if (bytesRead >= 0) {
                    auxBytes += bytesRead;
                }
            }

            //escreve os bytes no buffer de saída para o arquivo 'received-file.txt'
            bufferedOutputStream.write(byteArray, 0, auxBytes);
            bufferedOutputStream.flush();
            System.out.println("File " + FILE_OUTPUT + " downloaded");

        }catch (Exception e) {
            System.out.print("Error to download the file.");
        }
    }
}
