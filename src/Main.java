import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    static ArrayList<String[]> bigrammes;
    public static void main(String[] args){

        int n = 100;
        //Spelling corrector TODO
        String[] request = args[0].split(" ");
        String[] searches = new String[0];
        String bigramme = "";
        String concat = request[0] + request[1] + request[2] + request[3] + request[4];
        if (request[0].equals("search")) {
            searches = new String[args.length - 1];
            System.arraycopy(args, 1, searches, 0, args.length - 1);
        } else if (concat.equals("themostprobablebigramof")){
            bigramme = request[5];
        } else{
            System.out.println("Commande inconnue");
            return;
        }

        String dir = "dataset";

        FileMap<String, String> fichiers = new FileMap<>(n);
        FileMap<String, Integer> fichiersNbMots = new FileMap<>(n);

        WordMap<String, FileMap<String, ArrayList<Integer>>> mots = new WordMap<>(n);

        try {
            List<String> processedText = processFilesText(dir);
            if (!bigramme.isEmpty()){
                for (String contenu : processedText) {
                    bigramme(contenu.split(" "));
                }
            }
            List<String> nomsFichiers = fichiersDansDossier(dir);
            int k = 0;
            for (String nomFichier : nomsFichiers){
                fichiers.put(nomFichier, processedText.get(k));
                ++k;
            }

            for (Entry<String, String> fichier : fichiers.entrySet()){
                String nomFichier = fichier.getKey();
                String[] contenu = fichier.getValue().split(" ");
                int nbMotsDansFichier = contenu.length;
                fichiersNbMots.put(nomFichier, nbMotsDansFichier);

                for (int i = 0; i < nbMotsDansFichier; ++i) {
                    String mot = contenu[i];
                    FileMap<String, ArrayList<Integer>> fm =  mots.get(mot);
                    if (fm == null) {
                        FileMap<String, ArrayList<Integer>> motFileMap = mots.put(mot, new FileMap<>(n));
                        ArrayList<Integer> tempList = new ArrayList<>();
                        tempList.add(i);
                        motFileMap.put(nomFichier, tempList);
                    } else {
                        ArrayList<Integer> positions = fm.get(nomFichier);
                        if (positions == null) {
                            ArrayList<Integer> tempList = new ArrayList<>();
                            tempList.add(i);
                            fm.put(nomFichier, tempList);
                        } else {
                            positions.add(i);
                        }
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        //WordMap printer
        for (Entry<String, FileMap<String, ArrayList<Integer>>> entry : mots.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            ArrayList<String> mapS = new ArrayList<>();
            for (Entry<String, ArrayList<Integer>> entryFileMap : entry.getValue().entrySet())
                mapS.add(entryFileMap.getKey() + " -> " + entryFileMap.getValue().toString());
            System.out.println(mapS);
        }

        //Bigrammes
        if (! bigramme.isEmpty()){
            System.out.println();
        }


        //search
        int nbFichiersTotal = fichiersNbMots.size();
        for (String mot : searches) {
            FileMap<String, ArrayList<Integer>> fm = mots.get(mot);
            String fichier = "";
            double score = 0;
            double IDF = 1 + Math.log( (double) (1 + nbFichiersTotal) / (1 + fm.size()));
            for (Entry<String, ArrayList<Integer>> fic : fm.entrySet()) {
                String nomFichier = fic.getKey();
                double TF = (double) fic.getValue().size() / fichiersNbMots.get(nomFichier);
                double TFIDF = TF * IDF;
                if (TFIDF > score) {
                    fichier = nomFichier;
                    score = TFIDF;
                }
                else if (TFIDF == score)
                    fichier = nomFichier.compareTo(fichier) > 0 ? fichier : nomFichier;
            }
            System.out.println("Le meilleur fichier pour le mot \"" + mot + "\" est \"" + fichier + "\"."
            + " avec un score de " + arrondirDouble(score, 4));
        }
    }

    static double arrondirDouble(double n, int precision) {
        double facteur = Math.pow(10, precision);
        return  Math.round(n * facteur) / facteur;
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
                StringBuilder word = new StringBuilder();
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

    static void bigramme(String[] text){
        bigrammes = new ArrayList<>();
        for (int i = 0; i<text.length-1; i++) {
            String[] tuple = new String[2];
            tuple[0] = (text[i]);
            tuple[1] = (text[i+1]);
            bigrammes.add(tuple);
        }
    }
}