package com.iven.memo.models.Message;

import com.iven.memo.models.Enumerate.WSMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WSMessage<T> {
    private WSMessageType messageType;
    private T message;
}
