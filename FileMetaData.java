package BACKUPER;

public class FileMetaData {
    private int fileLength;
    private String filePath;
    private String clientPath;
    public int getFileLength() {
        return this.fileLength;
    }
    public String getFilePath() {
        return filePath;
    }

    public String getClientPath() {
        return clientPath;
    }

    public FileMetaData(String filePath, int fileLength){
        this.fileLength = fileLength;
        this.filePath = filePath;
        this.clientPath="";
    }
    public FileMetaData(String clientPath,String filePath,int fileLength){
        this.fileLength=fileLength;
        this.filePath=filePath;
        this.clientPath=clientPath;
    }
}