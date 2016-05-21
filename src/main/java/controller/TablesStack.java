package controller;

import java.util.HashMap;

/**
 * Created by Kryvonis on 3/9/16.
 */
public class TablesStack {
    private int codeKeyWords = 401;
    private int codeIdentifiers = 1001;
    private int constCode = 501;

    private HashMap<Character, Integer> attributes;
    private HashMap<String, Integer> keyWords;
    private HashMap<String, Integer> identifiers;
    private HashMap<String, Integer> attribvar;
    private HashMap<String, Integer> constTable;

    /**
     * @param keyWords
     */
    public TablesStack(String[] keyWords, String[] attrvar) {
        this.attributes = new HashMap<Character, Integer>();
        this.keyWords = new HashMap<String, Integer>();
        this.attribvar = new HashMap<String, Integer>();
        for (String i : keyWords) {
            this.keyWords.put(i, this.codeKeyWords++);

        }
        for (String i : attrvar) {
            this.attribvar.put(i, this.codeKeyWords++);
        }
        for (int i = 0; i < 128; i++) {
            if (i >= 9 && i <= 13 || i >= 28 && i <= 32) {
                this.attributes.put((char) i, 0);    //spaces
            } else if (i >= 48 && i <= 57) {
                this.attributes.put((char) i, 1);    //numbers

            } else if (i >= 65 && i <= 90) {
                this.attributes.put((char) i, 2);    //Symbols

            } else if (i == 40) {
                this.attributes.put((char) i, 3);    //сomment

            } else if (i == 42 || i == 41 || i == 44 || i == 59 || i == 60 || i == 62 || i == 58) {
                this.attributes.put((char) i, 4);    //one symbol

            } else {
                this.attributes.put((char) i, 5);    //others
            }
        }

        this.identifiers = new HashMap<String, Integer>();
        this.constTable = new HashMap<String, Integer>();
    }

    /**
     * Search valuse in identifiers
     *
     * @param value
     *
     * @return
     */
    public boolean searchInIdentifiers(String value) {
        return this.identifiers.containsKey(value);
    }

    /**
     * Add new identifiers
     *
     * @param value
     *
     * @return
     */
    public int addInIdentifier(String value) {
        this.identifiers.put(value, codeIdentifiers++);
        return codeIdentifiers - 1;
    }

    public String getIdentifierToPrint() {
        return this.identifiers.toString();
    }

    public String getKeyWordsToPrint() {
        return this.keyWords.toString();
    }

    public String getAttrVarToPrint() {
        return this.attribvar.toString();
    }

    /**
     * Get code from identifiers
     *
     * @param v
     *
     * @return
     */
    public int getIdentifierCode(String v) {
        return this.identifiers.get(v);
    }

    public boolean searchInKeyWords(String v) {
        return this.keyWords.containsKey(v);
    }

    public boolean searchInAttrVar(String v) {
        return this.attribvar.containsKey(v);
    }

    public int getKeyWordsCode(String v) {
        return this.keyWords.get(v);
    }

    public int getKeyAttrVar(String v) {
        return this.attribvar.get(v);
    }

    public boolean searchInConstTable(String v) {
        return this.constTable.containsKey(v);
    }

    public int addInConstTable(String v) {
        this.constTable.put(v, constCode++);
        return constCode - 1;
    }

    public int getConstTableCode(String v) {
        return this.constTable.get(v);
    }

    public int getAttribute(int c) {
        return this.attributes.get((char) c);
    }

    public String getConstTableToPrint() {
        return this.constTable.toString();
    }


    public HashMap<Character, Integer> getAttributes() {
        return this.attributes;
    }

    public HashMap<String, Integer> getKeyWords() {
        return this.keyWords;
    }

    public HashMap<String, Integer> getIdentifiers() {
        return this.identifiers;
    }
    public HashMap<String, Integer> getAttributsVarTable() {
        return this.attribvar;
    }
    public HashMap<String, Integer> getConstTable() {
        return this.constTable;
    }

    public void clearTables() {
        this.attributes.clear();
        this.keyWords.clear();
        this.identifiers.clear();
        this.constTable.clear();
    }
}
