import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by Yorkson on 2017/4/7.
 */
public class SimpleServer {
    private static final Logger logger = Logger.getLogger("SingleFileHTTPServer");

    private final byte[] content;
    private final byte[] header;
    private final int port;
    private final String encoding;

    public SimpleServer(String data,String encoding,String mimeType,int port) throws UnsupportedEncodingException {
        this(data.getBytes(encoding),encoding,mimeType,port);
    }
    public SimpleServer(byte[] data,String encoding,String mimeType,int port){
        this.content = data;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.1 200 OK\r\n" +
                "Server: OneFile 1.0\r\n" +
                "Content-length: "+this.content.length+"\r\n" +
                "Content-type" + mimeType +"; charset=" +encoding+ "\r\n" +
                "\r\n";
        this.header = header.getBytes(Charset.forName("US-ASCII"));
    }
    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try(ServerSocket server = new ServerSocket(this.port)){
            logger.info("Accepting connection on port "+server.getLocalPort());
            logger.info("Data to be sent:");
            logger.info(new String(this.content,encoding));

            while (true) {
                Socket connection = server.accept();
                pool.submit(new HTTPHandler(connection));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class HTTPHandler implements Callable<Void> {
        private final Socket connection;

        public HTTPHandler(Socket connection) {
            this.connection = connection;
        }
        @Override
        public Void call() throws IOException {
            try {
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());

                StringBuilder request = new StringBuilder(80);
                while (true){
                    int c = in.read();
                    if (c == '\r' || c == '\n' || c == -1) break;
                    request.append((char) c);
                }
                if (request.toString().indexOf("HTTP/") != -1){
                    out.write(header);
                }
                out.write(content);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                connection.close();
            }
            return null;
        }
    }
    public static void main(String[] args){
        try {
            Path path = Paths.get("index.html");
            byte[] data = Files.readAllBytes(path);
            String contentType = URLConnection.getFileNameMap().getContentTypeFor("index.html");
            SimpleServer server = new SimpleServer(data,"utf-8",contentType,80);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
