
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yorkson on 2017/4/8.
 */
public class HttpServer {
    private static final Logger logger = Logger.getLogger(HttpServer.class.getCanonicalName());
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";

    private final File rootDirectory;
    private final int port;

    public HttpServer(File rootDirectory,int port)throws IOException{
        if (!rootDirectory.isDirectory()){
            throw new IOException(rootDirectory+"dose not exit as a directory");
        }
        this.rootDirectory = rootDirectory;
        this.port = port;
    }
    private void start(){
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("Accepting connections on port "+serverSocket.getLocalPort());
            logger.info("Document root: "+rootDirectory);

            while (true){
                try {
                    Socket request = serverSocket.accept();
                    Runnable r = new RequestProcessor(rootDirectory,INDEX_FILE,request);
                    pool.submit(r);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.log(Level.WARNING,"Error accepting connection",e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
