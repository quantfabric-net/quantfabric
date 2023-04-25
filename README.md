# quantfabric
Welcome to our open source platform for quantitative trading different assets like crypto, FX, and other assets. Our platform is designed to help traders analyze market data, develop trading strategies, and automate trading using a variety of assets.

## Features
Our platform offers the following features:

- **Market data:** We provide real-time market data for different assets, including crypto, FX, and other assets.
- **Data analysis tools:** Our platform offers a variety of data analysis tools to help traders analyze market data and develop trading strategies.
- **Backtesting:** Traders can backtest their trading strategies using historical market data to see how they would have performed in the past.
- **Automated trading:** Our platform supports automated trading using a variety of trading bots that can execute trades based on predefined trading strategies.
- **Customization:** Traders can customize the platform to their specific needs by adding their own data sources, trading bots, and analysis tools.

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
