import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Created by Yorkson on 2017/4/9.
 */
public class RequestProcessor implements Runnable{
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private File rootDirectory;
    private String indexFileName = "index.html";
    private Socket connection;


    public RequestProcessor(File rootDirectory, String indexFile, Socket request) {
        if (rootDirectory.isFile()){
            throw new IllegalArgumentException(
                    "rootDirectory must be a directory, not a file"
            );
        }
        try {
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.rootDirectory = rootDirectory;
        if (indexFileName != null) this.indexFileName = indexFileName;
        this.connection = request;
    }

    @Override
    public void run() {
        String root = rootDirectory.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in  = new InputStreamReader(new BufferedInputStream(connection.getInputStream()),"ASCII");
            StringBuffer request=new StringBuffer(80);
            while(true) {
                int c = in.read();
                if (c == '\r' || c == '\n' || c == -1) {
                    break;
                }
                request.append((char)c);
            }
            String get=request.toString();
            logger.info(connection.getRemoteSocketAddress()+" "+get);
            StringTokenizer st = new StringTokenizer(get);
            String method=st.nextToken();
            String version = "";
            if (method.equals("GET")){
                String fileName = st.nextToken();
                if (fileName.endsWith("/")) fileName += indexFileName;
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                if(st.hasMoreTokens()){
                    version=st.nextToken();
                }
                File theFile = new File(rootDirectory,fileName.substring(1,fileName.length()));
                if (!theFile.exists()){

                }
                else if(theFile.canRead()&&theFile.getCanonicalPath().startsWith(root)){//检测所请求文件是否超出根目录
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")){
                        sendHeader(out,"HTTP/1.1 200 OK",contentType,theData.length);
                    }
                    raw.write(theData);
                    raw.flush();
                }
            }
            else if (method.equals("POST")){

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendHeader(Writer out, String responseCode, String )

    private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
        out.write(responseCode+"\r\n");
        Date now = new Date();
        out.write("Date:"+now+"\r\n");
        out.write("Server:JHTTP 1.0\r\n");
        out.write("Content-length:"+length+"\r\n");
        out.write("Content-Type:"+contentType+"\r\n\r\n");
        out.flush();
    }
}
