import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {
        File inputFile=null;
        boolean analysisOnly=false;
        boolean enableAnalyser=true;

        for (int i = 0; i < args.length; i++) {
            String s=args[i];
            if (s.equals("Disable:Analyser"))enableAnalyser=false;
            if (s.equals("-i")) {
                analysisOnly = true;
                inputFile=new File(args[i+1]);
            }
        }

        if (analysisOnly&&inputFile.canRead()){
            UserAnalyser analyser=new UserAnalyser();
            try {
                BufferedReader reader=new BufferedReader(new FileReader(inputFile));

                long err=0;
                long num=0;
                String line;
                while ((line= reader.readLine())!=null){
                    try {
                        line=line.substring(0,line.length()-1);
                        String[] data =line.split(",");
                        analyser.addUser(data[2],data[3],data[4]);
                        num++;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        err++;
                    }
                }

                if (err!=0)System.err.println("数据中有"+err+"个错误");
                analysisData(analyser,num-err);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        System.out.println("数据将输出到 "+UserSpider.OUTPUT.getAbsolutePath());
        Scanner scanner=new Scanner(System.in);
        String auth="";
        String saltKey="";
        try(FileReader fileReader=new FileReader("cookies.txt");
            BufferedReader reader=new BufferedReader(fileReader)) {
            String line=reader.readLine();
            auth=line.substring(line.indexOf(":")+1);
            System.out.println("cookie已加载"+" "+"0Xg2_2132_auth:"+auth);

            line= reader.readLine();
            saltKey=line.substring(line.indexOf(":")+1);
            System.out.println("cookie已加载"+" "+"0Xg2_2132_saltkey:"+saltKey);
        }catch (Exception e){
            System.err.println("无法读取cookie:"+e);
        }

        System.out.println("请输入开始和结束id");

        System.out.print("开始:");
        long start=scanner.nextLong();

        System.out.print("结束:");
        long end=scanner.nextLong();

        long startTime=System.currentTimeMillis();
        UserSpider spider=new UserSpider(start,end);
        if (enableAnalyser) {
            UserAnalyser analyser = new UserAnalyser();
            spider.setAnalyser(analyser);
        }
        spider.setCookie("0Xg2_2132_auth",auth);
        spider.setCookie("0Xg2_2132_saltkey",saltKey);

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.print("["+(System.currentTimeMillis()-startTime)/1000+"s"+"]"
                        +"完成/总数:"+spider.getFinish()+"/"+spider.getTotal()+","
                        +"失败:"+spider.getFail()
                        +",");
                long finish=spider.getFinish();
                if (finish>0){
                    System.out.println("平均速度:"+(System.currentTimeMillis()-startTime)/spider.getFinish()+"ms/个");
                }else {
                    System.out.println();
                }
            }
        },500,1000);

        spider.startAndWait();
        timer.cancel();

        printResult(startTime, spider);

        if (enableAnalyser) analysisData(spider.getAnalyser(), spider.getFound());
    }

    private static void printResult(long startTime, UserSpider spider) {
        System.out.println("----------------------------------");
        System.out.println("完成");
        System.out.println("耗时"+(System.currentTimeMillis()- startTime)/1000+"s");
        System.out.println("平均耗时:"+(System.currentTimeMillis()- startTime)/ spider.getFinish()+"ms/个");
        System.out.println("找到/总数:"+ spider.getFound()+"/"+ spider.getTotal());
        System.out.println("失败:"+ spider.getFail());
        System.out.println("----------------------------------");
        System.out.println();
    }

    private static void analysisData(UserAnalyser analyser, long total) {
        Map<String, Long> userGroupMap= analyser.getUserGroup();
        Map<LocalDate, Long> regDate= analyser.getRegDate();
        Map<LocalDate, Long> actDate= analyser.getActDate();

        System.out.println("--------------数据分析--------------");
        System.out.println(total+"个可用数据");
        System.out.println();

        System.out.println("用户组:");
        UserAnalyser.userGroups.forEach(s -> {
            Long num=userGroupMap.get(s);
            if (num!=null)System.out.println(s+":"+num);
        });
        Long other_userGroup_num=userGroupMap.get(null);
        if (other_userGroup_num!=null)System.out.println("其他:"+other_userGroup_num);
        System.out.println();

        System.out.println("注册时间:");
        UserAnalyser.regDates.forEach(localDate -> {
            Long num=regDate.get(localDate);
            if (num!=null)System.out.println(localDate+"以后:"+num);
        });
        Long other_regTime_num=regDate.get(null);
        if (other_regTime_num!=null)System.out.println("更早:"+other_regTime_num);
        System.out.println();

        System.out.println("上一次活动时间:");
        UserAnalyser.actDates.forEach(localDate -> {
            Long num=actDate.get(localDate);
            if (num!=null)System.out.println(localDate+"以后:"+num);
        });
        Long other_actTime_num=actDate.get(null);
        if (other_actTime_num!=null)System.out.println("更早:"+other_actTime_num);
        System.out.println();

        System.out.println("----------------------------------");
    }
}
