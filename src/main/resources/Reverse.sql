
create table `questionr` (
    `id` int primary key auto_increment,
    `keywords` varchar(200) not null,
	  `IDF` varchar(50) not null,
    `pageID` text(60000) not null,
    `TFIDF` text(60000) not null,
    `qualityAndPID` text(60000) not null
);

create table `peopler` (
    `id` int primary key auto_increment,
    `keywords` varchar(200) not null,
	  `IDF` varchar(50) not null,
    `pageID` text(60000) not null
);

create table `topicr` (
    `id` int primary key auto_increment,
    `keywords` varchar(200) not null,
    `IDF` varchar(50) not null,
	  `pageID` text(60000) not null
);

create table `collectionr` (
    `id` int primary key auto_increment,
    `keywords` varchar(200) not null,
    `IDF` varchar(4) not null,
	  `pageID` text(60000) not null
);

SHOW INDEX from questionr;
DROP INDEX zsc ON questionr;
ALTER TABLE questionr ADD INDEX zsc (keywords);
ALTER TABLE questionr engine=myisam;
ALTER TABLE questionr auto_increment=1;
ALTER TABLE peopler auto_increment=1;
ALTER TABLE topicr auto_increment=1;
