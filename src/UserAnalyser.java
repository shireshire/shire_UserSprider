import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAnalyser {
    public static final List<String> userGroups=
            List.of("无水", "一水" , "二水" , "三水" , "四水" , "五水" , "六水" , "七水" , "八水" , "九水",
                    "群主大人" , "VIP" , "名誉主管" , "名誉版主" , "名誉会员" , "等待邮箱验证" , "游客",
                    "主管" , "版主大人" , "版主" , "退休");
    public static final List<LocalDate> regDates=
            List.of(LocalDate.of(2025,4,1),
                    LocalDate.of(2025,1,1),
                    LocalDate.of(2024,1,1),
                    LocalDate.of(2023,1,1),
                    LocalDate.of(2022,1,1),
                    LocalDate.of(2021,1,1),
                    LocalDate.of(2020,1,1),
                    LocalDate.of(2019,1,1),
                    LocalDate.of(2018,1,1),
                    LocalDate.of(2017,1,1),
                    LocalDate.of(2016,1,1),
                    LocalDate.of(2015,1,1),
                    LocalDate.of(2014,1,1),
                    LocalDate.of(2013,1,1),
                    LocalDate.of(2012,1,1),
                    LocalDate.of(2011,1,1),
                    LocalDate.of(2010,1,1));
    public static final List<LocalDate> actDates=
            List.of(LocalDate.of(2025,4,1),
                    LocalDate.of(2025,1,1),
                    LocalDate.of(2024,1,1),
                    LocalDate.of(2015,1,1));

    private final Map<String,Long> userGroupMap;
    private final Map<LocalDate,Long> regDateMap;
    private final Map<LocalDate,Long> actDateMap;

    public UserAnalyser(){
        userGroupMap=new HashMap<>();
        regDateMap=new HashMap<>();
        actDateMap=new HashMap<>();
    }

    public void addUser(String userGroup,String regDate,String actData){
        LocalDate date_regDate=DateUtils.parseToLocalDate(regDate);
        LocalDate date_actDate=DateUtils.parseToLocalDate(actData);

        Long num_userGroup =userGroupMap.get(userGroup);
        if (num_userGroup ==null){
            if (userGroups.contains(userGroup))userGroupMap.put(userGroup,1L);
            else userGroupMap.put(null,1L);
        }else {
            if (userGroups.contains(userGroup))userGroupMap.put(userGroup, num_userGroup +1);
            else userGroupMap.put(null, num_userGroup +1);
        }

        LocalDate temp_regDate=null;
        for (LocalDate date:regDates){
            if (date.isBefore(date_regDate)){
                temp_regDate=date;
                break;
            }
        }
        Long num_regDate=regDateMap.get(temp_regDate);
        if (num_regDate==null) regDateMap.put(temp_regDate,1L);
        else regDateMap.put(temp_regDate,num_regDate+1);

        LocalDate temp_actDate=null;
        for (LocalDate date:actDates){
            if (date.isBefore(date_actDate)){
                temp_actDate=date;
                break;
            }
        }
        Long num_actDate=actDateMap.get(temp_actDate);
        if (num_actDate==null)actDateMap.put(temp_actDate,1L);
        else actDateMap.put(temp_actDate,num_actDate+1);
    }

    public Map<LocalDate, Long> getRegDate() {
        return regDateMap;
    }

    public Map<String, Long> getUserGroup() {
        return userGroupMap;
    }

    public Map<LocalDate, Long> getActDate() {
        return actDateMap;
    }
}
