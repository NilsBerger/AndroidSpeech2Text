package Service;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class QueueBuffer {

    private static LinkedBlockingQueue<String> _warteschlange = new LinkedBlockingQueue<String>(100);

    /*
     *Fügt ein Wort in die Wartschlange ein.
     *
     */
    public static void fuegeWortHinzu(final String wort)
    {
        _warteschlange.add(wort);
    }

    /*
     * Fügt eine Liste von Strings in die Warteschlange ein
     */
    public static void fuegeWoerterHinzu(final List<String> stringList)
    {
        List<String> liste = WordService.getUpperCaseList(stringList);
        for(String woerter: liste)
        {
            _warteschlange.add(woerter);
        }
    }

    public static String getWort()
    {
        return _warteschlange.poll();
    }

    public static boolean isEmpty()
    {
        return _warteschlange.isEmpty();
    }




}