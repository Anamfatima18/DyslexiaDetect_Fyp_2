
package com.example.frontend;
public class DataPart {
    private String fileName;
    private byte[] content;
    private String type;

    public DataPart(String name, byte[] data, String mimeType) {
        fileName = name;
        content = data;
        type = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}
