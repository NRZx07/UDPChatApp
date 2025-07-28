🗨️ UDP Chat Application
A simple client-server based chat application that uses UDP (User Datagram Protocol) for sending and receiving messages. This app demonstrates basic socket programming using datagram sockets, ideal for understanding real-time communication over unreliable transport.

📌 Features
Peer-to-peer chat over local network

Non-blocking message receive/send using threading

Lightweight and fast (no connection overhead like TCP)

Simple terminal/console UI

🛠️ Technologies Used
Programming Language: C / Java / Python (edit based on your app)

Networking: UDP Sockets

Threads: For simultaneous send & receive (if implemented)

🚀 Getting Started
🔧 Prerequisites
Make sure you have the necessary compiler/interpreter installed.

For C:

bash
Copy
Edit
gcc udp_server.c -o server
gcc udp_client.c -o client
For Python:

bash
Copy
Edit
python udp_server.py
python udp_client.py
▶️ Run the App
Start the Server:

bash
Copy
Edit
./server
or for Python:

bash
Copy
Edit
python udp_server.py
Start the Client:

bash
Copy
Edit
./client
or for Python:

bash
Copy
Edit
python udp_client.py
Make sure both server and client are on the same network (or use appropriate IP).

🧠 How It Works
Server creates a UDP socket and binds to a specific port.

Client sends datagrams to the server's IP and port.

Messages are exchanged via UDP packets (connectionless).

Threading is used to allow asynchronous send and receive.

📂 Project Structure
pgsql
Copy
Edit
udp-chat-app/
├── server.c / server.py
├── client.c / client.py
└── README.md
⚠️ Limitations
UDP is unreliable — no guarantee of message delivery or order.

No encryption (not secure for sensitive data).

Works best on local networks.

✅ Future Improvements
GUI using Tkinter / JavaFX / Qt

Add username handling

Use TCP for reliable delivery

File transfer over UDP

👨‍💻 Author 
NIRAJ JHA
