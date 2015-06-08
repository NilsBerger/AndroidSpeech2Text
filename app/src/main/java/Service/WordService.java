package Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Nils on 5/26/2015.
 */
public abstract class WordService {
    public static List<String> getUpperCaseList(List<String> stringList)
    {
        if(!stringList.get(0).isEmpty()){
            String word = stringList.get(0);
            String output = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            stringList.set(0,output);
            return stringList;

        }
        return stringList;
    }

    public static String getTime(){
        DateFormat df = new SimpleDateFormat("h:mm");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }
}
