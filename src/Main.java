import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args){

        int n = 100;
        String dir = "dataset";
        try {
            List<String> texteTraiteDesFichiers = processFilesText(dir);
            List<String> nomsFichiers = fichiersDansDossier(dir);
            FileMap<String, String> fichiers = new FileMap<>(n);
            FileMap<String, Integer> fichiersNbMots = new FileMap<>(n);
            WordMap<String, FileMap<String, ArrayList<Integer>>> mots = new WordMap<>(n);
            int k = 0;
            for (String nomFichier : nomsFichiers){
                fichiers.put(nomFichier, texteTraiteDesFichiers.get(k));
                ++k;
            }

            for (Entry<String, String> fichier : fichiers.entrySet()) {
                String nomFichier = fichier.getKey();
                String[] contenu = fichier.getValue().split(" ");
                int nbMotsDansFichier = contenu.length;
                fichiersNbMots.put(nomFichier, nbMotsDansFichier);

                for (int i = 0; i < nbMotsDansFichier; ++i) {
                    String mot = contenu[i];
                    FileMap<String, ArrayList<Integer>> fm = mots.get(mot);
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

            BufferedWriter bw = new BufferedWriter(new FileWriter("solution.txt"));
            String[] requetes =
                    Files.readAllLines(Paths.get("query.txt"), StandardCharsets.UTF_8).toArray(new String[0]);
            for (String requete : requetes) {
                String[] s = requete.split(" ");
                if (s[0].equals("search")) {
                    for (int i = 1; i < s.length; ++i) {
                        String mot = trouverMotLePlusProche((List<String>) mots.keySet(), s[i]);
                        int nbFichiersTotal = fichiersNbMots.size();
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
                        bw.write(fichier + "\n");
                        System.out.println("Le meilleur fichier pour le mot \"" + mot + "\" est \"" + fichier + "\"."
                                + " avec un score de " + arrondirDouble(score, 4));
                    }
                }
                else if (s[0].equals("the") && s[1].equals("most") && s[2].equals("probable") && s[3].equals("bigram")
                && s[4].equals("of")) {
                    String mot = trouverMotLePlusProche((List<String>) mots.keySet(), s[5]);
                    WordMap<String, Integer> big = new WordMap<>(n);
                    for (String contenu: texteTraiteDesFichiers) {
                        trouverBigramme(big, contenu.split(" "), mot);
                    }
                    String bigramme = "";
                    int fois = 0;
                    for(Entry<String, Integer> entry : big.entrySet()) {
                        String m = entry.getKey();
                        if (entry.getValue() > fois) {
                            bigramme = m;
                            fois = entry.getValue();
                        }
                        else if (entry.getValue() == fois) {
                            bigramme = m.compareTo(bigramme) > 0 ? bigramme : m;
                        }
                    }
                    bw.write(mot + " " + bigramme + "\n");
                    System.out.println(mot + " " + bigramme);
                }
                else {
                    System.out.println("Commande inconnue dans query.txt: " + requete);
                }
            }
            bw.close();

        } catch (IOException e) {
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
                System.out.println("Annotation des documents; Progrès: " + file);
            }
        }
        return processedTexts;
    }

    static void trouverBigramme(WordMap<String, Integer> map, String[] texte, String mot){
        for (int i = 0; i < texte.length-1; i++) {
            if (texte[i].equals(mot)) {
                String bi = texte[i + 1];
                if (map.containsKey(bi))
                    map.setValue(bi, map.get(bi) + 1);
                else
                    map.put(bi, 1);
            }
        }
    }

    static String trouverMotLePlusProche(List<String> mots, String mot) {
        if (mots.contains(mot))
            return mot;
        String motLePlusProche = "";
        int distanceMin = 100000000;
        for (String motAComparer : mots) {
            int distance = compute_Levenshtein_distanceDP(mot, motAComparer);
            if (distance < distanceMin) {
                motLePlusProche = motAComparer;
                distanceMin = distance;
            }
            else if (distance == distanceMin) {
                motLePlusProche = motAComparer.compareTo(motLePlusProche) > 0 ? motLePlusProche : motAComparer;
            }
        }
        return motLePlusProche;
    }
    //Code pris de https://www.geeksforgeeks.org/java-program-to-implement-levenshtein-distance-computing-algorithm/
    // DÉBUT DU PLAGIAT
    static int compute_Levenshtein_distanceDP(String str1, String str2)
    {
        // A 2-D matrix to store previously calculated
        // answers of subproblems in order
        // to obtain the final

        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
        {
            for (int j = 0; j <= str2.length(); j++) {

                // If str1 is empty, all characters of
                // str2 are inserted into str1, which is of
                // the only possible method of conversion
                // with minimum operations.
                if (i == 0) {
                    dp[i][j] = j;
                }

                // If str2 is empty, all characters of str1
                // are removed, which is the only possible
                //  method of conversion with minimum
                //  operations.
                else if (j == 0) {
                    dp[i][j] = i;
                }

                else {
                    // find the minimum among three
                    // operations below

                    dp[i][j] = minm_edits(dp[i - 1][j - 1]
                                    + NumOfReplacement(str1.charAt(i - 1),str2.charAt(j - 1)), // replace
                            dp[i - 1][j] + 1, // delete
                            dp[i][j - 1] + 1); // insert
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }

    // check for distinct characters
    // in str1 and str2

    static int NumOfReplacement(char c1, char c2) {
        return c1 == c2 ? 0 : 1;
    }

    // receives the count of different
    // operations performed and returns the
    // minimum value among them.

    static int minm_edits(int... nums)
    {
        return Arrays.stream(nums).min().orElse(Integer.MAX_VALUE);
    }
    //END OF PLAGIAT

    //TO DELETE AFTER
    static double arrondirDouble(double n, int precision) {
        double facteur = Math.pow(10, precision);
        return  Math.round(n * facteur) / facteur;
    }
    static List<String> fichiersDansDossier(String path) {
        return Arrays.asList(Objects.requireNonNull(new File(path).list()));
    }
}
