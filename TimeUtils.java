import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.premium.game.util.Util;

public class TimeUtils {
  private static final SimpleDateFormat Pu = new SimpleDateFormat("HH:mm dd.MM.yyyy");
  
  private static final SimpleDateFormat axh = new SimpleDateFormat("HH:mm dd.MM.yyyy");
  
  public enum DeclensionKey {
	  DAYS, HOUR, MINUTES, PIECE, POINT;
	}
  
  public static String declension(long paramLong, DeclensionKey paramDeclensionKey) {
	    String str1 = "";
	    String str2 = "";
	    String str3 = "";
	    switch (paramDeclensionKey) {
	      case DAYS:
	        str1 = "Day";
	        str2 = "Days";
	        str3 = "Days";
	        break;
	      case HOUR:
	        str1 = "Hour";
	        str2 = "Hours";
	        str3 = "Hours";
	        break;
	      case MINUTES:
	        str1 = "Minute";
	        str2 = "Minutes";
	        str3 = "Minutes";
	        break;
	      case PIECE:
	        str1 = "Piece";
	        str2 = "Pieces";
	        str3 = "Pieces";
	        break;
	      case POINT:
	        str1 = "Point";
	        str2 = "Points";
	        str3 = "Points";
	        break;
	    } 
	    if (paramLong > 100L)
	      paramLong %= 100L; 
	    if (paramLong > 20L)
	      paramLong %= 10L; 
	    return (paramLong == 1L) ? str1 : ((paramLong == 2L || paramLong == 3L || paramLong == 4L) ? str2 : str3);
	  }
  
  public static String toSimpleFormat(Calendar paramCalendar) {
    return Pu.format(paramCalendar.getTime());
  }
  
  public static String toSimpleFormat(long paramLong) {
    return Pu.format(Long.valueOf(paramLong));
  }
  
  public static String toHeroRecordFormat(long paramLong) {
    return axh.format(Long.valueOf(paramLong));
  }
  
  public static String formatTime(int paramInt, boolean paramBoolean) {
    String str;
    int i = paramInt / 86400;
    int j = (paramInt - i * 24 * 3600) / 3600;
    int k = (paramInt - i * 24 * 3600 - j * 3600) / 60;
    if (i >= 1) {
      if (j < 1 || paramBoolean) {
        str = i + " " + declension(i, DeclensionKey.DAYS);
      } else {
        str = i + " " + declension(i, DeclensionKey.DAYS) + " " + j + " " + declension(j, DeclensionKey.HOUR);
      } 
    } else if (j >= 1) {
      if (k < 1 || paramBoolean) {
        str = j + " " + declension(j, DeclensionKey.HOUR);
      } else {
        str = j + " " + declension(j, DeclensionKey.HOUR) + " " + k + " " + declension(k, DeclensionKey.MINUTES);
      } 
    } else {
      str = k + " " + declension(k, DeclensionKey.MINUTES);
    } 
    return str;
  }
  
  public static long parse(String paramString) throws ParseException {
    return Pu.parse(paramString).getTime();
  }
}