package entity;

/**
 * Entity class representing a cinema seat
 */
public class Seat {
    private int seatID;
    private int roomID;
    private String seatRow;
    private String seatNumber;
    private String seatType; // Standard, VIP, Couple, Disabled
    private String status; // Available, Booked, Maintenance, Unavailable
    
    public Seat() {
    }
    
    public Seat(int seatID, int roomID, String seatRow, String seatNumber, 
                String seatType, String status) {
        this.seatID = seatID;
        this.roomID = roomID;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
    }
    
    // Getters and Setters
    public int getSeatID() {
        return seatID;
    }
    
    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }
    
    public int getRoomID() {
        return roomID;
    }
    
    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }
    
    public String getSeatRow() {
        return seatRow;
    }
    
    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public String getSeatType() {
        return seatType;
    }
    
    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSeatLabel() {
        return seatRow + seatNumber;
    }
    
    @Override
    public String toString() {
        return "Seat{" +
                "seatID=" + seatID +
                ", roomID=" + roomID +
                ", seatRow='" + seatRow + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatType='" + seatType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

