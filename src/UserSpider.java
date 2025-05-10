import java.io.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;


public class UserSpider {
    public static final File OUTPUT=new File("output.txt");
    private static final int MAX_THREADS=100;
    private static final String URL_SITE ="https://www.shireyishunjian.com/main/home.php?mod=space&uid=%uid&do=profile&mobile=2";
    private static final int NUM_PER_THREAD=20;
    private static final int MAX_RETRY=5;
    private static PrintWriter out;

    private int runningThreads=0;

    private final AtomicLong finish=new AtomicLong(0);
    private final AtomicLong found=new AtomicLong(0);
    private final AtomicLong fail=new AtomicLong(0);

    private final long start;
    private final long end;
    private final long total;

    private UserAnalyser analyser;
    private HttpClient client;
    private CookieManager cookieManager;

    public UserSpider(long start, long end) {
        this.start = start;
        this.end = end;
        total=end-start+1;
        init();
    }

    public long getTotal() {
        return total;
    }

    public long getFound(){
        return found.get();
    }

    public long getFail(){
        return fail.get();
    }

    public long getFinish(){
        return finish.get();
    }

    public void setCookie(String name,String value){
        HttpCookie cookie=new HttpCookie(name,value);
        cookie.setDomain("www.shireyishunjian.com");
        cookie.setPath("/");
        cookieManager.getCookieStore().add(URI.create("https://www.shireyishunjian.com"),cookie);
    }

    public UserAnalyser getAnalyser() {
        return analyser;
    }

    public void setAnalyser(UserAnalyser analyser) {
        this.analyser = analyser;
    }


    public void startAndWait(){
        long idNow=start;
        try {
            while (idNow<end){
                long end=idNow+NUM_PER_THREAD;
                while (runningThreads>=MAX_THREADS)Thread.sleep(50);
                startThread(idNow, Math.min(end, this.end));
                runningThreads++;
                idNow=end;
            }
            while (runningThreads>0)Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startThread(long start,long end){
        Thread.ofVirtual().start(()->{
            long idNow=start;

            try {
                while (idNow<end){
                    idNow++;
                    for (int i = 0; i <MAX_RETRY ; i++) {
                        try {
                            extracted(idNow);
                            break;
                        } catch (Exception e) {
                            if (i==MAX_RETRY-1){
                                fail.getAndAdd(1);
                                System.err.println("uid:"+idNow+","+"失败:"+ e.getMessage());
                            }
                        }
                    }
                    finish.getAndAdd(1);
                }
            }finally {
                runningThreads--;
            }
        });
    }

    private void extracted(long idNow) throws Exception{
        boolean isFound;
        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create(URL_SITE.replace("%uid",String.valueOf(idNow))))
                .GET()
                .build();
        HttpResponse<String> response=client.send(downloadRequest,HttpResponse.BodyHandlers.ofString());
        String html=response.body();

        String title=HTMLUtils.getTitle(html);
        if (title==null) isFound=false;
        else isFound=!title.contains("提示信息 -  湿热一瞬间 - 湿热一瞬间");

        if (isFound){
            String userName=HTMLUtils.getUserName(html);
            String userGroup=HTMLUtils.getUserGroup(html);
            String regTime=HTMLUtils.getRegTime(html);
            String actTime =HTMLUtils.getActTime(html);

            if (analyser!=null)analyser.addUser(userGroup,regTime,actTime);
            found.getAndAdd(1);
            out.println(idNow +","+userName+","+userGroup+","+regTime+","+ actTime +";");
        }
    }

    private void init(){
        try {
            out=new PrintWriter(new FileOutputStream(OUTPUT,true),true, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
}
