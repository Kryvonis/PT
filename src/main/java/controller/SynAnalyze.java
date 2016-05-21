package controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kryvonis on 4/21/16.
 */
public class SynAnalyze {
    private final int SIGNAL_PROGRAM = -1;
    private final int PROGRAM = -2;
    private final int PROCEDURE = -3;
    private final int BLOCK = -4;
    private final int DECLARATIONS = -5;
    private final int PROCEDURE_DECLARATIONS = -6;
    private final int PROCEDURE_IDENTIFIER = -7;
    private final int PARAMETERS_LIST = -8;
    private final int STATEMENTS_LIST = -9;
    private final int IDENTIFIERS_LIST = -10;
    private final int VARIABLE_IDENTIFIER = -11;
    private final int STATEMENT = -12;
    private final int ACTUAL_ARGUMENTS = -13;
    private final int ACTUAL_ARGUMENT_LIST = -14;
    private final int UNSIGNED_INTEGER = -15;
    private final int IDENTIFIER = -16;
    private final int ATTRIBUTE = -17;


    private ArrayList<Lexem> resultArray;
    private ArrayList<String> errors;
    private TablesStack tables;
    private HashMap<String, Integer> identifiersTable;
    private HashMap<String, Integer> constTable;
    private HashMap<String, ArrayList<Integer>> procedureParam;
    private Tree<Integer> parseTree;
    private String fileXML;

    public SynAnalyze(ArrayList<Lexem> resultArray, TablesStack tables, String XMLfile) {
        this.resultArray = resultArray;
        this.tables = tables;

        this.fileXML = XMLfile;
        errors = new ArrayList<>();
        constTable = new HashMap<>();
        procedureParam = new HashMap<>();
        identifiersTable = new HashMap<>(tables.getIdentifiers());
        parseTree = new Tree<>(SIGNAL_PROGRAM);


        for (String identifier : identifiersTable.keySet()) {
            identifiersTable.put(identifier, null);
        }

    }

