import socket
import threading
listen = True

def listen_to_respone(clientSocket):
    while listen:
        data = clientSocket.recv(1024).decode()
        print(data.strip())

def start_client():
    host = "127.0.0.1"
    port = 5050
    client_socket = socket.socket()
    client_socket.connect((host, port))
    thread = threading.Thread(target = listen_to_respone, args = (client_socket, ))
    thread.start()
    print("Type: '/help' or '/?' for help")
    print("Enter your name: ", end='')
    name = "[NOTICE_USER_NAME]" + input()
    client_socket.send(name.encode())
    req = ""
    while req.lower().strip() != 'exit':
        req = input("")
        client_socket.send(req.encode())
    client_socket.close()
    listen = False

if __name__ == "__main__":
    start_client()