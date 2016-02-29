## Simple Data Types

type name | type description
----------|-----------------
uint8     | unsigned 8-bit integer
uint16    | unsigned 16-bit integer
uint32    | unsigned 32-bit integer
          |
int8      | signed 8-bit integer
int16     | signed 16-bit integer
int32     | signed 32-bit integer
          |
long      | java long type

All values are in big endian byte order.

## Complex Data Types

```
string {
  uint16 length;
  char[length];
}
```

# Discovery Protocol
---
Each robot binds to multicast group 238.160.102.2 (randomly chosen from random.org) port 25601. When it receives a UDP packet containing only the string "find robots" (not terminated in any way), it will respond with its name (in my implementation, the hostname of the machine).


# Control Protocol
---
Uses TCP port 25600

The TCP steam contains a sequence of packets. The first two bytes of a packet indicate the length of a packet, excluding the two length bytes and the type byte. The next byte indicates the type of packet. The length field is not actually necessary, but it makes it a whole lot easier to program the Java side of the protocol.

## Driver -> Robot Packets
#### 0x01: Keep Alive

    long time; // timestamp in milliseconds of when packet was sent
    
  Robot should respond with a **0x01 (Keep Alive)** packet containing the same timestamp

#### 0x02: Query Subsystems

    //no fields

  Robot should respond with a **0x02 (Subsystem Info)** packet and a **0x07 (Battery Info)** packet

#### 0x03: Register Driver

    string name; //name of driver. In my desktop driver, this is the username of the currently logged in user

  This packet tells the robot who the driver is. It must be sent before binding any subsytems. Triggers no response from robot.
  
#### 0x04: Bind Subsystem

    uint16 id; //number of subsystem in array from the 0x02 (Subsystem Info) packet

  Bind the selected subsystem. If it does not exist or is already bound by someone else, the robot will send a **0x03 (Subsystem Bind Failiure)** packet. Otherwise, it will send a **0x04 (Subsystem Bind Success)** packet

#### 0x05: Subsystem Update

    uint16 id; //the id of the subsystem being updated
    byte[] data;

  The rest of the packet depends upon the type of subsystem being updated. If a driver tries to update a subsystem that they do not have bound, or the specified subsystem does not exist, the packet is silently ignored.
  
##### 0x01: Skid Steer Drive

    int16 left; //speed of left motor, ranges from -2048 (full reverse) to 2048 (full forward)
    int16 right; //speed of right motor, same range

  Updates the speed for both drive motors

##### 0x02: Light

    uint8; //0: off, 1: on

#### 0x06: Unbind Subsytem

    uint16 id; //the id of the subsystem being unbound

## Robot -> Driver Packets

#### 0x01: Keep Alive

    long time; //timestamp in milliseconds of when original packet was sent

  Used to make sure the connection is still alive
#### 0x02: Subsystem Info

    uint16	length; //length of following array
    subsystem_t	systems[length];

    struct subsystem_t {
      uint8 type; // 0: invalid, 1: skid steer drive, 2: lights
      string name;
      string driver; //name of the current driver. empty string if the subsystem is not bound
    }

  Used to tell the driver software which subsystems are available for binding
  
#### 0x03: Subsystem Bind Failiure

    uint16 subsystem; //the subsystem that didn't get bound
    
  Sent when a drivers tries to bind a subsystem that either doesn't exist or is already bound by another driver
 
#### 0x04: Subsystem Bind Success
 
    uint16 subsystem; //the subsystem that got bound
    
  Sent when a driver's subsystem bind request is accepted.
  
#### 0x05: Battery Update

    uint16 length; //number of batteries in the robot
    int16 charge[length]; //array of battery charges in millivolts
    
  Sent to each driver every few seconds.
  
#### 0x06: Subsystem Update

    uint8 type; //type of subsystem
    uint16 id; //index of subsystem in 0x02 packet list

 The rest of the packet depends upon the type of the subsystem.
 
##### 0x01: Skid Steer Drive
 
    int16 left_current; // current draw from left motor in amps
    int16 left_temperature; // temperature of left driver in degrees celsius
    int16 right_current; //current draw from right motor in amps
    int16 right_temperature; // temperature of right driver in degrees celsius
    
  Sent to each driver a few times per second.
  
##### 0x02: Lights

    int16 current; // current draw from lamp
    int16 temperature; // temperature of the driver in degrees celsius
    
  Sent to each driver about once per second
#### 0x07: Battery Info

    uint16 length; //number of batteries, length of following array
    battery_t batteries[length];
    
    struct battery_t {
      int16 full_voltage; // millivolts
      int16 current_voltage; // millivolts
      string name;
    }
    
  List of batteries installed on the robot, sent to every driver when they request the list of subsystems.

#### 0x08: Log Message

    string message;
