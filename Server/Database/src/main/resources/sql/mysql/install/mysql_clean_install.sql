-- MySQL dump 10.13  Distrib 5.1.53, for apple-darwin10.5.0 (i386)
--
-- Host: localhost    Database: drac
-- ------------------------------------------------------
-- Server version 5.1.53-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


--
-- Drop existing drac db
--
drop database if exists drac;

--
-- Create drac db and user
--
CREATE DATABASE IF NOT EXISTS drac CHARACTER SET utf8 COLLATE utf8_bin;
GRANT ALL PRIVILEGES ON drac.* TO 'drac'@'localhost' IDENTIFIED BY 'draC20056';

USE drac;
--
-- Create schema drac



--
-- Table structure for table `drac`.`GlobalPolicy`
--

DROP TABLE IF EXISTS `drac`.`GlobalPolicy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`GlobalPolicy` (
  `id` varchar(5) COLLATE utf8_bin NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`GlobalPolicy`
--

LOCK TABLES `drac`.`GlobalPolicy` WRITE;
/*!40000 ALTER TABLE `GlobalPolicy` DISABLE KEYS */;
INSERT INTO `drac`.`GlobalPolicy` VALUES ('key','<globalPolicy>\n<userAccountPolicy>\n<localPasswordPolicy>\n<passwordAging>-1</passwordAging>\n<passwordExpirationNotification>0</passwordExpirationNotification>\n<passwordHistorySize>0</passwordHistorySize>\n<invalidPasswords></invalidPasswords>\n<passwordRules>%21%27%23%24%25%28%29*%2B-.%2F%3D%3C%3E%40%5B%5D%5E%7B%7C%7D%7E_Password+min+length%3A6%3BMin+Alpha%3A1%3BMin+Digit%3A1%3BMin+Special%3A1%3BMin+Different%3A2%3BMixed+Alpha%3Ayes%3B</passwordRules>\n</localPasswordPolicy>\n<dormantPeriod>90</dormantPeriod>\n<inactivityPeriod>900</inactivityPeriod>\n<maxInvalidLoginAttempts>3</maxInvalidLoginAttempts>\n<lockoutPeriod>600</lockoutPeriod>\n<lockedClientIPs></lockedClientIPs>\n</userAccountPolicy>\n<groupPolicy>\n</groupPolicy>\n<resourcePolicy><bandwidthControlRule ruleID=\"1158780889702\">\n<maximumServiceSize>100000</maximumServiceSize>\n<maximumServiceDuration>900000</maximumServiceDuration>\n<maximumServiceBandwidth>900000000</maximumServiceBandwidth>\n<maximumAggregateServiceSize></maximumAggregateServiceSize>\n</bandwidthControlRule>\n</resourcePolicy>\n<authenticationList>\n<authenticationType type=\"Internal\" supported=\"true\"></authenticationType>\n<authenticationType type=\"A-Select\" supported=\"true\"></authenticationType>\n<authenticationType type=\"RADIUS\" supported=\"false\"></authenticationType>\n</authenticationList>\n<preReservationConfirmationTimeout>10</preReservationConfirmationTimeout>\n<scheduleProvisioningOffset>10</scheduleProvisioningOffset>\n</globalPolicy>');
/*!40000 ALTER TABLE `drac`.`GlobalPolicy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`LightPathAlarmDetails`
--

