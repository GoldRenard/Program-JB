/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.core;

import lombok.Getter;
import lombok.Setter;
import org.goldrenard.jb.configuration.BotConfiguration;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.*;
import org.goldrenard.jb.parser.MapsResource;
import org.goldrenard.jb.parser.PronounsResource;
import org.goldrenard.jb.parser.SetsResource;
import org.goldrenard.jb.parser.base.CollectionResource;
import org.goldrenard.jb.parser.base.NamedResource;
import org.goldrenard.jb.utils.IOUtils;
import org.goldrenard.jb.utils.Timer;
import org.goldrenard.jb.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Class representing the AIML bot
 */
@Getter
@Setter
public class Bot {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final BotConfiguration configuration;

    private final Properties properties = new Properties();

    private final PreProcessor preProcessor;

    private final AIMLProcessor processor;

    private final Graphmaster brain;

    private Graphmaster learnfGraph;

    private Graphmaster learnGraph;

    private String name;

    private NamedResource<AIMLSet> sets = new SetsResource(this);

    private NamedResource<AIMLMap> maps = new MapsResource(this);

    private CollectionResource<String> pronouns = new PronounsResource();

    private String rootPath;
    private String aimlifPath;
    private String aimlPath;
    private String configPath;
    private String setsPath;
    private String mapsPath;

    public Bot() {
        this(BotConfiguration.builder().build());
    }

    /**
     * Constructor (default action, default path)
     *
     * @param name Bot Name
     */
    public Bot(String name) {
        this(BotConfiguration.builder().name(name).build());
    }

    /**
     * Constructor
     *
     * @param configuration configuration
     */
    public Bot(BotConfiguration configuration) {
        this.configuration = configuration;
        this.name = configuration.getName();
        setAllPaths(configuration);
        this.brain = new Graphmaster(this);
        this.learnfGraph = new Graphmaster(this, "learnf");
        this.learnGraph = new Graphmaster(this, "learn");
        this.preProcessor = new PreProcessor(this);
        this.processor = new AIMLProcessor(this);
        addProperties();

        int count = sets.read(setsPath);
        if (log.isDebugEnabled()) {
            log.debug("Loaded {} set elements.", count);
        }

        count = maps.read(mapsPath);
        if (log.isDebugEnabled()) {
            log.debug("Loaded {} map elements.", count);
        }

        count = pronouns.read(configPath);
        if (log.isDebugEnabled()) {
            log.debug("Loaded {} pronouns.", count);
        }

        Date aimlDate = new Date(new File(aimlPath).lastModified());
        Date aimlIFDate = new Date(new File(aimlifPath).lastModified());
        if (log.isDebugEnabled()) {
            log.debug("AIML modified {} AIMLIF modified {}", aimlDate, aimlIFDate);
        }

        switch (configuration.getAction()) {
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

        Category version = new Category(this, 0, "PROGRAM VERSION", "*", "*",
                configuration.getProgramName(), "update.aiml");
        brain.addCategory(version);
        brain.nodeStats();
        learnfGraph.nodeStats();
    }

    /**
     * Set all directory path variables for this bot
     *
     * @param configuration configuration of Program AB
     */
    private void setAllPaths(BotConfiguration configuration) {
        this.rootPath = configuration.getPath();
        String botNamePath = this.rootPath + "/bots/" + name;
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
     * add an array list of categories with a specific file name
     *
     * @param file           name of AIML file
     * @param moreCategories list of categories
     */
    private void addMoreCategories(String file, ArrayList<Category> moreCategories) {
        if (file.contains(Constants.learnfAimlFile)) {
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
                                ArrayList<Category> moreCategories = processor.AIMLToCategories(aimlPath, file);
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
                        if (file.endsWith(configuration.getAimlifFileSuffix()) || file.endsWith(configuration.getAimlifFileSuffix().toUpperCase())) {
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
        String filePath = aimlifPath + "/" + fileName + configuration.getAimlifFileSuffix();
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
        writeIFCategories(graph.getCategories(), file + configuration.getAimlifFileSuffix());
        File dir = new File(aimlifPath);
        return dir.setLastModified(new Date().getTime());
    }

    /**
     * write learned categories to AIMLIF file
     */
    public boolean writeLearnfIFCategories() {
        return writeCertainIFCategories(learnfGraph, Constants.learnfAimlFile);
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
        Category build = new Category(this, 0, "BRAIN BUILD", "*", "*", new Date().toString(), "update.aiml");
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
                        bw = new BufferedWriter(new FileWriter(aimlifPath + "/" + fileName + configuration.getAimlifFileSuffix()));
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
        Category build = new Category(this, 0, "BRAIN BUILD", "*", "*", new Date().toString(), "update.aiml");
        brain.addCategory(build);

        ArrayList<Category> brainCategories = brain.getCategories();
        brainCategories.sort(Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            if (!c.getFilename().equals(Constants.nullAimlFile))
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
                        Category c = Category.IFToCategory(this, strLine);
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
                AIMLSet set = sets.get(setName);
                x = set != null ? "FOUNDITEM" : "NOTFOUND";
            }
            builder.append(" ").append(x);
        }
        return builder.toString().trim();
    }
}
