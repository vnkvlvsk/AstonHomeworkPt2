package Homework5.service;

import Homework5.event.OperationType;

public interface EmailService {
    void sendUserEventNotification(String email, OperationType operation);
}
