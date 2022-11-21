import java.io.File;

public class Main {
    public static void main(String args[]) {
        try {
            String fileType = null;
            String lang = null;

            switch (args.length) {
                case 3 :
                    break;
                case 4 :
                    fileType = args[3];
                    break;
                case 5 :
                    fileType = args[3];
                    lang = args[4];
                    break;
                default :
                    System.out.println("Please input valid arguments.");
                    String[] argumentsExplanation = new String[5];
                    for (int i = 0; i < 5; i++) {
                       argumentsExplanation[i] = "Argument " + (i+1) + " is a";
                    }
                    argumentsExplanation[0] += " required URL.";
                    argumentsExplanation[1] += " required API key.";
                    argumentsExplanation[2] += " required boolean value: \"true\" if the URL points to an online image, \"false\" if it is a file path.";
                    argumentsExplanation[3] += "n optional file type (eg \"png\").";
                    argumentsExplanation[4] += "n optional three letter language code (eg \"bul\" will use the Bulgarian language for the output).\nIf language is omitted, English is used.";
                    for (int i = 0; i < 5; i++) {
                        System.out.println(argumentsExplanation[i]);
                    }
                    return;
            }

            if (Boolean.parseBoolean(args[2])) {
                System.out.println(OCRClass.getStringWithOCR(args[0],args[1],fileType,lang));
            } else {
                File file = new File(args[0]);
                System.out.println(OCRClass.getStringWithOCR(file,args[1],fileType,lang));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
