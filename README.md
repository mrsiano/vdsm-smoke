# vdsm-smoke

The aim is to provide performence testing capabilities to vdsm without using ovirt engine.

In order to implement a use case we can provide implementation of Scenerio class and configuration for it in yaml file.
Each scenario implementation represents single vdsm verb call. By using scenarios propery in yaml configuration file
we can chain scenarios to cover a use case like create and later remove a virtual machine.

After using maven to build far jar we can run it with:

java -jar smoke-jar-with-dependencies.jar <options>

Here are possible options:

 -f,--path <arg>                Path to a configuration yaml file for the scenario we want to test
 
 -h,--host <arg>                Host where vdsm is running
 
 -p,--port <arg>                Port on which vdsm is listening
 
 -l,--location <arg>            ssl configuration for either engine or vdsm so we can reuse certificates (based on -s)
 
 -m,--metric <arg>              A directory where scenario runtime metrics are stored as csv file
 
 -n,--number-of-threads <arg>   Specifies number of concurrent exceutions for a scenario
 
 -r,--repeat <arg>              Specifies how many times repeat a scenario
 
 -s,--secure <arg>              Specify whether to use engine or vdsm config to load certificates
 
 -t,--time <arg>                Specify for how long the scenario should be run in minutes
 
 In order to understand how performant vdsm is with current scenario we use dropwizard metrics to collect response times
 for each verb invocation. It is very easy to change what metrics can be collected and how we want to store them.
