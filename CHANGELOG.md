# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Visioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.0] - Exchanges connector, project structure
### Added
- [#158](/../../issues/158) Development environment setup guide
- [#161](/../../issues/161) New source of token definitions for Etherdelta connector
- Spring retry listener for logging
### Changes
- [#155](/../../issues/155) Rename Core to Common module, add Common Kafka module
### Fixed
- Dockerfiles for modules


## [0.2.0] - Exchanges connector
### Added
- [#151](/../../issues/151) Base monitoring using grafana and prometheus
### Changes
- [#151](/../../issues/151) Exchange connectors implementation using xchange-stream lib
- [#151](/../../issues/151) Implement Etherdelta connector using web3j
- Refactor trade/order classes and cassandra tables
### Fixed
- CirceCi configuration for new modules


## [0.1.0] - Tickers, REST api
### Added
- Spring ecosystem

[Unreleased]: https://github.com/cybercongress/cyber-markets/master
[0.1.0]: https://github.com/cybercongress/cyber-markets/releases/tag/0.1.0
[0.2.0]: https://github.com/cybercongress/cyber-markets/releases/tag/0.2.0
[0.3.0]: https://github.com/cybercongress/cyber-markets/releases/tag/0.3.0