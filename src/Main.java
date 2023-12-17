import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args){

        String dir = "Inputs";
        WordMap<String, Integer> mapMots = new WordMap<>(2);

        try {
            List<String> processedText = processFilesText(dir);
            System.out.println(processedText);

            int k = 0;

            for (String text : processedText){
                System.out.println(bigramme(text));
                // set up pipeline properties
                Properties props = new Properties();
                // set the list of annotators to run
                props.setProperty("annotators", "tokenize,pos,lemma");
                // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
                props.setProperty("coref.algorithm", "neural");
                // build pipeline
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                // create a document object
                CoreDocument document = new CoreDocument(text);
                // annnotate the document
                pipeline.annotate(document);
                //System.out.println(document.tokens());
                for (int i = 0; i<document.tokens().size(); i++) {
                    mapMots.put(document.tokens().get(i).word(), k);
                    k++;
                }
            }
            mapMots.remove("punctuation");

            System.out.println(mapMots.get("punctuation"));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static List<String> processFilesText(String dir) throws IOException{

        List<String> processedTexts = new ArrayList<>();

        // CODE OBTENU DE L'ÉNONCÉ DU TP
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                BufferedReader br = new BufferedReader(new FileReader(new File(dir + "/" + file.getName())));
                StringBuffer word = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    String newline = line.replaceAll("[^’'a-zA-Z0-9]", " ");
                    String finalline = newline.replaceAll("\\s+", " ").trim();
                    // set up pipeline properties
                    Properties props = new Properties();
                    // set the list of annotators to run
                    props.setProperty("annotators", "tokenize,pos,lemma");
                    // set a property for an annotator, in this case the coref
                    // annotator is being set to use the neural algorithm
                    props.setProperty("coref.algorithm", "neural");
                    // Delete red lines on excecution
                    RedwoodConfiguration.current().clear().apply();
                    // build pipeline
                    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    // create a document object
                    CoreDocument document = new CoreDocument(finalline);
                    // annnotate the document
                    pipeline.annotate(document);
                    //System.out.println(document.tokens());
                    for (CoreLabel tok : document.tokens()) {
                        //System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
                        String str = String.valueOf(tok.lemma());
                        if (!(str.contains("'s") || str.contains("’s"))) {
                            word.append(str).append(" ");
                        }
                    }
                }
                String str = String.valueOf(word);
                str = str.replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").trim();

                processedTexts.add(str);
                // now str is a string which has the content of the read file but it is
                // processed and their words are space-separated. However there maybe some
                // details which has not been cleaned very well, just follow these steps to
                // clean the text.
                // in the following you can continue your own implementation
            }
        }
        return processedTexts;
    }

    static ArrayList<LinkedList<String>> bigramme(String text){
        ArrayList<LinkedList<String>> bigrammes = new ArrayList<>();
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,pos,lemma");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(text);
        // annnotate the document
        pipeline.annotate(document);
        //System.out.println(document.tokens());
        for (int i = 0; i<document.tokens().size()-1; i++) {
            LinkedList<String> tuple = new LinkedList<>();
            tuple.add(document.tokens().get(i).word());
            tuple.add(document.tokens().get(i+1).word());
            bigrammes.add(tuple);
        }
        return bigrammes;
    }
}