    public void parser() {
        int pointer = 0;
        int address = 0;
        boolean errorFlag = false;
        boolean programEnd = false;
        boolean repeatIdnFlag = false;
        boolean repeatComFlag = false;
        String curProcBuffer = null;


        Tree<Integer> programNode = parseTree.addChild(PROGRAM);
        Tree<Integer> parameters_listNode = null;
        Tree<Integer> blockNode = null;
        Tree<Integer> declarationsNode = null;
        Tree<Integer> procedure_declarationsNode = null;
        Tree<Integer> procedureNode = null;
        Tree<Integer> identifiers_listNode = null;
        Tree<Integer> statements_listNode = null;
        Tree<Integer> statementNode = null;
        Tree<Integer> actual_arguments_listNode = null;
        Tree<Integer> actual_argumentsNode = null;
        Tree<Integer> tmpNode = null;

        int counter = 0;

        while (!errorFlag && !programEnd) switch (address) {
            /**
             * PROGRAM
             */
            case 0: {
                if (pointer + 2 > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() == 403) {
                    pointer++;
                    programNode.addChild(403);

                    if (resultArray.get(pointer).getLex() > 1000) {
                        curProcBuffer = findIdentifier(tables.getIdentifiers(),
                                resultArray.get(pointer).getLex());

                        procedureParam.put(curProcBuffer, new ArrayList<Integer>());

                        programNode.addChild(PROCEDURE_IDENTIFIER)
                                .addChild(IDENTIFIER)
                                .addChild(resultArray.get(pointer).getLex());
                        pointer++;
                    }
                    if (resultArray.get(pointer).getLex() == 40) {
                        pointer++;
                        address = 1;
                        parameters_listNode = programNode.addChild(PARAMETERS_LIST);
                        parameters_listNode.addChild(40);
                        tmpNode = parameters_listNode;
                        break;

                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        address = 1;
                        break;
                    }
                } else {
                    errorFlag = true;
                    break;
                }
            }
            /**
             * parameters-list...;
             */
            case 1: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 1000 && !repeatIdnFlag) {
                    repeatComFlag = false;
                    repeatIdnFlag = true;
                    if (procedureParam.get(curProcBuffer).indexOf(resultArray.get(pointer).getLex()) != -1) {
                        errorFlag = true;
                        break;
                    }
                    procedureParam.get(curProcBuffer).add(resultArray.get(pointer).getLex());

                    parameters_listNode.addChild(VARIABLE_IDENTIFIER)
                            .addChild(IDENTIFIER).addChild(resultArray.get(pointer).getLex());
                    pointer++;
                    if (resultArray.get(pointer).getLex() == 58) {
                        pointer++;
                        if (resultArray.get(pointer).getLex() > 404) {
                            parameters_listNode.addChild(58);
                            parameters_listNode.addChild(ATTRIBUTE).addChild(resultArray.get(pointer).getLex());
                            pointer++;
                        } else {
                            errorFlag = true;
                            break;
                        }
                    }
                    break;

                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    pointer++;
                    parameters_listNode.addChild(41);
                    if (pointer > resultArray.size()) {

                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        address = 4;
                        pointer++;
                        programNode.addChild(59);
                        blockNode = programNode.addChild(BLOCK);
                        repeatComFlag = false;
                        repeatIdnFlag = false;
                        break;
                    }
                    errorFlag = true;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && repeatIdnFlag) {
                    address = 6;
                    pointer++;
                    repeatComFlag = true;
                    repeatIdnFlag = false;
                    identifiers_listNode = parameters_listNode.addChild(IDENTIFIERS_LIST);
                    identifiers_listNode.addChild(44);
                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }
            /**
             * BEGIN...;
             */
            case 2: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                } else if (resultArray.get(pointer).getLex() == 402) {
                    blockNode.addChild(402);
                    pointer++;
                    if (pointer > resultArray.size()) {
                        errorFlag = true;
                        break;
                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        programNode.addChild(59);
                        programEnd = true;
                        break;
                    }
                    errorFlag = true;
                    break;
                } else if (resultArray.get(pointer).getLex() == 404) {
                    /**
                     * Statement-list
                     */
                    pointer++;
                    if (pointer > resultArray.size()) {
                        errorFlag = true;
                        break;
                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        if (statements_listNode == null) {
                            statements_listNode = blockNode.addChild(STATEMENTS_LIST);
                        }
                        statementNode = statements_listNode.addChild(STATEMENT);
                        statementNode.addChild(404);
                        statementNode.addChild(59);
                        pointer++;
                        break;
                    }
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 1000) {
                    curProcBuffer = findIdentifier(tables.getIdentifiers(),
                            resultArray.get(pointer).getLex());
                    if (!procedureParam.containsKey(curProcBuffer)) {
                        errorFlag = true;
                        break;
                    }
                    if (statements_listNode == null) {
                        statements_listNode = blockNode.addChild(STATEMENTS_LIST);
                    }
                    statementNode = statements_listNode.addChild(STATEMENT);
                    statementNode.addChild(PROCEDURE_IDENTIFIER)
                            .addChild(IDENTIFIER).addChild(resultArray.get(pointer).getLex());

                    pointer++;
                    if (pointer > resultArray.size()) {
                        errorFlag = true;
                        break;
                    }
                    if (resultArray.get(pointer).getLex() == 40) {
                        actual_argumentsNode = statementNode.addChild(ACTUAL_ARGUMENTS);
                        actual_argumentsNode.addChild(40);
                        address = 3;
                        repeatComFlag = false;
                        repeatIdnFlag = false;
                        pointer++;
                        break;
                    }
                    errorFlag = true;
                    break;

                } else {
                    errorFlag = true;
                    break;
                }

            }
            /**
             * bloc parameters list 1,2,...);
             */
            case 3: {
                if (resultArray.get(pointer).getLex() > 500 && !repeatIdnFlag) {
                    counter++;
                    repeatIdnFlag = true;
                    repeatComFlag = false;
                    actual_argumentsNode.addChild(UNSIGNED_INTEGER)
                            .addChild(resultArray.get(pointer).getLex());
                    pointer++;
                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    ;
                    actual_argumentsNode.addChild(41);
                    pointer++;
                    if (pointer > resultArray.size()) {
                        errorFlag = true;
                        break;
                    }
                    if (counter != procedureParam.get(curProcBuffer).size()) {
                        errorFlag = true;
                        break;
                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        statementNode.addChild(59);
                        address = 2;
                        counter = 0;
                        pointer++;
                        break;
                    }
                    errorFlag = true;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && !repeatComFlag && repeatIdnFlag) {
                    actual_arguments_listNode = actual_argumentsNode.addChild(ACTUAL_ARGUMENT_LIST);
                    actual_arguments_listNode.addChild(44);

                    pointer++;
                    address = 9;
                    repeatComFlag = true;
                    repeatIdnFlag = false;
                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }

            /**
             * PROGRAM in <BLOCK>
             */
            case 4: {
                if (pointer + 2 > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() == 401) {
                    pointer++;
                    address = 2;
                    blockNode.addChild(401);
                    break;
                }
                if (resultArray.get(pointer).getLex() == 403) {
                    pointer++;
                    declarationsNode = blockNode.addChild(DECLARATIONS);
                    procedure_declarationsNode = declarationsNode.addChild(PROCEDURE_DECLARATIONS);
                    procedureNode = procedure_declarationsNode.addChild(PROCEDURE);
                    procedureNode.addChild(403);
                    address = 7;
                    break;
                } else {
                    errorFlag = true;
                    break;
                }
            }
            case 5: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 1000 && !repeatIdnFlag) {
                    repeatComFlag = false;
                    repeatIdnFlag = true;
                    if (procedureParam.get(curProcBuffer).indexOf(resultArray.get(pointer).getLex()) != -1) {
                        errorFlag = true;
                        break;
                    }
                    procedureParam.get(curProcBuffer).add(resultArray.get(pointer).getLex());

                    tmpNode.addChild(VARIABLE_IDENTIFIER).
                            addChild(IDENTIFIER).addChild(resultArray.get(pointer).getLex());
                    if (resultArray.get(pointer).getLex() == 58) {
                        pointer++;
                        if (resultArray.get(pointer).getLex() > 404) {
                            tmpNode.addChild(58);
                            tmpNode.addChild(ATTRIBUTE).addChild(resultArray.get(pointer).getLex());
                            pointer++;
                        } else {
                            errorFlag = true;
                            break;
                        }
                    }
                    tmpNode = identifiers_listNode;
                    pointer++;
                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    pointer++;
                    parameters_listNode.addChild(41);
                    if (pointer > resultArray.size()) {
                        errorFlag = true;
                        break;
                    }
                    if (resultArray.get(pointer).getLex() == 59) {
                        address = 7;
                        pointer++;
                        procedureNode.addChild(59);
                        repeatComFlag = false;
                        repeatIdnFlag = false;
                        break;
                    }
                    errorFlag = true;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && !repeatComFlag && repeatIdnFlag) {
                    address = 8;
                    identifiers_listNode = parameters_listNode.addChild(IDENTIFIERS_LIST);
                    identifiers_listNode.addChild(44);
                    pointer++;
                    repeatComFlag = true;
                    repeatIdnFlag = false;


                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }
            /**
             * identifiriers list
             */
            case 6: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 1000 && !repeatIdnFlag) {
                    repeatComFlag = false;
                    repeatIdnFlag = true;
                    if (procedureParam.get(curProcBuffer).indexOf(resultArray.get(pointer).getLex()) != -1) {
                        errorFlag = true;
                        break;
                    }
                    procedureParam.get(curProcBuffer).add(resultArray.get(pointer).getLex());
                    identifiers_listNode.addChild(VARIABLE_IDENTIFIER).
                            addChild(IDENTIFIER).addChild(resultArray.get(pointer).getLex());
                    pointer++;
                    if (resultArray.get(pointer).getLex() == 58) {
                        pointer++;
                        if (resultArray.get(pointer).getLex() > 404) {
                            identifiers_listNode.addChild(58);
                            identifiers_listNode.addChild(ATTRIBUTE).addChild(resultArray.get(pointer).getLex());
                            pointer++;
                        } else {
                            errorFlag = true;
                            break;
                        }
                    }

                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    address = 1;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && !repeatComFlag && repeatIdnFlag) {
                    pointer++;
                    repeatComFlag = true;
                    repeatIdnFlag = false;
                    identifiers_listNode.addChild(44);
                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }
            /**
             * DECLARATIONS
             */
            case 7: {
                if (pointer + 2 > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() == 401) {
                    pointer++;
                    address = 2;
                    blockNode.addChild(401);
                    tmpNode = blockNode;
                    break;
                }
                if (resultArray.get(pointer).getLex() == 403) {
                    pointer++;
                    procedure_declarationsNode = procedure_declarationsNode.addChild(PROCEDURE_DECLARATIONS);
                    procedureNode = procedure_declarationsNode.addChild(PROCEDURE);
                    procedureNode.addChild(403);
                }
                if (resultArray.get(pointer).getLex() > 1000) {
                    curProcBuffer = findIdentifier(tables.getIdentifiers(), resultArray.get(pointer).getLex());
                    procedureParam.put(curProcBuffer, new ArrayList<Integer>());
                    procedureNode.addChild(PROCEDURE_IDENTIFIER)
                            .addChild(IDENTIFIER)
                            .addChild(resultArray.get(pointer).getLex());
                    pointer++;
                } else {
                    errorFlag = true;
                    break;
                }
                /**
                 * remember
                 */
                if (resultArray.get(pointer).getLex() == 40) {
                    pointer++;
                    address = 5;
                    procedureParam.put(curProcBuffer, new ArrayList<Integer>());
                    parameters_listNode = procedureNode.addChild(PARAMETERS_LIST);
                    parameters_listNode.addChild(40);
                    tmpNode = parameters_listNode;
                } else {
                    errorFlag = true;
                    break;
                }
                break;

            }
            case 8: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 1000 && !repeatIdnFlag) {
                    repeatComFlag = false;
                    repeatIdnFlag = true;
                    if (procedureParam.get(curProcBuffer).indexOf(resultArray.get(pointer).getLex()) != -1) {
                        errorFlag = true;
                        break;
                    }
                    procedureParam.get(curProcBuffer).add(resultArray.get(pointer).getLex());

                    identifiers_listNode.addChild(VARIABLE_IDENTIFIER).
                            addChild(IDENTIFIER).addChild(resultArray.get(pointer).getLex());
                    if (resultArray.get(pointer).getLex() == 58) {
                        pointer++;
                        if (resultArray.get(pointer).getLex() > 404) {
                            identifiers_listNode.addChild(58);
                            identifiers_listNode.addChild(ATTRIBUTE).addChild(resultArray.get(pointer).getLex());
                            pointer++;
                        } else {
                            errorFlag = true;
                            break;
                        }
                    }
                    pointer++;
                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    address = 5;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && !repeatComFlag && repeatIdnFlag) {
                    pointer++;
                    repeatComFlag = true;
                    repeatIdnFlag = false;
                    identifiers_listNode.addChild(44);
                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }
            /**
             * actual-arguments-list;
             */
            case 9: {
                if (pointer > resultArray.size()) {
                    errorFlag = true;
                    break;
                }
                if (resultArray.get(pointer).getLex() > 500 && !repeatIdnFlag) {
                    counter++;
                    repeatIdnFlag = true;
                    repeatComFlag = false;
                    actual_arguments_listNode.addChild(UNSIGNED_INTEGER).addChild(resultArray.get(pointer).getLex());
                    pointer++;
                } else if (resultArray.get(pointer).getLex() == 41 && !repeatComFlag) {
                    address = 3;
                    break;
                } else if (resultArray.get(pointer).getLex() == 44 && !repeatComFlag && repeatIdnFlag) {
                    pointer++;
                    repeatComFlag = true;
                    repeatIdnFlag = false;
                    actual_arguments_listNode.addChild(44);
                } else {
                    errorFlag = true;
                    break;
                }
                break;
            }
        }

        if (errorFlag) {
            if (pointer < resultArray.size())
                pointer--;
            errors.add("error in line " + resultArray.get(pointer).getRow() +
                    " and column " + resultArray.get(pointer).getColum());
        }
        buildXMLTree();
    }


    public String findIdentifier(HashMap<String, Integer> table, Integer value) {
        for (String key : table.keySet()) {
            if (table.get(key).equals(value))
                return key;
        }
        return null;
    }

    /**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    public void buildXMLTree() {
        Document dom;
        Element e;
        Tree<Integer> currentNode;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootElem = dom.createElement("non-terminal");
            rootElem.setAttribute("name", "SIGNAL-PROGRAM");
            e = rootElem;

            if (!parseTree.isLeaf()) {
                currentNode = parseTree.getChildren().get(0);
                parseTree(currentNode, rootElem, dom);
            }

            dom.appendChild(e);

            try (FileOutputStream xmlFile = new FileOutputStream(this.fileXML)) {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(xmlFile));
            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    public void parseTree(Tree<Integer> currentNode, Element rootElem, Document dom) {
        String textNode;
        Element e;
        if (currentNode.getData() < 0) {
            e = dom.createElement("non-terminal");
        } else {
            e = dom.createElement("terminal");
        }
        switch (currentNode.getData()) {
            case -2:
                textNode = "PROGRAM";
                break;
            case -3:
                textNode = "PROCEDURE";
                break;
            case -4:
                textNode = "BLOCK";
                break;
            case -5:
                textNode = "DECLARATIONS";
                break;
            case -6:
                textNode = "PROCEDURE_DECLARATIONS";
                break;
            case -7:
                textNode = "PROCEDURE_IDENTIFIER";
                break;
            case -8:
                textNode = "PARAMETERS_LIST";
                break;
            case -9:
                textNode = "STATEMENTS_LIST";
                break;
            case -10:
                textNode = "IDENTIFIERS_LIST";
                break;
            case -11:
                textNode = "VARIABLE_IDENTIFIER";
                break;
            case -12:
                textNode = "STATEMENT";
                break;
            case -13:
                textNode = "ACTUAL_ARGUMENTS";
                break;
            case -14:
                textNode = "ACTUAL_ARGUMENT_LIST";
                break;
            case -15:
                textNode = "UNSIGNED_INTEGER";
                break;
            case -16:
                textNode = "IDENTIFIER";
                break;
            case -17:
                textNode = "ATTRIBUTE";
                break;
            default:
                if (currentNode.getData() > 1000) {
                    textNode = currentNode.getData().toString() + " " + findIdentifier(tables.getIdentifiers(), currentNode.getData());
                } else if (currentNode.getData() > 500) {
                    textNode = currentNode.getData().toString() + " " + findIdentifier(tables.getConstTable(), currentNode.getData());
                } else if (currentNode.getData() > 404) {
                    textNode = currentNode.getData().toString() + " " + findIdentifier(tables.getAttributsVarTable(), currentNode.getData());
                } else {
                    textNode = currentNode.getData().toString();
                }
                break;
        }
        e.setAttribute("name", textNode);
        rootElem.appendChild(e);

        if (!currentNode.isLeaf()) {
            rootElem = e;
            for (Tree<Integer> node : currentNode.getChildren()) {
                parseTree(node, rootElem, dom);
            }
        }
    }

    public ArrayList<String> getErrors() {
        return errors;
    }
}
