package BACKUPER;

public class FileMetaData {
    private long fileLength;
    private String filePath;
    private String clientPath;
    private int version;
    
    public long getFileLength() {
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

    public FileMetaData(String filePath, long fileLength){
        this.fileLength = fileLength;
        this.filePath = filePath;
        this.clientPath="";
        this.version=0;
    }
    public FileMetaData(int version,String clientPath,String filePath,long fileLength){
        this.fileLength=fileLength;
        this.filePath=filePath;
        this.clientPath=clientPath;
        this.version=version;
    }
}