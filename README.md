![Logo](https://github.com/cuadratico/NowiLAN/blob/master/src/main/chat_back-playstore.png)


# NowiLAN
A simple app to establish connections via the local communication protocol **TCP**.

# Important!
The mentioned libraries do **not** use a hybrid encryption protocol like TLS or the Signal Protocol to encrypt TCP or UDP connections.

# Connection with Linux?
To generate **TCP** connections you will need to install the **Netcat** package.

## How to install **net-tools** and **Netcat** on Linux
  ```bash
    sudo apt install net-tools
  ```
  ```bash
    sudo apt install netcat-openbsd 
  ```

## My private ip?
The most recommended is to use the **ip addr** command, but if you want you can use the **ifconfig** command after downloading the **net-tools** package and go down to the network interface wlan0 and find the private IP according to the [IPv4](https://www.ibm.com/docs/es/networkmanager/4.2.0?topic=translation-private-address-ranges) protocol.

```bash
ifconfig
```

## Establish connection with NowiLAN from Linux
Once the **Netcat** package is installed you will have to use the keyword **nc** together with different parameters to generate a **TCP** server (this is an example and does not have to be the recommended way).

```bash
  nc -l -p 9090
```

## Explanation of the operation
**nc** is a Linux package that allows you to create TCP and UDP connections.

## What are the other operators?
This operator is used to indicate to the next operation that you want to open the port you specify with **-p** (It is not recommended to work with ports below **1024** since privileges are required).

```bash
nc -l
```

## Connect to a device via TCP on Linux
To be able to connect to a device via TCP on a local network you will need the private IP of the device on that local network and the port that it has opened (this is an example, the private IP is not real and the port does not have to be the recommended one — and remember to replace **100.100.100.100** with the private IP to analyze).

```bash
  nc 100.100.100.100 9090
```

## Explanation of the operation
When using the **nc** command by default without operators you need to specify two parameters.
According to how TCP works, the connection with the private IP (the device) is established first and then the connection is established with the port that this device has open.

## Is it possible to create connections without knowing the port
Yes — to do this you would need to perform a port sweep on the device with the [Nmap](https://nmap.org/) tool. Although if you don't want to check the documentation the easiest is a simple command for a quick check of the private IP (replace **100.100.100.100** with the private IP to analyze)

```bash
sudo apt install nmap
´´´

```bash
sudo nmap 100.100.100.100 
```
