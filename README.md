
# Template: NetSuite to Salesforce account to customer Broadcast

Broadcast changes or when accounts are created in NetSuite as customers to Salesforce in real time. The detection criteria, and fields that should be moved are configurable. Additional systems can be added to be notified of the changes. Real time synchronization is achieved via rapid polling of NetSuite. This template uses both Mule batching and watermarking capabilities to ensure that only recent changes are captured, and to efficiently process large amounts of records.

![f9fe78ce-fe30-4c0e-aacb-0c4e88a05519-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/f9fe78ce-fe30-4c0e-aacb-0c4e88a05519-image.png)

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
This template serves as a foundation for setting an online sync of customers in NetSuite instance to accounts from Salesforce instance. Every time there is a new customer or a change in an existing one, the integration polls for changes in NetSuite source instance and it's responsible for creating or updating the account in Salesforce target instance.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this template leverages the Batch Module.

The batch job is divided in *Process* and *On Complete* stages.

The integration is triggered by a scheduler defined in the flow that is going to trigger the application, querying newest NetSuite updates or creations matching a filter criteria and executing the batch job.

During the *Process* stage, for each customer from NetSuite instance, we try to find already created account in Salesforce instance according to account's name.
The data are adapted for upserting the account in Salesforce and call the upsert operation in Salesforce system.

Finally, during the *On Complete* stage the template logs output statistics data into the console.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source and destination systems, that must be made for all to run smoothly. 
Failing to do so could lead to unexpected behavior of the template.



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>


### As a Data Destination

There are no considerations with using Salesforce as a data destination.




## NetSuite Considerations

### As a Data Source

There are no considerations with using NetSuite as a data origin.





# Run it!
Simple steps to get NetSuite to Salesforce account to customer Broadcast running.


## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.


### Running on Mule Standalone

Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Running on CloudHub

While creating your application on CloudHub (or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to Configure** as well as the **mule.env**. 

Once your app is all set and started, there is no need to do anything else. Every time a customer is created or modified, it automatically synchronizes to an account in Salesforce as long as it has a Name.


### Deploying your template on CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.

### Application Configuration

**Batch Aggregator Configuration**
+ page.size `1000`

**Scheduler configuration**
+ scheduler.frequency `20000`
+ scheduler.start.delay `1000`

**Watermarking default last query timestamp**
+ watermark.default.expression `2016-12-13T03:00:59Z`

**Salesforce Connector Configuration**
+ sfdc.username `bob.dylan@orga`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

**NetSuite Connector Configuration**
+ nets.email `example@organization.com`
+ nets.password `Password123`
+ nets.account `NetSuiteaccount`
+ nets.roleId `3`
+ nets.applicationId `generatedApplicationId`
+ nets.customer.subsidiary.internalId `1`

**Note**: the property `nets.customer.subsidiary.internalId` set **subsidiary** for every new customer in NetSuite instance.

# API Calls
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The template calls to the API can be calculated using the formula:

***X + X / ${page.size}***

***X*** the number of accounts to be synchronized on each run. 

Divide by ***${page.size}*** because by default, accounts are gathered in groups of ${page.size} for each Upsert API Call in the commit step. Also consider that this calls are executed repeatedly every polling cycle.	

For instance if 10 records are fetched from origin instance, then 11 API calls are made (10 + 1).


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.

## businessLogic.xml
Functional aspect of the template is implemented on this XML, directed by one flow that polls for NetSuite creations or updates.
For the purpose of this particular template the *mainFlow* uses a Batch Job, which handles all the logic of it.

The several message processors constitute four high level actions that fully implement the logic of this template:
1. The template gets the customers polled from the NetSuite instance which matched the filter criteria and for each customer from NetSuite instance the data are adapted for upserting the account in Salesforce.
2. Then for each customer from NetSuite instance, we try to find already created account in Salesforce instance according to account's name. The data are adapted for upserting the account in Salesforce and call the upsert operation in Salesforce system.
3. Finally the template logs output statistics data into the console.

## endpoints.xml
This file contains the Scheduler endpoint that periodically queries NetSuite for updated or created customers that meet the defined criteria in the query, and then executing the batch job process, where the data serve as input.

## errorHandling.xml
This file handles how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.
