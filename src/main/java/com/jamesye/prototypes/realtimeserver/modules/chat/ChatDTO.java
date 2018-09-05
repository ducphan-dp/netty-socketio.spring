package com.jamesye.prototypes.realtimeserver.modules.chat;

public class ChatDTO {

    private String userName;
    private String message;
    private String roomName;

    public ChatDTO() {
    }

    public ChatDTO(String userName, String message, String roomName) {
        super();
        this.userName = userName;
        this.message = message;
        this.roomName = roomName;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return "ChatDTO{" +
                "userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                ", roomName='" + roomName + '\'' +
                '}';
    }
}
