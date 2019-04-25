# JConfig
Java distributed real time configuration system that is designed to scale well for multiple cpus machines and reduce contention when using large number of threads.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Development](#development)
- [Contribute](#contribute)
- [License](#license)

## Background
Java distributed real time configuration system that is designed to scale well for multiple cpus machines and reduce contention when using large number of threads.
More detailed document is [here](https://github.com/lafaspot/jconfig/wiki)

## Usage

```
sh jconfig-daemon.sh [-c configDirectoryPath -l libDirectoryPath -p logDirectoryPath] (start|stop) 

where: 
    -c  JCONFIG loader conf directory path (required as arg or set it as env var)
    -l  lib directory path  (required as arg or set it as env var)
    -p  log directory path (optional. If not specified, logs will be written in libs directory)
    start - start ConfigLoader application 
    stop - stop ConfigLoader application. 
```

You can also specify config/lib/log directory path in form of environment variables. 
If both are set, priority will be given to arguments passed while running script. 
   {JCONFIG_CDIR}  JCONFIG loader conf directory path. 
   {JCONFIG_HOME}  ConfigLoader lib directory path. This is the path for all dependency jars. 
   {JCONFIG_LOG_DIR} ConfigLoader log directory path.     

NuclearApp in jconfig-test package consumes config via JCONFIG. If config loader is not running, it will run with defaults specified
in NuclearConfig class file.

## Development

To build before you submit a PR
```
$ mvn clean install
```

For contibutors, run deploy to do a push to nexus servers
```
$ mvn clean deploy -Dgpg.passphrase=[pathPhrase]
```

All pull requests need to pass continous integration before being merged.
Please go to https://travis-ci.org/lafaspot/jconfig
  
## Contribute

Please refer to the [contributing.md](Contributing.md) for information about how to get involved. We welcome issues, questions, and pull requests. Pull Requests are welcome.

## Maintainers

Luis Alves: (lafa at verizonmedia.com)

## License

This project is licensed under the terms of the [Apache 2.0](LICENSE-Apache-2.0) open source license. Please refer to [LICENSE](LICENSE) for the full terms.
