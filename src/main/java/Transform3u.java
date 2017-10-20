import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.*;

public class Transform3u {

    private TM3UList FList = new TM3UList();

    public void getContent(String Filename) throws IOException, ExceptionInvalidFormat {
        FList.Load(Filename);
    }

    public void saveContent(String Filename) throws IOException {
        Files.copy(Paths.get(Filename), Paths.get(Filename + ".old"), REPLACE_EXISTING);
        FList.Save(Filename);
    }

    private String translitChar(char ch){
        switch (ch){
            case 'А': return "A";
            case 'Б': return "B";
            case 'В': return "V";
            case 'Г': return "G";
            case 'Д': return "D";
            case 'Е': return "E";
            case 'Ё': return "JE";
            case 'Ж': return "ZH";
            case 'З': return "Z";
            case 'И': return "I";
            case 'Й': return "Y";
            case 'К': return "K";
            case 'Л': return "L";
            case 'М': return "M";
            case 'Н': return "N";
            case 'О': return "O";
            case 'П': return "P";
            case 'Р': return "R";
            case 'С': return "S";
            case 'Т': return "T";
            case 'У': return "U";
            case 'Ф': return "F";
            case 'Х': return "KH";
            case 'Ц': return "C";
            case 'Ч': return "CH";
            case 'Ш': return "SH";
            case 'Щ': return "JSH";
            case 'Ъ': return "HH";
            case 'Ы': return "IH";
            case 'Ь': return "JH";
            case 'Э': return "EH";
            case 'Ю': return "JU";
            case 'Я': return "JA";
            default: return String.valueOf(ch);
        }
    }

    private String formatChannelName(String Name) {
        char[] c = Name.toCharArray();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            if (!Character.isLetterOrDigit(c[i]))
                continue;
            if (i == 0) {
                b.append(Character.toUpperCase(c[i]));
            } else {
                char n = c[i - 1];
                if (!Character.isLetterOrDigit(n)) {
                    b.append(Character.toUpperCase(c[i]));
                } else {
                    b.append(c[i]);
                }
            }
        }
        return b.toString();
    }

    private String translitStr(String Name) {
        char[] c = Name.toCharArray();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            boolean f = Character.isUpperCase(c[i]);
            char r = Character.toUpperCase(c[i]);
            String s = translitChar(r);
            if (f == false) {
                s = s.toLowerCase();
            }
            b.append(s);
        }
        return b.toString();
    }

    private String changeChannelName(String Name) {
        // перед пробелом, не буквами и не цифрами букву сделать заглавной и удалить все пробелы, не буквы, не цифры
        String LName = formatChannelName(Name);
        // сделать транслитерацию с сохранением регистра
        LName = translitStr(LName);
        return LName;
    }

    private String generateUniqueName(String Name, int iteration) {
        String LName = (iteration == 0)?Name:Name + Integer.toString(iteration);
        int i = FList.FindByName(LName);
        if (i == -1) {
            return LName;
        } else {
            return generateUniqueName(Name, ++iteration);
        }
    }

    public void changeContent() {
        for (TM3UItem t: FList) {
            String LName = changeChannelName(t.Name);
            if (!LName.equalsIgnoreCase(t.Name)) {
                LName = generateUniqueName(LName, 0);
            }
            //t.Name = LName;
            t.URL = "pipe://ffmpeg -loglevel fatal -re -i " +
                    t.URL +
                    " -vcodec copy -acodec copy -metadata "+
                    "service_provider=EdemTV -metadata service_name=" +
                    LName +
                    " -tune zerolatency -f mpegts pipe:1";
        }

    }

    public static void main(String[] args) throws IOException, ExceptionInvalidFormat {

        Transform3u transform3u = new Transform3u();
        transform3u.getContent(args[0]);
        transform3u.changeContent();
        transform3u.saveContent(args[0]);

    }

 }
