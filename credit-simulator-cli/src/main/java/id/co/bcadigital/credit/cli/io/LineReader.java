package id.co.bcadigital.credit.cli.io;

public interface LineReader {

    String readLine(String prompt);

    boolean hasNext();

    boolean interactive();
}
