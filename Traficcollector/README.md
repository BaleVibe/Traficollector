# Traficcollector

A comprehensive Android application for monitoring and collecting network traffic from selected applications using VPNService.

## Features

- **App Selection**: Choose any installed application to monitor its network traffic
- **Monitoring Modes**: 
  - Simple Mode: Basic connection information (endpoints, ports, protocols)
  - Full Mode: Complete packet data including raw bytes
- **Real-time Monitoring**: Live traffic collection and display
- **Traffic Analysis**: View detailed information about each network connection
- **Data Export**: Export collected data to CSV format
- **Clean UI**: Modern Material Design interface with intuitive navigation

## Technical Implementation

### Core Components

1. **VPNService**: Implements Android's VpnService to intercept network traffic
2. **Traffic Parser**: Analyzes network packets and extracts relevant information
3. **Data Storage**: In-memory storage with configurable limits
4. **UI Components**: Material Design with RecyclerView for efficient data display

### Network Traffic Collection

The app uses Android's VPNService to create a local VPN that intercepts network traffic. Key features:

- **IP Header Parsing**: Extracts source/destination IPs, ports, and protocols
- **Protocol Support**: Handles TCP, UDP, and ICMP protocols
- **Real-time Processing**: Processes packets as they flow through the VPN
- **Data Filtering**: Filters traffic based on selected application

### Data Model

```kotlin
data class TrafficData(
    val timestamp: Long,
    val sourceIp: String,
    val destIp: String,
    val sourcePort: Int,
    val destPort: Int,
    val protocol: String,
    val dataLength: Int,
    val direction: String, // OUTGOING or INCOMING
    val rawData: ByteArray? = null // Full mode only
)
```

## Installation

### Prerequisites

- Android 7.0 (API level 24) or higher
- VPN permissions (granted in-app)

### Build from Source

1. Clone the repository
2. Open in Android Studio
3. Build and run the project

Or use the command line:

```bash
./gradlew assembleDebug
```

## Usage

1. **Select Application**: Choose the app you want to monitor from the list of installed applications
2. **Choose Monitoring Mode**: Select Simple or Full mode depending on your needs
3. **Start Monitoring**: Grant VPN permission and begin traffic collection
4. **View Data**: Browse collected traffic data in real-time
5. **Export Results**: Export data to CSV for further analysis

## Permissions

The app requires the following permissions:

- `INTERNET`: Network access
- `ACCESS_NETWORK_STATE`: Check network connectivity
- `VPN_SERVICE`: Create VPN for traffic interception
- `FOREGROUND_SERVICE`: Run monitoring service in foreground
- `PACKAGE_USAGE_STATS`: Get installed applications list

## Security & Privacy

- **Local Processing**: All traffic processing happens locally on the device
- **No External Servers**: No data is sent to external servers
- **User Control**: User has full control over what data is collected
- **Transparent Operation**: Open-source code for complete transparency

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer     │    │  Business Logic  │    │   Data Layer    │
│                 │    │                  │    │                 │
│ - Activities    │◄──►│ - VPN Service    │◄──►│ - Traffic Store │
│ - Adapters      │    │ - Packet Parser  │    │ - App Cache     │
│ - ViewModels    │    │ - Data Manager   │    │ - Export Utils  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## GitHub Actions

The project includes automated workflows for:

- **Continuous Integration**: Run tests and lint on every push/PR
- **APK Generation**: Automatically build and release APKs
- **Artifact Storage**: Store build artifacts for download

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This application is designed for educational and testing purposes only. Users are responsible for ensuring compliance with applicable laws and regulations when monitoring network traffic. The developers are not responsible for any misuse of this application.# Repository structure fixed - Thu Nov 13 18:24:53 UTC 2025
# GitHub Actions test - Thu Nov 13 18:45:49 UTC 2025
