package BACKUPER;

public class FileMetaData {
    private int fileLength;
    private String filePath;
    private String clientPath;
    private int version;
    public int getFileLength() {
        return this.fileLength;
    }
    public String getFilePath() {
        return filePath;
    }
    public String getClientPath() {
        return clientPath;
    }

    public int getVersion() {
        return version;
    }

    public FileMetaData(String filePath, int fileLength){
        this.fileLength = fileLength;
        this.filePath = filePath;
        this.clientPath="";
        this.version=0;
    }
    public FileMetaData(int version,String clientPath,String filePath,int fileLength){
        this.fileLength=fileLength;
        this.filePath=filePath;
        this.clientPath=clientPath;
        this.version=version;
    }
}