package Multithreded;

/**
 * Created by mpars on 13.11.2017.
 */
public class FileMetaData {
    private int fileLength;
    private String filePath;
    public int getFileLength(){
        return this.fileLength;
    }
    public String getFilePath() {
        return filePath;
    }
    public FileMetaData(String filePath, int fileLength){
        this.fileLength = fileLength;
        this.filePath = filePath;
    }
}
