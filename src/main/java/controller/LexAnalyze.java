package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Kryvonis on 3/7/16.
 */
public class LexAnalyze {
    private String filename;
    private String[] keyWords = {"BEGIN", "END", "PROCEDURE", "RETURN"};
    private String[] attribvar = {"SIGNAL", "COMPLEX", "INTEGER", "FLOAT", "BLOCKFLOAT", "EXT"};
    private ArrayList<Lexem> ResultArray;

    private int charId;
    private BufferedReader reader;
    private TablesStack tables;
    private int row;
    private int colum;
    private ArrayList errors = new ArrayList<>();


    /**
     * @param filename "src/main/resources/input"
     */

    public LexAnalyze(String filename) {
        this.filename = filename;
        this.tables = new TablesStack(keyWords,attribvar);
        this.ResultArray = new ArrayList<Lexem>();
    }

    public void start() {
        tables.clearTables();
        this.tables = new TablesStack(keyWords,attribvar);
        this.ResultArray.clear();
        this.row = 1;
        this.colum = 1;
        try {
            this.reader = new BufferedReader(new FileReader(this.filename));
            this.charId = reader.read();
            while (this.charId != -1) {
                checkProgram();

            }
            this.reader.close();
        } catch (Exception e) {
            errors.add(e.toString());
        }
    }

    public void addLexCode(int lexCode, String lexeme) {
        if (lexCode == -1) {
            errors.add(lexeme);
        } else {
            ResultArray.add(new Lexem(lexCode, row, colum ));
        }
    }

    /**
     * check current Word
     *
     * @throws Exception
     */


    private void checkProgram() throws Exception {

        int lexCode = 0;
        switch (tables.getAttribute(charId)) {
            case 0:
                checkWiteSpace();
                break;
            case 1:
                lexCode = checkConst();
                break;
            case 2:
                lexCode = checkIdentifier();
                break;
            case 3:
                lexCode = checkComment();
                break;
            case 4:
                colum++;
                addLexCode(charId, "" + (char) charId);
                charId = reader.read();

                break;
            case 5:
                String buffer = "Illegal symbol at line " + row + " on column " + colum;
                addLexCode(-1, buffer);
                charId = reader.read();
        }
        if ((char) charId == '\n') {
            do {
                colum = 1;
                row++;
            } while ((char) (charId = reader.read()) == '\n');
        }


    }


    private void checkWiteSpace() throws IOException {
        while (charId != -1 && tables.getAttribute(charId) == 0) {
            charId = reader.read();
            colum++;
            if ((char) charId == '\n') {
                colum = 1;
                row++;
            }
        }
    }


    private int checkIdentifier() throws IOException {
        String buffer = "";
        int lexCode;


        while (charId != -1 && (tables.getAttribute(charId) == 2 ||
                tables.getAttribute(charId) == 1)) {
            buffer += (char) charId;
            charId = reader.read();
            colum++;
        }
        if (tables.searchInKeyWords(buffer)) {
            lexCode = tables.getKeyWordsCode(buffer);
        }else if(tables.searchInAttrVar(buffer)){
            lexCode = tables.getKeyAttrVar(buffer);
        }
        else {
            if (tables.searchInIdentifiers(buffer)) {
                lexCode = tables.getIdentifierCode(buffer);
            } else {
                lexCode = tables.addInIdentifier(buffer);
            }
        }
        addLexCode(lexCode, buffer);
        return lexCode;
    }

    private int checkConst() throws IOException {
        String buff = "";
        int lexCode;
        buff += (char) charId;
        charId = reader.read();
        while (tables.getAttribute(charId) == 1) {
            buff += (char) charId;
            charId = reader.read();
            colum++;
        }

        if (tables.searchInConstTable(buff)) {
            lexCode = tables.getConstTableCode(buff);
        } else {
            lexCode = tables.addInConstTable(buff);
        }
        addLexCode(lexCode, buff);
        return lexCode;
    }


    private int checkComment() throws Exception {
        try {

            int[] lexCode = new int[2];

            lexCode[0] = charId;
            charId = reader.read();
            colum++;
            if ((char) charId == '\n') {
                colum = 1;
                row++;
            }

            if (charId != 42) {
                addLexCode(lexCode[0], "" + (char) charId);
                lexCode[1] = charId;

                return 0;
            }
            lexCode[0] = reader.read();
            colum++;
            if ((char) lexCode[0] == '\n') {
                colum = 1;
                row++;
            }
            lexCode[1] = reader.read();
            colum++;
            if ((char) lexCode[1] == '\n') {
                colum = 1;
                row++;
            }
            while (lexCode[1] != -1 && !(lexCode[0] == 42 && lexCode[1] == 41)) {
                lexCode[0] = lexCode[1];
                lexCode[1] = reader.read();
                colum++;
                if ((char) lexCode[1] == '\n') {
                    colum = 1;
                    row++;
                }
            }
            if (lexCode[1] == -1) {
                throw new Exception("END OF FILE BUT COMMENT IS`t CLOSE");
            }
            charId = reader.read();
            colum++;
            if ((char) charId == '\n') {
                colum = 1;
                row++;
            }
            lexCode[0] = 0;
            return 0;

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            throw e;
        }

        return 0;
    }


    public String getResult() {
        return ResultArray.toString();
    }

    public ArrayList getErrors() {
        return errors;
    }

    public String getIdentifiers() {
        return tables.getIdentifierToPrint();
    }

    public String getKeyWords() {
        return tables.getKeyWordsToPrint();
    }

    public String getAttrVar() {
        return tables.getAttrVarToPrint();
    }

    public String getConstTable() {
        return tables.getConstTableToPrint();
    }

    public TablesStack getTables() {
        return tables;
    }

    public ArrayList<Lexem> getResultArray() {
        return ResultArray;
    }

    public void clearAll() {
        tables.clearTables();
        ResultArray.clear();

    }
}
