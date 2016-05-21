package controller;

/**
 * Created by Kryvonis on 4/6/16.
 */
public class Lexem {
    private int lex;
    private int colum;
    private int row;

    public Lexem(int lex,int row,int colum) {
        this.lex = lex;
        this.row = row;
        this.colum = colum;
    }

    @Override
    public String toString(){
        return ""+lex+"{"+row+","+colum+"}";
    }
    public int getLex() {
        return lex;
    }

    public void setLex(int lex) {
        this.lex = lex;
    }

    public int getColum() {
        return colum;
    }

    public void setColum(int colum) {
        this.colum = colum;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
