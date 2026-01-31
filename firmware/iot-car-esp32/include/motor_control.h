#include <Arduino.h>
#ifndef MOTOTR_CONTROL_H // this is why we use include guard, that mean to prevent multiple inclusions
#define MOTOTR_CONTROL_H

// Motor A (Left Motor) - Represented by LEDs in simulation this is equal to motor control pins of L298N motor driver
#define MOTOR_A_IN1 25 // Green LED - Forward
#define MOTOR_A_IN2 26 // Red LED - Reverse

// Motor B (Right Motor) - Represented by LEDs in simulation
#define MOTOR_B_IN3 27 // Green LED - Forward
#define MOTOR_B_IN4 14 // Red LED - Reverse


void stopMotors()
{
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, LOW);
}

void setupMotors()
{
    pinMode(MOTOR_A_IN1, OUTPUT);
    pinMode(MOTOR_A_IN2, OUTPUT);
    pinMode(MOTOR_B_IN3, OUTPUT);
    pinMode(MOTOR_B_IN4, OUTPUT);

    // Initialize all motors to stopped
    stopMotors();
}

void moveForward()
{
    digitalWrite(MOTOR_A_IN1, HIGH);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, HIGH);
    digitalWrite(MOTOR_B_IN4, LOW);
    
    Serial.println("[MOTOR] Moving Forward");
}

void moveBackward()
{
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, HIGH);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, HIGH);
    
    Serial.println("[MOTOR] Moving Backward");
}


void turnLeft()
{
    digitalWrite(MOTOR_A_IN1, LOW);
    digitalWrite(MOTOR_A_IN2, HIGH);
    digitalWrite(MOTOR_B_IN3, HIGH);
    digitalWrite(MOTOR_B_IN4, LOW);
    
    Serial.println("[MOTOR] Turning Left");
}



void turnRight()
{
    digitalWrite(MOTOR_A_IN1, HIGH);
    digitalWrite(MOTOR_A_IN2, LOW);
    digitalWrite(MOTOR_B_IN3, LOW);
    digitalWrite(MOTOR_B_IN4, HIGH);
    
    Serial.println("[MOTOR] Turning Right");
}

#endif // MOTOTR_CONTROL_H