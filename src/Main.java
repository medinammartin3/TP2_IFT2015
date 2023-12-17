import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args){

        int n = 100;

        String dir = "Inputs";

        WordMap<String, String> mapNomFichiers = new WordMap<>(n);

        WordMap<String, FileMap<String, ArrayList<Integer>>> mapMots = new WordMap<>(n);

        try {
            List<String> processedText = processFilesText(dir);
            System.out.println(processedText);

            List<String> nomsFichiers = fichiersDansDossier(dir);

            int k = 0;

            for (String nomFichier : nomsFichiers){
                mapNomFichiers.put(nomFichier, processedText.get(k));
            }

            for (Entry<String, String> nomFichier : mapNomFichiers.entrySet()){
                String fichier = nomFichier.getKey();
                String text = nomFichier.getValue();
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
                    String mot = document.tokens().get(i).word();
                    FileMap<String, ArrayList<Integer>> motInFileMap =  mapMots.get(mot);
                    if (motInFileMap == null) {
                        FileMap<String, ArrayList<Integer>> motFileMap = mapMots.put(mot, new FileMap<>(n));
                        ArrayList<Integer> tempList = new ArrayList<>();
                        tempList.add(i);
                        motFileMap.put(fichier, tempList);
                    } else {
                        ArrayList<Integer> positions = motInFileMap.get(fichier);
                        if (positions == null) {
                            ArrayList<Integer> tempList = new ArrayList<>();
                            tempList.add(i);
                            motInFileMap.put(fichier, tempList);
                        } else {
                            positions.add(i);
                        }
                    }

                }
            }

            for (Entry<String, FileMap<String, ArrayList<Integer>>> entry : mapMots.entrySet()) {
                System.out.print(entry.getKey() + ": ");
                for (Entry<String, ArrayList<Integer>> entryFileMap : entry.getValue().entrySet()){
                    System.out.print(entryFileMap.getKey() + " -> [");
                    for (int pos : entryFileMap.getValue()){
                        System.out.print(pos + ", ");
                    }
                    System.out.println("]");
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static List<String> fichiersDansDossier(String path) {
        return Arrays.asList(Objects.requireNonNull(new File(path).list()));
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