DROP TABLE IF EXISTS `LightPathAlarmDetails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`LightPathAlarmDetails` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `neid` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  `time` bigint(20) NOT NULL,
  `duration` bigint(20) NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `duration` (`duration`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`LightPathAlarmDetails`
--

LOCK TABLES `drac`.`LightPathAlarmDetails` WRITE;
/*!40000 ALTER TABLE `drac`.`LightPathAlarmDetails` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`LightPathAlarmDetails` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Definition of table `drac`.`LightPathEdge`
--
CREATE TABLE  `drac`.`LightPathEdge` (
  `pk` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id` varchar(28) DEFAULT NULL,
  `source` varchar(25) NOT NULL,
  `sourceAid` varchar(28) NOT NULL,
  `sourceChannel` mediumint(9) NOT NULL,
  `target` varchar(25) NOT NULL,
  `targetAid` varchar(28) NOT NULL,
  `targetChannel` mediumint(9) NOT NULL,
  `rate` varchar(8) NOT NULL,
  `CCT` varchar(28) DEFAULT NULL,
  `SWMATE` varchar(28) DEFAULT NULL,
  `mep` varchar(255) NOT NULL,
  `vlanId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`pk`),
  KEY `id` (`id`),
  KEY `source` (`source`,`sourceAid`),
  KEY `target` (`target`,`targetAid`)
) ;

--
-- Table structure for table `drac`.`LightPath`
--

DROP TABLE IF EXISTS `drac`.`LightPath`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`LightPath` (
  `serviceId` varchar(28) COLLATE utf8_bin NOT NULL,
  `id` varchar(28) COLLATE utf8_bin NOT NULL,
  `scheduleId` varchar(28) COLLATE utf8_bin NOT NULL,
  `activationType` varchar(255) COLLATE utf8_bin NOT NULL,
  `controllerId` varchar(255) COLLATE utf8_bin NOT NULL,
  `scheduleName` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` varchar(4) COLLATE utf8_bin NOT NULL,
  `vcat` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `startTime` bigint(20) NOT NULL,
  `endTime` bigint(20) NOT NULL,
  `user` varchar(255) COLLATE utf8_bin NOT NULL,
  `billingGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `priority` varchar(3) COLLATE utf8_bin NOT NULL,
  `mbs` int(11) NOT NULL,
  `rate` varchar(8) COLLATE utf8_bin NOT NULL,
  `aEnd` varchar(25) COLLATE utf8_bin NOT NULL,
  `zEnd` varchar(25) COLLATE utf8_bin NOT NULL,
  `lp_1plus1_path_data` mediumblob,
  `xml` varchar(5000) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`serviceId`),
  KEY `id` (`id`),
  KEY `scheduleId` (`scheduleId`),
  KEY `startTime` (`startTime`),
  KEY `endTime` (`endTime`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`LightPath`
--

LOCK TABLES `drac`.`LightPath` WRITE;
/*!40000 ALTER TABLE `drac`.`LightPath` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`LightPath` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`NetworkElementAdjacency`
--

DROP TABLE IF EXISTS `drac`.`NetworkElementAdjacency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`NetworkElementAdjacency` (
  `pk` varchar(255) COLLATE utf8_bin NOT NULL,
  `neid` varchar(255) COLLATE utf8_bin NOT NULL,
  `port` varchar(255) COLLATE utf8_bin NOT NULL,
  `txtag` varchar(255) COLLATE utf8_bin NOT NULL,
  `rxtag` varchar(255) COLLATE utf8_bin NOT NULL,
  `type` varchar(255) COLLATE utf8_bin NOT NULL,
  `manualProvision` tinyint(1) NOT NULL,
  PRIMARY KEY (`pk`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`NetworkElementAdjacency`
--

LOCK TABLES `drac`.`NetworkElementAdjacency` WRITE;
/*!40000 ALTER TABLE `drac`.`NetworkElementAdjacency` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`NetworkElementAdjacency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NetworkElementConnection`
--

DROP TABLE IF EXISTS `drac`.`NetworkElementConnection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`NetworkElementConnection` (
  `pk` varchar(255) COLLATE utf8_bin NOT NULL,
  `id` varchar(64) COLLATE utf8_bin NOT NULL,
  `data` mediumblob NOT NULL,
  PRIMARY KEY (`pk`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`NetworkElementConnection`
--

LOCK TABLES `drac`.`NetworkElementConnection` WRITE;
/*!40000 ALTER TABLE `drac`.`NetworkElementConnection` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`NetworkElementConnection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`NetworkElementFacility`
--

DROP TABLE IF EXISTS `drac`.`NetworkElementFacility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`NetworkElementFacility` (
  `pk` varchar(64) COLLATE utf8_bin NOT NULL,
  `layer` varchar(8) COLLATE utf8_bin NOT NULL,
  `shelf` varchar(16) COLLATE utf8_bin NOT NULL,
  `slot` varchar(16) COLLATE utf8_bin NOT NULL,
  `port` varchar(16) COLLATE utf8_bin NOT NULL,
  `primaryState` varchar(16) COLLATE utf8_bin NOT NULL,
  `signalingType` varchar(16) COLLATE utf8_bin NOT NULL,
  `tna` varchar(64) COLLATE utf8_bin NOT NULL,
  `siteId` varchar(255) COLLATE utf8_bin NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`pk`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`NetworkElementFacility`
--

LOCK TABLES `drac`.`NetworkElementFacility` WRITE;
/*!40000 ALTER TABLE `drac`.`NetworkElementFacility` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`NetworkElementFacility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`NetworkElement`
--

DROP TABLE IF EXISTS `drac`.`NetworkElement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`NetworkElement` (
  `pk` varchar(255) COLLATE utf8_bin NOT NULL,
  `autoReDiscover` tinyint(1) NOT NULL,
  `commProtocol` varchar(255) COLLATE utf8_bin NOT NULL,
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `managedBy` varchar(255) COLLATE utf8_bin NOT NULL,
  `mode` varchar(255) COLLATE utf8_bin NOT NULL,
  `neIndex` int(11) NOT NULL,
  `password` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` varchar(255) COLLATE utf8_bin NOT NULL,
  `tid` varchar(255) COLLATE utf8_bin NOT NULL,
  `type` varchar(255) COLLATE utf8_bin NOT NULL,
  `userId` varchar(255) COLLATE utf8_bin NOT NULL,
  `subType` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT 'Unknown',
  `neRelease` varchar(255) COLLATE utf8_bin NOT NULL,
  `positionX` double,
  `positionY` double,
  PRIMARY KEY (`pk`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`NetworkElement`
--

LOCK TABLES `drac`.`NetworkElement` WRITE;
/*!40000 ALTER TABLE `drac`.`NetworkElement` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`NetworkElement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ResourceGroups`
--

DROP TABLE IF EXISTS `drac`.`ResourceGroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`ResourceGroups` (
  `groupId` varchar(255) COLLATE utf8_bin NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`groupId`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`ResourceGroups`
--

LOCK TABLES `drac`.`ResourceGroups` WRITE;
/*!40000 ALTER TABLE `drac`.`ResourceGroups` DISABLE KEYS */;
INSERT INTO `ResourceGroups` VALUES ('SystemAdminResourceGroup','<resourceGroup name=\"SystemAdminResourceGroup\" lastModificationUserID=\"admin\" creationDate=\"2000-08-02T18:12:12+02:00\" lastModifiedDate=\"2000-08-02T18:12:12+02:00\">\n<defaultResourceGroup>true</defaultResourceGroup>\n<resourceList></resourceList>\n<organization></organization>\n<resourcePolicy><resourceStateRule>closed</resourceStateRule><bandwidthControlRule ruleID=\"1287051815936\">\n<maximumServiceSize></maximumServiceSize>\n<maximumServiceDuration></maximumServiceDuration>\n<maximumServiceBandwidth></maximumServiceBandwidth>\n<maximumAggregateServiceSize></maximumAggregateServiceSize>\n</bandwidthControlRule>\n</resourcePolicy><membership>\n<createdByMemberName></createdByMemberName>\n<memberUserGroupName>SystemAdminGroup</memberUserGroupName>\n</membership>\n</resourceGroup>');
/*!40000 ALTER TABLE `drac`.`ResourceGroups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`Schedule`
--

DROP TABLE IF EXISTS `drac`.`Schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`Schedule` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `activationType` varchar(255) COLLATE utf8_bin NOT NULL,
  `name` varchar(255) COLLATE utf8_bin NOT NULL,
  `status` varchar(255) COLLATE utf8_bin NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `endTime` bigint(20) NOT NULL,
  `duration` bigint(20) NOT NULL,
  `userId` varchar(255) COLLATE utf8_bin NOT NULL,
  `sourceEndpointUserGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `targetEndpointUserGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `billingGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `sourceEndpointResourceGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `targetEndpointResourceGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `email` varchar(255) COLLATE utf8_bin NOT NULL,
  `recurrenceType` varchar(255) COLLATE utf8_bin NOT NULL,
  `recurrenceDay` int(11) NOT NULL,
  `recurrenceMonth` int(10) unsigned NOT NULL,
  `recurrenceWeekday` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_source` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_target` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_rate` mediumint(9) NOT NULL,
  `path_srlg` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_cost` int(11) NOT NULL,
  `path_metric` int(11) NOT NULL,
  `path_hop` int(11) NOT NULL,
  `path_vcatRoutingOption` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_sharedriskservicegroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_protection` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_NameStringValue` varchar(5000) COLLATE utf8_bin NOT NULL,
  `path_sourceendpoint_channel` int(11) NOT NULL,
  `path_sourceendpoint_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_sourceendpoint_tna` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_sourceendpoint_vlanid` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_targetendpoint_channel` int(11) NOT NULL,
  `path_targetendpoint_id` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_targetendpoint_tna` varchar(255) COLLATE utf8_bin NOT NULL,
  `path_targetendpoint_vlanid` varchar(255) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `startTime` (`startTime`),
  KEY `endTime` (`endTime`),
  KEY `duration` (`duration`),
  KEY `billingGroup` (`billingGroup`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`Schedule`
--

LOCK TABLES `drac`.`Schedule` WRITE;
/*!40000 ALTER TABLE `drac`.`Schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `drac`.`Schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`UserGroups`
--

DROP TABLE IF EXISTS `drac`.`UserGroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`UserGroups` (
  `groupId` varchar(255) COLLATE utf8_bin NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`groupId`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`UserGroups`
--

LOCK TABLES `UserGroups` WRITE;
/*!40000 ALTER TABLE `drac`.`UserGroups` DISABLE KEYS */;
INSERT INTO `drac`.`UserGroups` VALUES ('SystemAdminGroup','<userGroup name=\"SystemAdminGroup\" lastModificationUserID=\"admin\" creationDate=\"2000-08-02T18:12:12+02:00\" lastModifiedDate=\"2000-08-02T18:12:13+02:00\">\n<userGroupType>SystemAdministrator</userGroupType>\n<defaultUserGroup>false</defaultUserGroup>\n<organization></organization>\n<groupPolicy>\n<bandwidthControlRule ruleID=\"1159803989702\">\n<maximumServiceSize></maximumServiceSize>\n<maximumServiceDuration></maximumServiceDuration>\n<maximumServiceBandwidth></maximumServiceBandwidth>\n<maximumAggregateServiceSize></maximumAggregateServiceSize>\n</bandwidthControlRule>\n</groupPolicy><membership>\n<createdByMemberName></createdByMemberName>\n<memberUserID>admin</memberUserID>\n<memberResourceGroupName>SystemAdminResourceGroup</memberResourceGroupName>\n</membership>\n<referencingUserGroupName></referencingUserGroupName>\n</userGroup>');
/*!40000 ALTER TABLE `drac`.`UserGroups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drac`.`Users`
--

DROP TABLE IF EXISTS `drac`.`Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`Users` (
  `userId` varchar(255) COLLATE utf8_bin NOT NULL,
  `xml` mediumtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`userId`)
);
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`Users`
--

LOCK TABLES `drac`.`Users` WRITE;
/*!40000 ALTER TABLE `drac`.`Users` DISABLE KEYS */;
INSERT INTO `drac`.`Users` VALUES ('admin','<user userID=\"admin\" lastModifiedDate=\"2002-06-29T15:20:10+02:00\" creationDate=\"2002-05-30T15:20:10+02:00\"> <authentication> <authenticationType>Internal</authenticationType> <internalAccountData>\n<userPassword>292c2cdcb5f669a8</userPassword>\n<lastPasswordChanged>2002-05-30T15:20:10+02:00</lastPasswordChanged>\n<expirationDate>2013-05-30T15:20:10+02:00</expirationDate>\n<passwordHistory oldPassword=\"1c9df49b71e05b56c080fe58f480b657\" dateChanged=\"2002-05-30T15:30:10+02:00\"></passwordHistory>\n</internalAccountData> <authenticationState>valid</authenticationState> <lastAuthenticationStateChange>2010-12-21T00:30:50+01:00</lastAuthenticationStateChange> <userAccountPolicy>\n<localPasswordPolicy>\n<passwordAging>-1</passwordAging>\n<passwordExpirationNotification></passwordExpirationNotification>\n<passwordHistorySize></passwordHistorySize>\n<invalidPasswords></invalidPasswords>\n<passwordRules></passwordRules>\n</localPasswordPolicy>\n<dormantPeriod>0</dormantPeriod>\n<inactivityPeriod>900</inactivityPeriod>\n<maxInvalidLoginAttempts>3</maxInvalidLoginAttempts>\n<lockoutPeriod>300</lockoutPeriod>\n<lockedClientIPs></lockedClientIPs>\n</userAccountPolicy> <auditData>\n<lastLoginAddress>localhost,0:0:0:0:0:0:0:1%0:64459,0:0:0:0:0:0:0:1%0:65518,127.0.0.1</lastLoginAddress>\n<numOfInvalidAttempts>0</numOfInvalidAttempts>\n<locationOfInvalidAttempts></locationOfInvalidAttempts>\n</auditData> <wsdlCredential>292c2cdcb5f669a8</wsdlCredential></authentication> <accountStatus> <accountState>enabled</accountState> <disabledReason></disabledReason> </accountStatus> <userData><commonName>admin</commonName>\n<givenName>admin</givenName>\n<surname></surname>\n<title></title>\n<telephoneNumber></telephoneNumber>\n<mail></mail>\n<postalAddress></postalAddress>\n<description></description>\n</userData> <organization>\n<description></description>\n<organizationName></organizationName>\n<organizationalUnitName></organizationalUnitName>\n<owner></owner>\n<seeAlso></seeAlso>\n<businessCategory></businessCategory>\n</organization> <membership>\n<createdByMemberName></createdByMemberName>\n<memberUserGroupName>SystemAdminGroup</memberUserGroupName>\n</membership> <preferences>\n<timeZoneId>Europe/Amsterdam</timeZoneId>\n</preferences></user>');
/*!40000 ALTER TABLE `drac`.`Users` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Definition of table `drac`.`UserPreferences`
--
DROP TABLE IF EXISTS `drac`.`UserPreferences`;
CREATE TABLE  `drac`.`UserPreferences` (
  `userId` varchar(255) NOT NULL,
  `xml` varchar(65000) NOT NULL,
  PRIMARY KEY (`userId`)
);

--
-- Definition of table `drac`.`inbox`
--
CREATE TABLE  `drac`.`inbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` varchar(255) NOT NULL,
  `date` bigint(20) NOT NULL,
  `sender` varchar(255) NOT NULL,
  `receipient` varchar(255) NOT NULL,
  `cc` varchar(255) DEFAULT NULL,
  `unread` enum('FALSE','TRUE') DEFAULT NULL,
  `replied` enum('FALSE','TRUE') DEFAULT NULL,
  `deleted` enum('FALSE','TRUE') DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
);

ALTER TABLE `drac`.`inbox` ADD CONSTRAINT `fk_inbox_users_userid` FOREIGN KEY `fk_inbox_users_userid` (`userId`)
    REFERENCES `Users` (`userId`);

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `drac`.`logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `drac`.`logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `time` bigint(20) NOT NULL,
  `originator` varchar(255) COLLATE utf8_bin NOT NULL,
  `address` varchar(255) COLLATE utf8_bin NOT NULL,
  `billingGroup` varchar(255) COLLATE utf8_bin NOT NULL,
  `severity` enum('INFO','WARNING','MINOR','MAJOR','CRITICAL') COLLATE utf8_bin NOT NULL,
  `category` enum('NA','UNKNOWN','AUTHENTICATION','AUTHORIZATION','RESERVATIONGROUP','RESERVATION','SECURITY','NE','ENDPOINT','SYSTEM') COLLATE utf8_bin NOT NULL,
  `logType` enum('NA','UNKNOWN','CREATED','MODIFIED','DELETED','CANCELED','ALARM_RAISED','ALARM_CLEARED','MANAGED','UNMANAGED','ALIGNED','LOGGED_IN','LOGGED_OUT','EXECUTED','ACCESS_CHECK','REDUNDANCY','VERIFIED') COLLATE utf8_bin NOT NULL,
  `resource` varchar(255) COLLATE utf8_bin NOT NULL,
  `result` enum('NA','UNKNOWN','FAILED','SUCCESS') COLLATE utf8_bin NOT NULL,
  `descr` varchar(255) COLLATE utf8_bin NOT NULL,
  `xml` longtext COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  KEY `time` (`time`)
) AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drac`.`logs`
--

LOCK TABLES `drac`.`logs` WRITE;
/*!40000 ALTER TABLE `logs` DISABLE KEYS */;
INSERT INTO `drac`.`logs` VALUES (1,1292885966662,'admin','0:0:0:0:0:0:0:1%0:64459','N/A','INFO','AUTHENTICATION','VERIFIED','admin','SUCCESS','User credentials verified','<details />'),(2,1292885966683,'admin','0:0:0:0:0:0:0:1%0:64459','N/A','INFO','AUTHORIZATION','LOGGED_IN','admin','SUCCESS','User logged in from WEB_CLIENT_LOGIN','<details />'),(3,1292885989085,'admin','0:0:0:0:0:0:0:1%0:64459','N/A','INFO','AUTHENTICATION','LOGGED_OUT','admin','SUCCESS','User logged out','<details />'),(4,1292887758875,'admin','0:0:0:0:0:0:0:1%0:65518','N/A','INFO','AUTHENTICATION','VERIFIED','admin','SUCCESS','User credentials verified','<details />'),(5,1292887758914,'admin','0:0:0:0:0:0:0:1%0:65518','N/A','INFO','AUTHORIZATION','LOGGED_IN','admin','SUCCESS','User logged in from WEB_CLIENT_LOGIN','<details />'),(6,1292887850150,'admin','127.0.0.1','N/A','INFO','AUTHENTICATION','VERIFIED','admin','SUCCESS','User credentials verified','<details />'),(7,1292887850210,'admin','127.0.0.1','N/A','INFO','AUTHORIZATION','LOGGED_IN','admin','SUCCESS','User logged in from WEB_SERVICE_LOGIN','<details />');
/*!40000 ALTER TABLE `drac`.`logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Definition of table `drac`.`outbox`
--
CREATE TABLE  `drac`.`outbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` varchar(255) NOT NULL,
  `date` bigint(20) NOT NULL,
  `receipient` varchar(255) NOT NULL,
  `cc` varchar(255) DEFAULT NULL,
  `bcc` varchar(255) DEFAULT NULL,
  `deleted` enum('FALSE','TRUE') DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`id`),
  KEY `userId` (`userId`)
);

ALTER TABLE `drac`.`outbox` ADD CONSTRAINT `fk_outbox_users_userid` FOREIGN KEY `fk_outbox_users_userid` (`userId`)
    REFERENCES `Users` (`userId`);
    
    
--
-- Definition of table `drac`.`LightPathAlarmSummaries`
--
CREATE TABLE  `drac`.`LightPathAlarmSummaries` (
  `pk` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id` varchar(255) NOT NULL,
  `severity` varchar(5) NOT NULL,
  `occurredTime` bigint(20) NOT NULL,
  `serviceId` varchar(28) NOT NULL,
  `source` varchar(25) NOT NULL,
  PRIMARY KEY (`pk`),
  KEY `id` (`id`)
);

--
-- Definition of table `drac`.`Sites`
--
CREATE TABLE `drac`.`Sites` (
  `id` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL,
  `description` varchar(65000) NOT NULL,
  PRIMARY KEY (`id`)
);


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-12-21 18:42:33


