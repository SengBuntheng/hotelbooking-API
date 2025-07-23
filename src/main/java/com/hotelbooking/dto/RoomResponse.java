        package com.hotelbooking.dto;

        import com.fasterxml.jackson.annotation.JsonProperty;
        import com.hotelbooking.Enum.RoomStatus;

        import java.time.LocalDateTime;

        public class RoomResponse {
            private Long id;
            @JsonProperty("Room_number")
            private String roomNumber;
            @JsonProperty("status")
            private RoomStatus status;
            @JsonProperty("roomId")
            private Long roomTypeId;
            @JsonProperty("Createat")
            private LocalDateTime createdAt;
        }
