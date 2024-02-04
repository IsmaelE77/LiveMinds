### intro
This project is a Spring Boot application that provides a platform for
streaming university lectures using Livekit server. The platform allows
lecturers to create and manage lectures, and students to attend and watch
lectures.
Features:
- Lecturers can create and manage lectures, including setting the lecture title,
description, and time.
- Lecturers can control the room by muting, changing permissions, and kicking
participants.
- Students can view a list of upcoming lectures and register to attend.
- Students can participate in the lecture via audio, video, or chat.

### [livekit](https://livekit.io)
LiveKit is an open source alternative to Agora or Twilio,
Build live video and audio applications using a modern, end-to-end WebRTC stack.
[docs](https://docs.livekit.io/realtime/)

### Liveminds
Livekit server provides server-side libraries that allow the developer to control Livekit using API key and secret.
Liveminds manages the broadcast and full permissions. 
#### This is an explanation of some use cases.
##### login user and connect to room:
![image](https://github.com/IsmaelE77/LiveMinds/assets/93754014/b16c1feb-13bc-4705-81f5-a6ffba0858ff)
##### create room (if user have perrimsion):
![image](https://github.com/IsmaelE77/LiveMinds/assets/93754014/04be3b0a-6147-4dd2-9067-324ab91d27f7)
##### manage room : mute , expel , change bordcasting perrmision (if the user is the owner of this room):
![image](https://github.com/IsmaelE77/LiveMinds/assets/93754014/59ad64c8-cbc2-4ad1-9de5-2e37c513dc17)

#### documentation
The server provides a restful API service for all the required services,
and the documents can be viewed at the following link (it may be slow to open because hosting is free):
[documentation](https://liveminds.onrender.com/swagger-ui/index.html)


