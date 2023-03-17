import socket

def main():
    # Connect to the server
    host = 'localhost'
    port = 1337
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((host, port))
    print(f'Connected to {host}:{port}')

    # Send the user's name to the server
    name = input('Enter your name: ')
    client_socket.send(name.encode())

    # Receive messages from the server
    while True:
        message = client_socket.recv(1024).decode()
        if not message:
            break
        print(message)

    # Close the connection
    client_socket.close()
    print('Connection closed')

if __name__ == '__main__':
    main()
