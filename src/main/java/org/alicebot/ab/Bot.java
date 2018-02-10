/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/
package org.alicebot.ab;

import lombok.Getter;
import lombok.Setter;
import org.alicebot.ab.configuration.MagicStrings;
import org.alicebot.ab.model.*;
import org.alicebot.ab.utils.IOUtils;
import org.alicebot.ab.utils.Timer;
import org.alicebot.ab.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class representing the AIML bot
 */
@Getter
@Setter
public class Bot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final Properties properties = new Properties();

    private final PreProcessor preProcessor;

    private final AIMLProcessor processor;

    private final Graphmaster brain;

    private Graphmaster learnfGraph;

    private Graphmaster learnGraph;

    private String name = MagicStrings.default_bot_name;

    private HashMap<String, AIMLSet> setMap = new HashMap<>();

    private HashMap<String, AIMLMap> mapMap = new HashMap<>();

    private HashSet<String> pronounSet = new HashSet<>();

    private String aimlifPath;
    private String aimlPath;
    private String configPath;
    private String setsPath;
    private String mapsPath;

    /**
     * Set all directory path variables for this bot
     *
     * @param root root directory of Program AB
     * @param name name of bot
     */
    private void setAllPaths(String root, String name) {
        String botNamePath = root + "/bots/" + name;
        if (log.isTraceEnabled()) {
            log.trace("Init bot: Name = {} Path = {}", name, botNamePath);
        }
        aimlPath = botNamePath + "/aiml";
        aimlifPath = botNamePath + "/aimlif";
        configPath = botNamePath + "/config";
        setsPath = botNamePath + "/sets";
        mapsPath = botNamePath + "/maps";
    }

    /**
     * Constructor (default action, default path, default bot name)
     */
    public Bot() {
        this(MagicStrings.default_bot, MagicStrings.root_path);
    }

    /**
     * Constructor (default action, default path)
     *
     * @param name Bot Name
     */
    public Bot(String name) {
        this(name, MagicStrings.root_path);
    }

    /**
     * Constructor (default action)
     *
     * @param name Bot Name
     * @param path Base path of bot
     */
    public Bot(String name, String path) {
        this(name, path, "auto");
    }

    /**
     * Constructor
     *
     * @param name   name of bot
     * @param path   root path of Program AB
     * @param action Program AB action
     */
    public Bot(String name, String path, String action) {
        int count;
        this.name = name;
        setAllPaths(path, name);
        this.brain = new Graphmaster(this);
        this.learnfGraph = new Graphmaster(this, "learnf");
        this.learnGraph = new Graphmaster(this, "learn");
        this.preProcessor = new PreProcessor(this);
        this.processor = new AIMLProcessor();
        addProperties();
        count = addAIMLSets();
        if (log.isDebugEnabled()) {
            log.debug("Loaded {} set elements.", count);
        }
        count = addAIMLMaps();
        if (log.isDebugEnabled()) {
            log.debug("Loaded {} map elements.", count);
        }
        this.pronounSet = getPronouns();
        this.setMap.put(MagicStrings.natural_number_set_name, new AIMLSet(MagicStrings.natural_number_set_name, this));
        this.mapMap.put(MagicStrings.map_successor, new AIMLMap(MagicStrings.map_successor, this));
        this.mapMap.put(MagicStrings.map_predecessor, new AIMLMap(MagicStrings.map_predecessor, this));
        this.mapMap.put(MagicStrings.map_singular, new AIMLMap(MagicStrings.map_singular, this));
        this.mapMap.put(MagicStrings.map_plural, new AIMLMap(MagicStrings.map_plural, this));

        Date aimlDate = new Date(new File(aimlPath).lastModified());
        Date aimlIFDate = new Date(new File(aimlifPath).lastModified());
        if (log.isDebugEnabled()) {
            log.debug("AIML modified {} AIMLIF modified {}", aimlDate, aimlIFDate);
        }
        MagicStrings.pannous_api_key = Utilities.getPannousAPIKey(this);
        MagicStrings.pannous_login = Utilities.getPannousLogin(this);

        switch (action) {
            case "aiml2csv":
                addCategoriesFromAIML();
                break;
            case "csv2aiml":
            case "chat-app":
                addCategoriesFromAIMLIF();
                break;
            default:
                if (aimlDate.after(aimlIFDate)) {
                    addCategoriesFromAIML();
                    writeAIMLIFFiles();
                } else {
                    addCategoriesFromAIMLIF();
                    if (brain.getCategories().size() == 0) {
                        addCategoriesFromAIML();
                    }
                }
                break;
        }

        Category version = new Category(0, "PROGRAM VERSION", "*", "*", MagicStrings.program_name_version, "update.aiml");
        brain.addCategory(version);
        brain.nodeStats();
        learnfGraph.nodeStats();
    }

    private HashSet<String> getPronouns() {
        HashSet<String> pronounSet = new HashSet<>();
        String pronouns = Utilities.getFile(configPath + "/pronouns.txt");
        String[] splitPronouns = pronouns.split("\n");
        for (String p : splitPronouns) {
            if (p.length() > 0) {
                pronounSet.add(p);
            }
        }
        return pronounSet;
    }

    /**
     * add an array list of categories with a specific file name
     *
     * @param file           name of AIML file
     * @param moreCategories list of categories
     */
    private void addMoreCategories(String file, ArrayList<Category> moreCategories) {
        if (file.contains(MagicStrings.learnf_aiml_file)) {
            for (Category c : moreCategories) {
                brain.addCategory(c);
                learnfGraph.addCategory(c);
            }
        } else {
            for (Category c : moreCategories) {
                brain.addCategory(c);
            }
        }
    }

    /**
     * Load all brain categories from AIML directory
     */
    private void addCategoriesFromAIML() {
        Timer timer = new Timer();
        timer.start();
        int count = 0;
        try {
            // Directory path here
            String file;
            File folder = new File(aimlPath);
            if (folder.exists()) {
                File[] listOfFiles = IOUtils.listFiles(folder);
                if (log.isTraceEnabled()) {
                    log.trace("Loading AIML files from {}", aimlPath);
                }
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".aiml") || file.endsWith(".AIML")) {
                            if (log.isTraceEnabled()) {
                                log.trace("Reading AIML {}", file);
                            }
                            try {
                                ArrayList<Category> moreCategories = AIMLProcessor.AIMLToCategories(aimlPath, file);
                                addMoreCategories(file, moreCategories);
                                count += moreCategories != null ? moreCategories.size() : 0;
                            } catch (Exception e) {
                                log.error("Problem loading {}", file, e);
                            }
                        }
                    }
                }
            } else {
                log.warn("addCategoriesFromAIML: {} does not exist.", aimlPath);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        if (log.isTraceEnabled()) {
            log.trace("Loaded {} categories in {} sec", count, timer.elapsedTimeSecs());
        }
    }

    /**
     * load all brain categories from AIMLIF directory
     */
    private void addCategoriesFromAIMLIF() {
        Timer timer = new Timer();
        timer.start();
        int count = 0;
        try {
            // Directory path here
            String file;
            File folder = new File(aimlifPath);
            if (folder.exists()) {
                File[] listOfFiles = IOUtils.listFiles(folder);
                if (log.isTraceEnabled()) {
                    log.trace("Loading AIML files from {}", aimlifPath);
                }
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        file = listOfFile.getName();
                        if (file.endsWith(MagicStrings.aimlif_file_suffix) || file.endsWith(MagicStrings.aimlif_file_suffix.toUpperCase())) {
                            if (log.isTraceEnabled()) {
                                log.trace("Reading AIML {}", file);
                            }
                            try {
                                ArrayList<Category> moreCategories = readIFCategories(aimlifPath + "/" + file);
                                count += moreCategories.size();
                                addMoreCategories(file, moreCategories);
                            } catch (Exception e) {
                                log.error("Problem loading {}", file, e);
                            }
                        }
                    }
                }

            } else {
                log.warn("addCategoriesFromAIMLIF: {} does not exist.", aimlifPath);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        if (log.isTraceEnabled()) {
            log.trace("Loaded {} categories in {} sec", count, timer.elapsedTimeSecs());
        }
    }

    /**
     * write all AIML and AIMLIF categories
     */
    public void writeQuit() {
        writeAIMLIFFiles();
        writeAIMLFiles();
    }

    /**
     * read categories from specified AIMLIF file into specified graph
     *
     * @param graph    Graphmaster to store categories
     * @param fileName file name of AIMLIF file
     */
    public void readCertainIFCategories(Graphmaster graph, String fileName) {
        int count;
        String filePath = aimlifPath + "/" + fileName + MagicStrings.aimlif_file_suffix;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                ArrayList<Category> certainCategories = readIFCategories(filePath);
                for (Category d : certainCategories) {
                    graph.addCategory(d);
                }
                count = certainCategories.size();
                log.info("readCertainIFCategories {} categories from {}", count, filePath);
            } catch (Exception e) {
                log.error("Problem loading {}", file, e);
            }
        } else {
            log.warn("No {} file found", filePath);
        }
    }

    /**
     * write certain specified categories as AIMLIF files
     *
     * @param graph the Graphmaster containing the categories to write
     * @param file  the destination AIMLIF file
     */
    public boolean writeCertainIFCategories(Graphmaster graph, String file) {
        if (log.isTraceEnabled()) {
            log.trace("writeCertainIFCaegories {} size={}", file, graph.getCategories().size());
        }
        writeIFCategories(graph.getCategories(), file + MagicStrings.aimlif_file_suffix);
        File dir = new File(aimlifPath);
        return dir.setLastModified(new Date().getTime());
    }

    /**
     * write learned categories to AIMLIF file
     */
    public boolean writeLearnfIFCategories() {
        return writeCertainIFCategories(learnfGraph, MagicStrings.learnf_aiml_file);
    }

    /**
     * write categories to AIMLIF file
     *
     * @param cats     array list of categories
     * @param filename AIMLIF filename
     */
    private void writeIFCategories(ArrayList<Category> cats, String filename) {
        File existsPath = new File(aimlifPath);
        if (existsPath.exists())
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(aimlifPath + "/" + filename))) {
                for (Category category : cats) {
                    writer.write(Category.categoryToIF(category));
                    writer.newLine();
                }
            } catch (Exception e) {
                log.error("writeIFCategories problem {}", filename, e);
            }
    }

    /**
     * Write all AIMLIF files from bot brain
     */
    public boolean writeAIMLIFFiles() {
        if (log.isTraceEnabled()) {
            log.trace("writeAIMLIFFiles");
        }

        HashMap<String, BufferedWriter> fileMap = new HashMap<>();
        Category build = new Category(0, "BRAIN BUILD", "*", "*", new Date().toString(), "update.aiml");
        brain.addCategory(build);
        ArrayList<Category> brainCategories = brain.getCategories();
        brainCategories.sort(Category.CATEGORY_NUMBER_COMPARATOR);

        File existsPath = new File(aimlifPath);
        if (existsPath.exists()) {
            for (Category c : brainCategories) {
                try {
                    BufferedWriter bw;
                    String fileName = c.getFilename();
                    if (fileMap.containsKey(fileName)) {
                        bw = fileMap.get(fileName);
                    } else {
                        bw = new BufferedWriter(new FileWriter(aimlifPath + "/" + fileName + MagicStrings.aimlif_file_suffix));
                        fileMap.put(fileName, bw);
                    }
                    bw.write(Category.categoryToIF(c));
                    bw.newLine();
                } catch (Exception e) {
                    log.error("Error: ", e);
                }
            }
            for (String set : fileMap.keySet()) {
                BufferedWriter bw = fileMap.get(set);
                //Close the bw
                try {
                    if (bw != null) {
                        bw.flush();
                        bw.close();
                    }
                } catch (IOException e) {
                    log.error("Error closing writer {}", set, e);
                }
            }
            return existsPath.setLastModified(new Date().getTime());
        }
        return false;
    }

    /**
     * Write all AIML files.  Adds categories for BUILD and DEVELOPMENT ENVIRONMENT
     */
    public boolean writeAIMLFiles() {
        if (log.isTraceEnabled()) {
            log.trace("writeAIMLFiles");
        }
        HashMap<String, BufferedWriter> fileMap = new HashMap<>();
        Category build = new Category(0, "BRAIN BUILD", "*", "*", new Date().toString(), "update.aiml");
        brain.addCategory(build);

        ArrayList<Category> brainCategories = brain.getCategories();
        brainCategories.sort(Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            if (!c.getFilename().equals(MagicStrings.null_aiml_file))
                try {
                    BufferedWriter bw;
                    String fileName = c.getFilename();
                    if (fileMap.containsKey(fileName)) {
                        bw = fileMap.get(fileName);
                    } else {
                        String copyright = Utilities.getCopyright(this, fileName);
                        bw = new BufferedWriter(new FileWriter(aimlPath + "/" + fileName));
                        fileMap.put(fileName, bw);
                        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
                                "<aiml>\n");
                        bw.write(copyright);
                        //bw.newLine();
                    }
                    bw.write(Category.categoryToAIML(c) + "\n");
                    //bw.newLine();
                } catch (Exception e) {
                    log.error("Error: ", e);
                }
        }
        for (String set : fileMap.keySet()) {
            BufferedWriter bw = fileMap.get(set);
            //Close the bw
            try {
                if (bw != null) {
                    bw.write("</aiml>\n");
                    bw.flush();
                    bw.close();
                }
            } catch (IOException e) {
                log.error("Error closing writer {}", set, e);
            }
        }
        File dir = new File(aimlPath);
        return dir.setLastModified(new Date().getTime());
    }

    /**
     * load bot properties
     */
    private void addProperties() {
        try {
            properties.getProperties(configPath + "/properties.txt");
        } catch (Exception e) {
            log.error("Error reading properties {}", e);
        }
    }

    /**
     * read AIMLIF categories from a file into bot brain
     *
     * @param filename name of AIMLIF file
     * @return array list of categories read
     */
    private ArrayList<Category> readIFCategories(String filename) {
        ArrayList<Category> categories = new ArrayList<>();
        try (FileInputStream fstream = new FileInputStream(filename)) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    try {
                        Category c = Category.IFToCategory(strLine);
                        categories.add(c);
                    } catch (Exception e) {
                        log.error("Invalid AIMLIF in {} line {}", filename, strLine);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return categories;
    }

    /**
     * Load all AIML Sets
     */
    private int addAIMLSets() {
        int count = 0;
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            File folder = new File(setsPath);
            if (folder.exists()) {
                File[] listOfFiles = IOUtils.listFiles(folder);
                if (log.isTraceEnabled()) {
                    log.trace("Loading AIML Sets files from {}", setsPath);
                }
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".txt") || file.endsWith(".TXT")) {
                            String setName = file.substring(0, file.length() - ".txt".length());
                            if (log.isTraceEnabled()) {
                                log.trace("Read AIML Set {} from {}", setName, file);
                            }
                            AIMLSet aimlSet = new AIMLSet(setName, this);
                            count += aimlSet.readAIMLSet(this);
                            setMap.put(setName, aimlSet);
                        }
                    }
                }
            } else {
                log.warn("addAIMLSets: {} does not exist.", setsPath);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return count;
    }

    /**
     * Load all AIML Maps
     */
    private int addAIMLMaps() {
        int cnt = 0;
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            File folder = new File(mapsPath);
            if (folder.exists()) {
                File[] listOfFiles = IOUtils.listFiles(folder);
                if (log.isTraceEnabled()) {
                    log.trace("Loading AIML Map files from{}", mapsPath);
                }
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".txt") || file.endsWith(".TXT")) {
                            String mapName = file.substring(0, file.length() - ".txt".length());
                            if (log.isTraceEnabled()) {
                                log.trace("Read AIML Map {} from {}", mapName, file);
                            }
                            AIMLMap aimlMap = new AIMLMap(mapName, this);
                            cnt += aimlMap.readAIMLMap(this);
                            mapMap.put(mapName, aimlMap);
                        }
                    }
                }
            } else {
                log.warn("addAIMLMaps: {} does not exist.", mapsPath);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return cnt;
    }

    public void deleteLearnfCategories() {
        ArrayList<Category> learnfCategories = learnfGraph.getCategories();
        deleteLearnCategories(learnfCategories);
        learnfGraph = new Graphmaster(this);
    }

    public void deleteLearnCategories() {
        ArrayList<Category> learnCategories = learnGraph.getCategories();
        deleteLearnCategories(learnCategories);
        learnGraph = new Graphmaster(this);
    }

    private void deleteLearnCategories(ArrayList<Category> learnCategories) {
        for (Category c : learnCategories) {
            Nodemapper n = brain.findNode(c);
            log.info("Found node {} for {}", n, c.inputThatTopic());
            if (n != null) {
                n.setCategory(null);
            }
        }
    }

    /**
     * check Graphmaster for shadowed categories
     */
    public void shadowChecker() {
        shadowChecker(brain.getRoot());
    }

    /**
     * traverse graph and test all categories found in leaf nodes for shadows
     *
     * @param node Node mapper
     */
    private void shadowChecker(Nodemapper node) {
        if (NodemapperOperator.isLeaf(node)) {
            String input = node.getCategory().getPattern();
            input = brain.replaceBotProperties(input);
            input = input
                    .replace("*", "XXX")
                    .replace("_", "XXX")
                    .replace("^", "")
                    .replace("#", "");
            String that = node.getCategory().getThat()
                    .replace("*", "XXX")
                    .replace("_", "XXX")
                    .replace("^", "")
                    .replace("#", "");
            String topic = node.getCategory().getTopic()
                    .replace("*", "XXX")
                    .replace("_", "XXX")
                    .replace("^", "")
                    .replace("#", "");
            input = instantiateSets(input);
            log.debug("shadowChecker: input={}", input);
            Nodemapper match = brain.match(input, that, topic);
            if (match != node) {
                log.debug("             {}", Graphmaster.inputThatTopic(input, that, topic));
                log.debug("MATCHED:     {}", match.getCategory().inputThatTopic());
                log.debug("SHOULD MATCH:{}", node.getCategory().inputThatTopic());
            }
        } else {
            for (String key : NodemapperOperator.keySet(node)) {
                shadowChecker(NodemapperOperator.get(node, key));
            }
        }
    }

    private String instantiateSets(String pattern) {
        String[] splitPattern = pattern.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String x : splitPattern) {
            if (x.startsWith("<SET>")) {
                String setName = AIMLProcessor.trimTag(x, "SET");
                AIMLSet set = setMap.get(setName);
                x = set != null ? "FOUNDITEM" : "NOTFOUND";
            }
            builder.append(" ").append(x);
        }
        return builder.toString().trim();
    }
}
