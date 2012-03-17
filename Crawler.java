import java.net;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.omg.DynamicAny.NameValuePair;

public class RetrivePage{
    private static HttpClient HttpClient = new HttpClient();
    //设置代理服务器
    public static boolean downloadPage(String path) throws HttpException, IOException{
	InputStream input = null;
	OutputStream output = null;
	PostMethod postMethod = new PostMethod(path);
	//设置post方法参数
	NameValuePair[] postData = new NameValuePair[2];
	
    }
}


public static void main(String[] args){
    //抓取网页,输出
    try{
	RetrivePage
    }
}
