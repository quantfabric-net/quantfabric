# quantfabric
Welcome to our open source platform for quantitative trading multiple asset classes, i.e. crypto, FX, and equity. The platform is designed to connect to multiple sources of market data, develop trading strategies and execute trades - all with lowest latency possible.
## Features
Our platform offers the following features:

- **Market data:** Low-latency market data connectivity for different asset classes, including crypto, FX, and other.
- **Analytics:** Pre-built statistical, ML and data engineering functions, including proprietary order book indicators.
- **Backtesting:** Tick data replay for realistic strategy backtesting.
- **Execution:** Full execution lifecycle management.
- **Low-latency/high-throughput:** It is designed using low-latency Java techniques to ensure the lowest end-to-end latency and highest throughput possible.

## Installation

To install our platform, you will need to have Java and Docker installed on your system. Follow these steps to get started:

1. Clone the repository from Github using the following command:
```bash
git clone https://github.com/quantfabric-net/quantfabric.git
```

2. Build the Docker image using the following command:
```
./DockerBuild.sh
```
3. Run the Docker container using the following command:
```
docker run quantfabric -v config:/opt/quantfabric/config
```

## Getting Started
To get started with our platform, follow these steps:

1. Configure connectors to desired market provider.
2. Choose the assets you want to trade and configure your market data pipelines.
3. Analyze market data using our data analysis tools and develop trading strategies.
4. Backtest your trading strategies using historical market data.
5. Configure your strategy runner and start automated trading.

## Contributing
We welcome contributions to our open source platform. If you're interested in contributing, please follow these steps:

Fork the repository on Github.

Make your changes and test them thoroughly.

Submit a pull request with a detailed description of your changes.

## Support
If you have any questions or issues with our platform, please contact us . We're happy to help!
