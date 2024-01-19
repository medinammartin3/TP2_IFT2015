//Auteurs:
//Étienne Mitchell-Bouchard (20243430)
//Martin Medina (20235219)

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.*;
import java.util.*;

public class Main {
    static final String dataPath = "Data";
    static final String queryPath = "query.txt";
    public static void main(String[] args){
        try {
            List<String> texteTraiteDesFichiers = formatterFichiers();
            FileMap<String, String[]> fichiers = new FileMap<>(100); //Nom des fichiers et leur contenu
            //Remplissage de la FileMap
            int k = 0;
            for (String nomFichier : Objects.requireNonNull(new File(dataPath).list())){
                fichiers.put(nomFichier, texteTraiteDesFichiers.get(k).split(" "));
                ++k;
            }
            //Remplissage de la WordMap
            WordMap<String, FileMap<String, ArrayList<Integer>>> mots = new WordMap<>(100); //Map principale
            for (Entry<String, String[]> fichier : fichiers.entrySet()) {
                String nomFichier = fichier.getKey();
                String[] contenu = fichier.getValue();
                int nbMotsDansFichier = contenu.length;
                //On parcourt chaque mot dans le fichier
                for (int i = 0; i < nbMotsDansFichier; ++i) {
                    String mot = contenu[i];
                    FileMap<String, ArrayList<Integer>> fm = mots.get(mot);
                    //Pour un mot jamais rencontré dans le dataset
                    if (fm == null) {
                        FileMap<String, ArrayList<Integer>> motFileMap = mots.put(mot, new FileMap<>());
                        ArrayList<Integer> tempList = new ArrayList<>();
                        tempList.add(i);
                        motFileMap.put(nomFichier, tempList);

                    }
                    //Pour un mot déjà rencontré dans le dataset
                    else {
                        ArrayList<Integer> positions = fm.get(nomFichier);
                        //Première fois qu'on rencontre le mot dans le fichier
                        if (positions == null) {
                            ArrayList<Integer> tempList = new ArrayList<>();
                            tempList.add(i);
                            fm.put(nomFichier, tempList);
                        }
                        //Mot déjà rencontré dans le fichier
                        else
                            positions.add(i);
                    }
                }
            }
            //Traitement des requêtes
            BufferedReader br = new BufferedReader(new FileReader(queryPath));
            ArrayList<String> solutions = new ArrayList<>();
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] s = ligne.split(" ");
                String out;
                //Requête de recherche
                if (s[0].equals("search")) {
                    String fichierMax = "";
                    //Pour un seul mot
                    if (s.length == 2) {
                        String mot = trouverMotLePlusProche((List<String>) mots.keySet(), s[1]);
                        FileMap<String, ArrayList<Integer>> fm = mots.get(mot);
                        double score = 0;
                        double IDF = 1 + Math.log( (double) (1 + fichiers.size()) / (1 + fm.size()));
                        //Trouver le score pour chaque fichier et trouver le score maximum
                        for (Entry<String, ArrayList<Integer>> fic : fm.entrySet()) {
                            String fichier = fic.getKey();
                            double TF = (double) fic.getValue().size() / fichiers.get(fichier).length;
                            double TFIDF = TF * IDF;
                            if (TFIDF > score) {
                                fichierMax = fichier;
                                score = TFIDF;
                            }
                            else if (TFIDF == score)
                                fichierMax = plusPetitString(fichierMax, fichier);
                        }
                        out = "Best file for \"" + mot + "\" : " + fichierMax + " | Score: " + score;
                    }
                    //Pour plusieurs mots
                    else {
                        //Additionner les scores
                        FileMap<String, Double> scoresDesFichiers = new FileMap<>(100);
                        for (int i = 1; i < s.length; ++i) {
                            String mot = trouverMotLePlusProche((List<String>) mots.keySet(), s[i]);
                            FileMap<String, ArrayList<Integer>> fm = mots.get(mot);
                            double IDF = 1 + Math.log( (double) (1 + fichiers.size()) / (1 + fm.size()));
                            for (Entry<String, ArrayList<Integer>> fic : fm.entrySet()) {
                                String fichier = fic.getKey();
                                double TF = (double) fic.getValue().size() / fichiers.get(fichier).length;
                                double TFIDF = TF * IDF;
                                if (scoresDesFichiers.containsKey(fichier))
                                    scoresDesFichiers.put(fichier, scoresDesFichiers.get(fichier) + TFIDF);
                                else
                                    scoresDesFichiers.put(fichier, TFIDF);
                            }
                        }
                        //Trouver le plus grand score
                        double max = 0;
                        for(Entry<String, Double> e : scoresDesFichiers.entrySet()) {
                            if (e.getValue() > max){
                                fichierMax = e.getKey();
                                max = e.getValue();
                            }
                            else if (e.getValue() == max)
                                fichierMax = plusPetitString(fichierMax, e.getKey());
                        }
                        out = "Best file for \"" + ligne.substring(7) + "\" : " +  fichierMax
                                + " | Score: " + max;
                    }
                }
                //Bigrammes
                else if (s[0].equals("the") && s[1].equals("most") && s[2].equals("probable") && s[3].equals("bigram")
                && s[4].equals("of")) {
                    String mot = trouverMotLePlusProche((List<String>) mots.keySet(), s[5]);
                    //Trouver tous les bigrammes du mot
                    WordMap<String, Integer> big = new WordMap<>(100);
                    for (String contenu: texteTraiteDesFichiers)
                        trouverBigramme(big, contenu.split(" "), mot);
                    String bigrammeMax = "";
                    int fois = 0;
                    //Trouver le bigramme le plus probable
                    for(Entry<String, Integer> entry : big.entrySet()) {
                        String bigramme = entry.getKey();
                        if (entry.getValue() > fois) {
                            bigrammeMax = bigramme;
                            fois = entry.getValue();
                        }
                        else if (entry.getValue() == fois)
                            bigrammeMax = plusPetitString(bigramme, bigrammeMax);
                    }
                    out = "Most probable bigram of \"" + mot + "\" : " + bigrammeMax;
                }
                else {
                    out = "INVALID COMMAND: " + ligne;
                }
                System.out.println(out);
                solutions.add(out);
            }
            br.close();
            //Écrire les solutions
            BufferedWriter bw = new BufferedWriter(new FileWriter("solution.txt"));
            bw.write(String.join("\n", solutions));
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Trouve le plus petit String alphabétiquement. Chiffre < Majuscules < Minuscules
    static String plusPetitString(String s1, String s2) {
        return s1.compareTo(s2) > 0 ? s2 : s1;
    }
    //Formatte tous les fichiers dans le dataset et les retourne sous forme de List avec comme élément le contenu
    //formatté de chaque fichier sous forme de String
    static List<String> formatterFichiers() throws IOException{

        List<String> processedTexts = new ArrayList<>();

        // Code obtenu de l'énoncé du TP avec quelques modifications
        File folder = new File(dataPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
            listOfFiles = new File[0]; //Liste vide s'il n'y a pas de fichiers dans le dossier
        int cpt = 0;
        boolean dejaPrint = false;
        System.out.println("Annotation des documents avec StanfordNLP...");
        for (File file : listOfFiles) {
            if (file.isFile()) {
                BufferedReader br = new BufferedReader(new FileReader(dataPath + "/" + file.getName()));
                StringBuilder word = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    String newline = line.replaceAll("[^’'a-zA-Z0-9]", " ");
                    String finalline = newline.replaceAll("\\s+", " ").trim();
                    Properties props = new Properties();
                    props.setProperty("annotators", "tokenize,pos,lemma");
                    props.setProperty("coref.algorithm", "neural");
                    RedwoodConfiguration.current().clear().apply();
                    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    CoreDocument document = new CoreDocument(finalline);
                    pipeline.annotate(document);
                    for (CoreLabel tok : document.tokens()) {
                        String str = String.valueOf(tok.lemma());
                        if (!(str.contains("'s") || str.contains("’s"))) {
                            word.append(str).append(" ");
                        }
                    }
                }
                String str = String.valueOf(word);
                str = str.replaceAll("[^a-zA-Z0-9]"," ").replaceAll("\\s+"," ").trim();

                processedTexts.add(str);
                //Afficheur de la progression
                int progres = Math.round((float) cpt / listOfFiles.length * 100);
                if (progres % 10 == 0 && progres > 0) {
                    if (!dejaPrint) {
                        System.out.println("Progrès: " + progres + "%");
                        dejaPrint = true;
                    }
                }
                else
                    dejaPrint = false;
                ++cpt;
            }
        }
        System.out.println("Annotation des documents terminée!");
        return processedTexts;
    }
    //Trouve tous les bigrammes pour le mot passé en paramètre dans le texte et compte les occurences dans la Map passé
    //en paramètre
    static void trouverBigramme(WordMap<String, Integer> map, String[] texte, String mot){
        for (int i = 0; i < texte.length-1; i++) {
            if (texte[i].equals(mot)) {
                String bi = texte[i + 1];
                if (map.containsKey(bi))
                    map.put(bi, map.get(bi) + 1);
                else
                    map.put(bi, 1);
            }
        }
    }
    //Corrige les erreurs d'orthographe des requetes en trouvant le mot le plus proche
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
    //Les 3 prochaines fonctions ont été pris du site cité en haut pour calculer la distance de Levenshtein
    //Nous avons ajouté quelques modifications au code pour le rendre plus compact
    //DÉBUT
    static int compute_Levenshtein_distanceDP(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++)
        {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else {
                    dp[i][j] = minm_edits(dp[i - 1][j - 1]
                                    + NumOfReplacement(str1.charAt(i - 1),str2.charAt(j - 1)),
                            dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                }
            }
        }
        return dp[str1.length()][str2.length()];
    }

    static int NumOfReplacement(char c1, char c2) {
        return c1 == c2 ? 0 : 1;
    }

    static int minm_edits(int... nums) {
        return Arrays.stream(nums).min().orElse(Integer.MAX_VALUE);
    }
    //FIN
}
